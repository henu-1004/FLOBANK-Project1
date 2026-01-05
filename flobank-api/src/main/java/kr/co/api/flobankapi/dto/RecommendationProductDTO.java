package kr.co.api.flobankapi.dto;

import lombok.Data;

@Data
public class RecommendationProductDTO {
    private Long recoId;
    private Integer rankNo;
    private String productId;
    private String productName;
    private String productInfo;
    private String productDescription;
    private String productCurrency;
}
