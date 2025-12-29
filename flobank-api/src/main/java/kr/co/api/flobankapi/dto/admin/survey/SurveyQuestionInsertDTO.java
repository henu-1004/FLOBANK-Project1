package kr.co.api.flobankapi.dto.admin.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestionInsertDTO {
    private Long qId;
    private Long surveyId;
    private Integer qNo;
    private String qKey;
    private String qText;
    private String qType;
    private String isRequired;
    private Integer maxSelect;
    private String isActive;
    private String createdBy;
    private String updatedBy;
}
