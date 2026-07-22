package com.educationcertificationsystem.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.dto.survey.SurveySubmitRequest;
import com.educationcertificationsystem.model.entity.SurveyResponse;
import com.educationcertificationsystem.model.vo.survey.SurveyFillVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionStatsVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseOverviewVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponsePageVO;
import com.educationcertificationsystem.survey.service.SurveyResponseService;
import com.educationcertificationsystem.support.CsvExportSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Survey Response")
@RequestMapping("/api/surveys/questionnaires/{questionnaireId}")
public class SurveyResponseController {

    private final SurveyResponseService surveyResponseService;

    @GetMapping("/fill")
    @Operation(summary = "Get fill view")
    public Result<SurveyFillVO> fillView(@PathVariable Long questionnaireId,
                                         @RequestParam(required = false) Long respondentUserId) {
        try {
            return Result.success(surveyResponseService.getFillView(questionnaireId, respondentUserId));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Get fill view failed, questionnaireId={}", questionnaireId, ex);
            return Result.error("Get fill view failed");
        }
    }

    @PostMapping("/responses/submit")
    @Operation(summary = "Submit questionnaire")
    public Result<SurveyResponse> submit(@PathVariable Long questionnaireId,
                                         @RequestBody SurveySubmitRequest request) {
        try {
            return Result.success(surveyResponseService.submitResponse(questionnaireId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Submit survey response failed, questionnaireId={}", questionnaireId, ex);
            return Result.error("Submit survey response failed");
        }
    }

    @GetMapping("/responses")
    @Operation(summary = "Page responses")
    public Result<Page<SurveyResponsePageVO>> list(@PathVariable Long questionnaireId,
                                                   @RequestParam(defaultValue = "1") long pageNum,
                                                   @RequestParam(defaultValue = "10") long pageSize,
                                                   @RequestParam(required = false) String respondentType,
                                                   @RequestParam(required = false) String keyword) {
        return Result.success(surveyResponseService.pageByCondition(
                questionnaireId, pageNum, pageSize, respondentType, keyword));
    }

    @GetMapping("/responses/{responseId}")
    @Operation(summary = "Response detail")
    public Result<SurveyResponseDetailVO> detail(@PathVariable Long questionnaireId,
                                                 @PathVariable Long responseId) {
        SurveyResponseDetailVO detail = surveyResponseService.getDetail(questionnaireId, responseId);
        if (detail == null) {
            return Result.error("Response not found");
        }
        return Result.success(detail);
    }

    @GetMapping("/response-stats/overview")
    @Operation(summary = "Response overview stats")
    public Result<SurveyResponseOverviewVO> overview(@PathVariable Long questionnaireId) {
        return Result.success(surveyResponseService.getOverview(questionnaireId));
    }

    @GetMapping("/response-stats/questions")
    @Operation(summary = "Question stats")
    public Result<List<SurveyQuestionStatsVO>> questionStats(@PathVariable Long questionnaireId) {
        return Result.success(surveyResponseService.getQuestionStats(questionnaireId));
    }

    @GetMapping("/export/responses")
    @Operation(summary = "Export responses data")
    public Result<List<SurveyResponseDetailVO>> export(@PathVariable Long questionnaireId) {
        return Result.success(surveyResponseService.exportResponses(questionnaireId));
    }

    @GetMapping("/download/responses")
    @Operation(summary = "Download responses")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long questionnaireId) {
        List<SurveyResponseDetailVO> details = surveyResponseService.exportResponses(questionnaireId);
        List<LinkedHashMap<String, Object>> rows = surveyResponseService.buildExportRows(questionnaireId, details);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "survey_responses_" + questionnaireId + "_" + timestamp + ".csv";
        return CsvExportSupport.csv(fileName, buildHeaders(rows), buildBodyRows(rows));
    }

    private List<String> buildHeaders(List<LinkedHashMap<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of("Response ID", "Respondent", "Respondent Type", "Submitted At");
        }
        return List.copyOf(rows.get(0).keySet());
    }

    private List<List<?>> buildBodyRows(List<LinkedHashMap<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        List<List<?>> bodyRows = new java.util.ArrayList<>(rows.size());
        for (LinkedHashMap<String, Object> row : rows) {
            bodyRows.add(new java.util.ArrayList<>(row.values()));
        }
        return bodyRows;
    }
}
