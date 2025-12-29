package kr.co.api.flobankapi.dto.admin.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveyOptionRequest {
    private String optCode;
    private String optText;
    private String optValue;
    private Integer optOrder;
    private String isActive;
}
