package kr.co.api.flobankapi.dto;

import lombok.Data;

@Data
public class RecommendationPrefillDTO {
    private String productId;
    private String currency;
    private Integer termMonths;
    private Integer recommendedAmount;
    private String withdrawalAccountType;
}
