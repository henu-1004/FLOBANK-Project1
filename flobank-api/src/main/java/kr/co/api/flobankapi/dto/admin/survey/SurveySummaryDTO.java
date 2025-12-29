package kr.co.api.flobankapi.dto.admin.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurveySummaryDTO {
    private Long surveyId;
    private String title;
    private String description;
    private String isActive;
    private Date createdAt;
}
