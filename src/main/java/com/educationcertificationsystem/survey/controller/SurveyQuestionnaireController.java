package com.educationcertificationsystem.survey.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.dto.survey.SurveyDispatchRequest;
import com.educationcertificationsystem.model.dto.survey.SurveyQuestionnaireSaveRequest;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.educationcertificationsystem.model.vo.survey.SurveyPublishTaskVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnaireDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnairePageVO;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Survey Questionnaire")
@RequestMapping("/api/surveys/questionnaires")
public class SurveyQuestionnaireController {

    private final SurveyQuestionnaireService surveyQuestionnaireService;

    @GetMapping
    @Operation(summary = "Page questionnaires")
    public Result<Page<SurveyQuestionnairePageVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String publishStatus,
            @RequestParam(required = false) String questionnaireType,
            @RequestParam(required = false) String targetObjectType,
            @RequestParam(required = false) String keyword) {
        return Result.success(surveyQuestionnaireService.pageByCondition(
                pageNum, pageSize, publishStatus, questionnaireType, targetObjectType, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Questionnaire detail")
    public Result<SurveyQuestionnaireDetailVO> detail(@PathVariable Long id) {
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.getDetail(id);
        if (detail == null) {
            return Result.error("Questionnaire not found");
        }
        return Result.success(detail);
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "Preview questionnaire")
    public Result<SurveyQuestionnaireDetailVO> preview(@PathVariable Long id) {
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.preview(id);
        if (detail == null) {
            return Result.error("Questionnaire not found");
        }
        return Result.success(detail);
    }

    @GetMapping("/{id}/publish-tasks")
    @Operation(summary = "Page publish tasks")
    public Result<Page<SurveyPublishTaskVO>> publishTasks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return Result.success(surveyQuestionnaireService.pagePublishTasks(id, pageNum, pageSize));
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Create questionnaire")
    public Result<SurveyQuestionnaireDetailVO> create(@RequestBody SurveyQuestionnaireSaveRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.createQuestionnaire(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Create questionnaire failed", ex);
            return Result.error("Create questionnaire failed");
        }
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update questionnaire")
    public Result<SurveyQuestionnaireDetailVO> update(@PathVariable Long id,
                                                      @RequestBody SurveyQuestionnaireSaveRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.updateQuestionnaire(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update questionnaire failed, id={}", id, ex);
            return Result.error("Update questionnaire failed");
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Delete questionnaire")
    public Result<String> delete(@PathVariable Long id) {
        try {
            surveyQuestionnaireService.deleteQuestionnaire(id);
            return Result.success("Deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Delete questionnaire failed, id={}", id, ex);
            return Result.error("Delete questionnaire failed");
        }
    }

    @PostMapping("/{id}/publish")
    @Transactional
    @Operation(summary = "Publish questionnaire")
    public Result<SurveyQuestionnaire> publish(@PathVariable Long id, @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.publish(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Publish questionnaire failed, id={}", id, ex);
            return Result.error("Publish questionnaire failed");
        }
    }

    @PostMapping("/{id}/retry-publish")
    @Transactional
    @Operation(summary = "Retry publish questionnaire")
    public Result<SurveyQuestionnaire> retryPublish(@PathVariable Long id,
                                                    @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.retryPublish(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Retry publish questionnaire failed, id={}", id, ex);
            return Result.error("Retry publish questionnaire failed");
        }
    }

    @PostMapping("/{id}/revoke")
    @Transactional
    @Operation(summary = "Revoke questionnaire")
    public Result<SurveyQuestionnaire> revoke(@PathVariable Long id,
                                              @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.revoke(id, request == null ? null : request.getRemark()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Revoke questionnaire failed, id={}", id, ex);
            return Result.error("Revoke questionnaire failed");
        }
    }

    @PostMapping("/{id}/end")
    @Transactional
    @Operation(summary = "End questionnaire")
    public Result<SurveyQuestionnaire> end(@PathVariable Long id,
                                           @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.end(id, request == null ? null : request.getRemark()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("End questionnaire failed, id={}", id, ex);
            return Result.error("End questionnaire failed");
        }
    }

    @PostMapping("/{id}/deadline-reminder")
    @Transactional
    @Operation(summary = "Send deadline reminder")
    public Result<SurveyQuestionnaire> deadlineReminder(@PathVariable Long id,
                                                        @RequestBody(required = false) SurveyDispatchRequest request) {
        try {
            return Result.success(surveyQuestionnaireService.sendDeadlineReminder(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Send deadline reminder failed, id={}", id, ex);
            return Result.error("Send deadline reminder failed");
        }
    }
}
