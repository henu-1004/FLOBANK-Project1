package kr.co.api.flobankapi.dto.admin.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyQuestionRequest {
    private Integer qNo;
    private String qKey;
    private String qText;
    private String qType;
    private String isRequired;
    private Integer maxSelect;
    private String isActive;
    private List<SurveyOptionRequest> options;
}
