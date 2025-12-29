package kr.co.api.flobankapi.service.admin;

import kr.co.api.flobankapi.dto.admin.survey.SurveyCreateRequest;
import kr.co.api.flobankapi.dto.admin.survey.SurveyInsertDTO;
import kr.co.api.flobankapi.dto.admin.survey.SurveyOptionInsertDTO;
import kr.co.api.flobankapi.dto.admin.survey.SurveyQuestionInsertDTO;
import kr.co.api.flobankapi.dto.admin.survey.SurveyQuestionRequest;
import kr.co.api.flobankapi.dto.admin.survey.SurveyOptionRequest;
import kr.co.api.flobankapi.dto.admin.survey.SurveySummaryDTO;
import kr.co.api.flobankapi.mapper.admin.SurveyAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SurveyAdminService {

    private static final String DEFAULT_ADMIN = "admin";

    private final SurveyAdminMapper surveyAdminMapper;

    @Transactional
    public Long createSurvey(SurveyCreateRequest request) {
        SurveyInsertDTO survey = SurveyInsertDTO.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isActive(normalizeYn(request.getIsActive()))
                .createdBy(DEFAULT_ADMIN)
                .updatedBy(DEFAULT_ADMIN)
                .build();

        surveyAdminMapper.insertSurvey(survey);

        List<SurveyQuestionRequest> questions = request.getQuestions();
        if (questions == null) {
            return survey.getSurveyId();
        }

        for (int i = 0; i < questions.size(); i++) {
            SurveyQuestionRequest questionRequest = questions.get(i);
            int qNo = questionRequest.getQNo() != null ? questionRequest.getQNo() : i + 1;
            String qKey = StringUtils.hasText(questionRequest.getQKey())
                    ? questionRequest.getQKey()
                    : "Q" + qNo;

            SurveyQuestionInsertDTO question = SurveyQuestionInsertDTO.builder()
                    .surveyId(survey.getSurveyId())
                    .qNo(qNo)
                    .qKey(qKey)
                    .qText(questionRequest.getQText())
                    .qType(normalizeType(questionRequest.getQType()))
                    .isRequired(normalizeYn(questionRequest.getIsRequired()))
                    .maxSelect(questionRequest.getMaxSelect())
                    .isActive(normalizeYn(questionRequest.getIsActive()))
                    .createdBy(DEFAULT_ADMIN)
                    .updatedBy(DEFAULT_ADMIN)
                    .build();

            surveyAdminMapper.insertQuestion(question);

            if ("TEXT".equals(question.getQType())) {
                continue;
            }

            List<SurveyOptionRequest> options = questionRequest.getOptions();
            if (options == null) {
                options = Collections.emptyList();
            }

            for (int j = 0; j < options.size(); j++) {
                SurveyOptionRequest optionRequest = options.get(j);
                int optOrder = optionRequest.getOptOrder() != null ? optionRequest.getOptOrder() : j + 1;
                String optCode = StringUtils.hasText(optionRequest.getOptCode())
                        ? optionRequest.getOptCode()
                        : "OPT" + optOrder;

                SurveyOptionInsertDTO option = SurveyOptionInsertDTO.builder()
                        .qId(question.getQId())
                        .optCode(optCode)
                        .optText(optionRequest.getOptText())
                        .optValue(optionRequest.getOptValue())
                        .optOrder(optOrder)
                        .isActive(normalizeYn(optionRequest.getIsActive()))
                        .createdBy(DEFAULT_ADMIN)
                        .updatedBy(DEFAULT_ADMIN)
                        .build();

                surveyAdminMapper.insertOption(option);
            }
        }

        return survey.getSurveyId();
    }

    public List<SurveySummaryDTO> getSurveyList() {
        return surveyAdminMapper.selectSurveyList();
    }

    private String normalizeYn(String value) {
        if (value == null) {
            return "Y";
        }
        return "N".equalsIgnoreCase(value) ? "N" : "Y";
    }

    private String normalizeType(String value) {
        if (!StringUtils.hasText(value)) {
            return "TEXT";
        }
        return value.trim().toUpperCase();
    }
}
