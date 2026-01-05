package kr.co.api.flobankapi.dto;

import lombok.Data;

@Data
public class SurveyResponseDTO {
    private Long qId;
    private String qKey;
    private Long optId;
    private String optValue;
    private String optCode;
    private String answerText;
}
