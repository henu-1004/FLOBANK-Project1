package kr.co.api.flobankapi.dto;

import lombok.Data;

@Data
public class RecommendationInsertDTO {
    private String custCode;
    private Long surveyId;
    private Integer rankNo;
    private String productId;
}
