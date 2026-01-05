package kr.co.api.flobankapi.mapper;

import java.util.List;
import kr.co.api.flobankapi.dto.RecommendationInsertDTO;
import kr.co.api.flobankapi.dto.RecommendationProductCandidateDTO;
import kr.co.api.flobankapi.dto.RecommendationProductDTO;
import kr.co.api.flobankapi.dto.SurveyResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SurveyRecoMapper {
    Long findLatestRespId(@Param("custCode") String custCode, @Param("surveyId") Long surveyId);

    List<SurveyResponseDTO> findResponsesByRespId(@Param("respId") Long respId);

    List<RecommendationProductCandidateDTO> findRecommendableProducts();

    void deleteRecoTop3(@Param("custCode") String custCode, @Param("surveyId") Long surveyId);

    void insertRecoTop3(@Param("list") List<RecommendationInsertDTO> list);

    List<RecommendationProductDTO> findRecoTop3(@Param("custCode") String custCode,
                                                @Param("surveyId") Long surveyId);
}
