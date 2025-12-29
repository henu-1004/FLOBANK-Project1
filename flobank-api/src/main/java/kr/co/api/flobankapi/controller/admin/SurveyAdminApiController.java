package kr.co.api.flobankapi.controller.admin;

import kr.co.api.flobankapi.dto.admin.survey.SurveyCreateRequest;
import kr.co.api.flobankapi.dto.admin.survey.SurveySummaryDTO;
import kr.co.api.flobankapi.service.admin.SurveyAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/admin/api/surveys", "/backend/admin/api/surveys"})
@RequiredArgsConstructor
public class SurveyAdminApiController {

    private final SurveyAdminService surveyAdminService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSurvey(@RequestBody SurveyCreateRequest request) {
        Long surveyId = surveyAdminService.createSurvey(request);
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("surveyId", surveyId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<SurveySummaryDTO>> getSurveys() {
        return ResponseEntity.ok(surveyAdminService.getSurveyList());
    }
}
