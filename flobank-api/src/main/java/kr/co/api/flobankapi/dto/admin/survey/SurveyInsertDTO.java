package kr.co.api.flobankapi.dto.admin.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyInsertDTO {
    private Long surveyId;
    private String title;
    private String description;
    private String isActive;
    private String createdBy;
    private String updatedBy;
}
