package kr.co.api.flobankapi.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.api.flobankapi.dto.RecommendationInsertDTO;
import kr.co.api.flobankapi.dto.RecommendationPrefillDTO;
import kr.co.api.flobankapi.dto.RecommendationProductCandidateDTO;
import kr.co.api.flobankapi.dto.RecommendationProductDTO;
import kr.co.api.flobankapi.dto.SurveyResponseDTO;
import kr.co.api.flobankapi.mapper.SurveyRecoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SurveyRecommendationService {

    private static final String Q_GOAL = "Q_GOAL";
    private static final String Q_PRIORITY = "Q_PRIORITY";
    private static final String Q_LIQUIDITY = "Q_LIQUIDITY";
    private static final String Q_CCY_START = "Q_CCY_START";
    private static final String Q_AMOUNT_RANGE = "Q_AMOUNT_RANGE";
    private static final String Q_TERM_MONTHS = "Q_TERM_MONTHS";
    private static final String Q_FX_ATTITUDE = "Q_FX_ATTITUDE";
    private static final String Q_WDR_KRW_ACCOUNT = "Q_WDR_KRW_ACCOUNT";

    private static final Map<String, Integer> AMOUNT_RANGE_MAP = Map.of(
            "AMT_LT_1M", 500_000,
            "AMT_1_5M", 3_000_000,
            "AMT_5_10M", 7_500_000,
            "AMT_GT_10M", 15_000_000
    );

    private final SurveyRecoMapper surveyRecoMapper;

    public List<RecommendationProductDTO> getRecommendations(String custCode, Long surveyId) {
        List<RecommendationProductDTO> existing = surveyRecoMapper.findRecoTop3(custCode, surveyId);
        if (!existing.isEmpty()) {
            return existing;
        }

        return calculateAndSaveRecommendations(custCode, surveyId);
    }

    @Transactional
    public List<RecommendationProductDTO> calculateAndSaveRecommendations(String custCode, Long surveyId) {
        Long respId = surveyRecoMapper.findLatestRespId(custCode, surveyId);
        if (respId == null) {
            return List.of();
        }

        List<SurveyResponseDTO> responses = surveyRecoMapper.findResponsesByRespId(respId);
        Map<String, List<SurveyResponseDTO>> responseMap = responses.stream()
                .filter(response -> response.getQKey() != null)
                .collect(Collectors.groupingBy(SurveyResponseDTO::getQKey));

        RecommendationSignals signals = buildSignals(responseMap);
        List<RecommendationProductCandidateDTO> candidates = surveyRecoMapper.findRecommendableProducts();
        if (candidates.isEmpty()) {
            return List.of();
        }

        List<ScoredProduct> scoredProducts = new ArrayList<>();
        for (RecommendationProductCandidateDTO candidate : candidates) {
            int score = scoreProduct(candidate, signals);
            scoredProducts.add(new ScoredProduct(candidate, score));
        }

        scoredProducts.sort(Comparator
                .comparingInt(ScoredProduct::score).reversed()
                .thenComparing(sp -> sp.product().getProductName(), Comparator.nullsLast(String::compareTo))
                .thenComparing(sp -> sp.product().getProductId(), Comparator.nullsLast(String::compareTo)));

        List<RecommendationProductCandidateDTO> top3 = scoredProducts.stream()
                .limit(3)
                .map(ScoredProduct::product)
                .toList();

        surveyRecoMapper.deleteRecoTop3(custCode, surveyId);

        List<RecommendationInsertDTO> inserts = new ArrayList<>();
        for (int i = 0; i < top3.size(); i++) {
            RecommendationProductCandidateDTO product = top3.get(i);
            RecommendationInsertDTO insert = new RecommendationInsertDTO();
            insert.setCustCode(custCode);
            insert.setSurveyId(surveyId);
            insert.setRankNo(i + 1);
            insert.setProductId(product.getProductId());
            inserts.add(insert);
        }

        if (!inserts.isEmpty()) {
            surveyRecoMapper.insertRecoTop3(inserts);
        }

        return surveyRecoMapper.findRecoTop3(custCode, surveyId);
    }

    public RecommendationPrefillDTO buildPrefill(String custCode, Long surveyId, String productId) {
        Long respId = surveyRecoMapper.findLatestRespId(custCode, surveyId);
        if (respId == null) {
            return null;
        }

        List<SurveyResponseDTO> responses = surveyRecoMapper.findResponsesByRespId(respId);
        Map<String, List<SurveyResponseDTO>> responseMap = responses.stream()
                .filter(response -> response.getQKey() != null)
                .collect(Collectors.groupingBy(SurveyResponseDTO::getQKey));

        RecommendationPrefillDTO prefillDTO = new RecommendationPrefillDTO();
        prefillDTO.setProductId(productId);

        String currency = getFirstOptValue(responseMap, Q_CCY_START);
        if (currency == null) {
            List<String> currencies = getAllOptValues(responseMap, Q_CCY_START);
            if (!currencies.isEmpty()) {
                currency = currencies.get(0);
            }
        }
        prefillDTO.setCurrency(currency);

        String termValue = getFirstOptValue(responseMap, Q_TERM_MONTHS);
        if (termValue != null) {
            try {
                prefillDTO.setTermMonths(Integer.parseInt(termValue));
            } catch (NumberFormatException ex) {
                log.warn("Unable to parse term months: {}", termValue, ex);
            }
        }

        String amountRange = getFirstOptValue(responseMap, Q_AMOUNT_RANGE);
        if (amountRange != null && AMOUNT_RANGE_MAP.containsKey(amountRange)) {
            prefillDTO.setRecommendedAmount(AMOUNT_RANGE_MAP.get(amountRange));
        }

        String withdrawalType = getFirstOptValue(responseMap, Q_WDR_KRW_ACCOUNT);
        prefillDTO.setWithdrawalAccountType(withdrawalType);

        return prefillDTO;
    }

    private RecommendationSignals buildSignals(Map<String, List<SurveyResponseDTO>> responseMap) {
        String goal = getFirstOptValue(responseMap, Q_GOAL);
        String priority = getFirstOptValue(responseMap, Q_PRIORITY);
        String liquidity = getFirstOptValue(responseMap, Q_LIQUIDITY);
        String fxAttitude = getFirstOptValue(responseMap, Q_FX_ATTITUDE);

        boolean preferLiquidity = "PRIOR_LIQ".equals(priority) || "LIQ_NEED".equals(liquidity);
        boolean preferStable = "GOAL_STABLE".equals(goal) || "PRIOR_RATE".equals(priority);
        boolean preferFx = "GOAL_FX".equals(goal) || "PRIOR_FX".equals(priority) || "FX_UTIL".equals(fxAttitude);
        boolean preferOverseas = "GOAL_OVERSEAS".equals(goal);
        boolean preferEvent = "GOAL_EVENT".equals(goal) || "PRIOR_EVENT".equals(priority);

        List<String> currencies = getAllOptValues(responseMap, Q_CCY_START);

        return new RecommendationSignals(preferLiquidity, preferStable, preferFx, preferOverseas, preferEvent, currencies);
    }

    private int scoreProduct(RecommendationProductCandidateDTO product, RecommendationSignals signals) {
        if (product == null) {
            return 0;
        }

        int score = 0;
        String haystack = buildProductSearchText(product);

        if (signals.preferLiquidity() && containsAny(haystack, List.of("보통", "mmda", "수퍼", "자유", "입출금"))) {
            score += 30;
        }
        if (signals.preferStable() && containsAny(haystack, List.of("정기", "거치", "특별"))) {
            score += 25;
        }
        if (signals.preferFx() && containsAny(haystack, List.of("환율", "fx"))) {
            score += 20;
        }
        if (signals.preferOverseas() && containsAny(haystack, List.of("해외", "유학", "여행"))) {
            score += 15;
        }
        if (signals.preferEvent() && containsAny(haystack, List.of("이벤트", "경품", "추천", "슈카"))) {
            score += 10;
        }

        if (signals.preferStable() && Integer.valueOf(1).equals(product.getDpstType())) {
            score += 5;
        }
        if (signals.preferLiquidity() && Integer.valueOf(2).equals(product.getDpstType())) {
            score += 5;
        }
        if (signals.preferLiquidity() && "Y".equalsIgnoreCase(product.getDpstPartWdrwYn())) {
            score += 5;
        }

        Set<String> productCurrencies = splitCurrencies(product.getProductCurrency());
        if (!signals.currencies().isEmpty() && !productCurrencies.isEmpty()) {
            int matches = 0;
            for (String currency : signals.currencies()) {
                if (productCurrencies.contains(currency)) {
                    matches++;
                }
            }
            score += matches * 3;
        }

        return score;
    }

    private String buildProductSearchText(RecommendationProductCandidateDTO product) {
        StringBuilder builder = new StringBuilder();
        if (product.getProductName() != null) {
            builder.append(product.getProductName()).append(" ");
        }
        if (product.getProductInfo() != null) {
            builder.append(product.getProductInfo()).append(" ");
        }
        if (product.getProductDescription() != null) {
            builder.append(product.getProductDescription()).append(" ");
        }
        return builder.toString().toLowerCase(Locale.KOREAN);
    }

    private boolean containsAny(String text, List<String> keywords) {
        if (text == null || text.isBlank()) {
            return false;
        }
        for (String keyword : keywords) {
            if (keyword != null && !keyword.isBlank() && text.contains(keyword.toLowerCase(Locale.KOREAN))) {
                return true;
            }
        }
        return false;
    }

    private Set<String> splitCurrencies(String currencies) {
        if (currencies == null || currencies.isBlank()) {
            return Set.of();
        }
        String[] parts = currencies.split(",");
        Set<String> trimmed = new HashSet<>();
        for (String part : parts) {
            if (part != null && !part.isBlank()) {
                trimmed.add(part.trim());
            }
        }
        return trimmed;
    }

    private String getFirstOptValue(Map<String, List<SurveyResponseDTO>> responseMap, String key) {
        List<SurveyResponseDTO> responses = responseMap.getOrDefault(key, List.of());
        return responses.stream()
                .map(SurveyResponseDTO::getOptValue)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<String> getAllOptValues(Map<String, List<SurveyResponseDTO>> responseMap, String key) {
        List<SurveyResponseDTO> responses = responseMap.getOrDefault(key, List.of());
        return responses.stream()
                .map(SurveyResponseDTO::getOptValue)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private record RecommendationSignals(boolean preferLiquidity,
                                         boolean preferStable,
                                         boolean preferFx,
                                         boolean preferOverseas,
                                         boolean preferEvent,
                                         List<String> currencies) {
    }

    private record ScoredProduct(RecommendationProductCandidateDTO product, int score) {
    }
}
