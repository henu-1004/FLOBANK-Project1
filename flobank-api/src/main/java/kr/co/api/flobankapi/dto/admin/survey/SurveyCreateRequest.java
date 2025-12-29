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
public class SurveyCreateRequest {
    private String title;
    private String description;
    private String isActive;
    private List<SurveyQuestionRequest> questions;
}
