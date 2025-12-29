package kr.co.api.flobankapi.mapper.admin;

import kr.co.api.flobankapi.dto.admin.survey.SurveyInsertDTO;
import kr.co.api.flobankapi.dto.admin.survey.SurveyOptionInsertDTO;
import kr.co.api.flobankapi.dto.admin.survey.SurveyQuestionInsertDTO;
import kr.co.api.flobankapi.dto.admin.survey.SurveySummaryDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SurveyAdminMapper {
    int insertSurvey(SurveyInsertDTO survey);
    int insertQuestion(SurveyQuestionInsertDTO question);
    int insertOption(SurveyOptionInsertDTO option);
    List<SurveySummaryDTO> selectSurveyList();
}
