package kr.co.api.flobankapi.dto;

import lombok.Data;

@Data
public class RecommendationProductCandidateDTO {
    private String productId;
    private String productName;
    private String productInfo;
    private String productDescription;
    private String productCurrency;
    private Integer dpstType;
    private Integer dpstRateType;
    private String dpstAddPayYn;
    private String dpstPartWdrwYn;
}
