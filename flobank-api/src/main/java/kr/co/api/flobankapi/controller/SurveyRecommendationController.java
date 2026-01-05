package kr.co.api.flobankapi.controller;

import java.util.List;
import kr.co.api.flobankapi.dto.RecommendationPrefillDTO;
import kr.co.api.flobankapi.dto.RecommendationProductDTO;
import kr.co.api.flobankapi.service.SurveyRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/survey")
@RequiredArgsConstructor
public class SurveyRecommendationController {

    private final SurveyRecommendationService surveyRecommendationService;

    @GetMapping("/recommendations")
    public List<RecommendationProductDTO> getRecommendations(@RequestParam(required = false) String custCode,
                                                             @RequestParam Long surveyId) {
        String resolvedCustCode = resolveCustCode(custCode);
        if (resolvedCustCode == null) {
            return List.of();
        }

        return surveyRecommendationService.getRecommendations(resolvedCustCode, surveyId);
    }

    @GetMapping("/recommendations/refresh")
    public List<RecommendationProductDTO> refreshRecommendations(@RequestParam(required = false) String custCode,
                                                                 @RequestParam Long surveyId) {
        String resolvedCustCode = resolveCustCode(custCode);
        if (resolvedCustCode == null) {
            return List.of();
        }

        return surveyRecommendationService.calculateAndSaveRecommendations(resolvedCustCode, surveyId);
    }

    @GetMapping("/prefill")
    public RecommendationPrefillDTO getPrefill(@RequestParam(required = false) String custCode,
                                               @RequestParam Long surveyId,
                                               @RequestParam String productId) {
        String resolvedCustCode = resolveCustCode(custCode);
        if (resolvedCustCode == null) {
            return null;
        }

        return surveyRecommendationService.buildPrefill(resolvedCustCode, surveyId, productId);
    }

    private String resolveCustCode(String custCode) {
        if (custCode != null && !custCode.isBlank()) {
            return custCode;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }
}
