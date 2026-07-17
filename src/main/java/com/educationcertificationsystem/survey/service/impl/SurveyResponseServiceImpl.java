package com.educationcertificationsystem.survey.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.dto.survey.SurveyMatrixSelectionRequest;
import com.educationcertificationsystem.model.dto.survey.SurveySubmitAnswerRequest;
import com.educationcertificationsystem.model.dto.survey.SurveySubmitRequest;
import com.educationcertificationsystem.model.entity.SurveyQuestionMatrixColumn;
import com.educationcertificationsystem.model.entity.SurveyQuestionMatrixRow;
import com.educationcertificationsystem.model.entity.SurveyQuestionOption;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.educationcertificationsystem.model.entity.SurveyResponse;
import com.educationcertificationsystem.model.entity.SurveyResponseAnswer;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.vo.survey.SurveyFillVO;
import com.educationcertificationsystem.model.vo.survey.SurveyMatrixCellStatsVO;
import com.educationcertificationsystem.model.vo.survey.SurveyOptionStatsVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionMatrixColumnVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionMatrixRowVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionOptionVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionStatsVO;
import com.educationcertificationsystem.model.vo.survey.SurveyQuestionnaireDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseDetailVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseMatrixAnswerVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseOverviewVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponsePageVO;
import com.educationcertificationsystem.model.vo.survey.SurveyResponseQuestionAnswerVO;
import com.educationcertificationsystem.survey.mapper.SurveyResponseMapper;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireService;
import com.educationcertificationsystem.survey.service.SurveyResponseAnswerService;
import com.educationcertificationsystem.survey.service.SurveyResponseService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.user.service.SysUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
* @author Lizc233
* @description 针对表【survey_response(问卷作答表)】的数据库操作Service实现
* @createDate 2026-07-16 14:29:35
*/
@RequiredArgsConstructor
@Service
public class SurveyResponseServiceImpl extends ServiceImpl<SurveyResponseMapper, SurveyResponse>
    implements SurveyResponseService {

    private static final String STATUS_SUBMITTED = "SUBMITTED";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String TARGET_TEACHER = "TEACHER";
    private static final String TARGET_GRADUATE = "GRADUATE";
    private static final String TARGET_IN_SCHOOL = "IN_SCHOOL_STUDENT";
    private static final String QUESTION_SINGLE = "SINGLE";
    private static final String QUESTION_MULTIPLE = "MULTIPLE";
    private static final String QUESTION_SCALE = "SCALE";
    private static final String QUESTION_TEXT = "TEXT";
    private static final String QUESTION_MATRIX = "MATRIX";

    private final SurveyQuestionnaireService surveyQuestionnaireService;
    private final SurveyResponseAnswerService surveyResponseAnswerService;
    private final SysUserService sysUserService;

    @Override
    public SurveyFillVO getFillView(Long questionnaireId, Long respondentUserId) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.getDetail(questionnaireId);
        SurveyFillVO fillVO = new SurveyFillVO();
        fillVO.setQuestionnaireId(questionnaireId);
        fillVO.setTitle(questionnaire.getTitle());
        fillVO.setAnonymousFlag(questionnaire.getAnonymousFlag());
        fillVO.setPublishStatus(questionnaire.getPublishStatus());
        fillVO.setStartTime(questionnaire.getStartTime());
        fillVO.setEndTime(questionnaire.getEndTime());
        fillVO.setQuestionnaire(detail);
        Long submittedResponseId = findSubmittedResponseId(questionnaireId, respondentUserId);
        fillVO.setAlreadySubmitted(submittedResponseId == null ? 0 : 1);
        fillVO.setSubmittedResponseId(submittedResponseId);
        String message = validateFillAvailability(questionnaire, respondentUserId, false);
        fillVO.setCanSubmit(message == null && submittedResponseId == null ? 1 : 0);
        fillVO.setSubmitMessage(message == null
                ? (submittedResponseId == null ? "Can submit" : "Already submitted")
                : message);
        return fillVO;
    }

    @Override
    @Transactional
    public SurveyResponse submitResponse(Long questionnaireId, SurveySubmitRequest request) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        if (request == null) {
            throw new IllegalArgumentException("Submit request cannot be null");
        }
        String availabilityError = validateFillAvailability(questionnaire, request.getRespondentUserId(), true);
        if (availabilityError != null) {
            throw new IllegalStateException(availabilityError);
        }
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.getDetail(questionnaireId);
        Map<Long, SurveyQuestionDetailVO> questionMap = detail.getQuestions().stream()
                .collect(Collectors.toMap(SurveyQuestionDetailVO::getId, item -> item, (left, right) -> left, LinkedHashMap::new));
        Map<Long, SurveySubmitAnswerRequest> requestAnswerMap = normalizeAnswerMap(request.getAnswers());
        for (Long questionId : requestAnswerMap.keySet()) {
            if (!questionMap.containsKey(questionId)) {
                throw new IllegalArgumentException("Answer contains question outside current questionnaire: " + questionId);
            }
        }
        validateSubmitAnswers(detail.getQuestions(), requestAnswerMap);

        SurveyResponse response = new SurveyResponse();
        response.setQuestionnaireId(questionnaireId);
        response.setRespondentUserId(request.getRespondentUserId());
        response.setRespondentName(resolveRespondentName(request));
        response.setRespondentType(resolveRespondentType(questionnaire, request));
        response.setResponseToken(StringUtils.hasText(request.getResponseToken())
                ? request.getResponseToken().trim()
                : buildResponseToken(questionnaireId, request.getRespondentUserId()));
        response.setSubmitStatus(STATUS_SUBMITTED);
        response.setSubmittedAt(LocalDateTime.now());
        response.setIpAddress(normalizeText(request.getIpAddress()));
        response.setRemark(normalizeText(request.getRemark()));
        EntityAuditSupport.touchCreate(response);
        save(response);

        List<SurveyResponseAnswer> answers = new ArrayList<>();
        for (SurveyQuestionDetailVO question : detail.getQuestions()) {
            SurveySubmitAnswerRequest answerRequest = requestAnswerMap.get(question.getId());
            if (answerRequest == null) {
                continue;
            }
            answers.addAll(buildResponseAnswers(response.getId(), question, answerRequest));
        }
        if (!answers.isEmpty()) {
            surveyResponseAnswerService.saveBatch(answers);
        }
        return response;
    }

    @Override
    public Page<SurveyResponsePageVO> pageByCondition(Long questionnaireId,
                                                      long pageNum,
                                                      long pageSize,
                                                      String respondentType,
                                                      String keyword) {
        getRequiredQuestionnaire(questionnaireId);
        long current = Math.max(pageNum, 1L);
        long size = Math.max(pageSize, 1L);
        long offset = (current - 1) * size;
        long total = baseMapper.countByCondition(questionnaireId, normalizeEnum(respondentType), STATUS_SUBMITTED, normalizeText(keyword));
        List<SurveyResponsePageVO> records = total == 0
                ? List.of()
                : baseMapper.selectPageByCondition(offset, size, questionnaireId, normalizeEnum(respondentType),
                STATUS_SUBMITTED, normalizeText(keyword));
        boolean anonymous = isAnonymous(questionnaireId);
        if (anonymous) {
            records.forEach(this::maskAnonymous);
        }
        Page<SurveyResponsePageVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public SurveyResponseDetailVO getDetail(Long questionnaireId, Long responseId) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        SurveyResponse response = getOne(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getId, responseId)
                .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                .eq(SurveyResponse::getIsDeleted, 0), false);
        if (response == null) {
            return null;
        }
        SurveyResponseDetailVO detail = buildResponseDetail(questionnaire, response);
        if (questionnaire.getAnonymousFlag() != null && questionnaire.getAnonymousFlag() == 1) {
            detail.setRespondentName("Anonymous");
        }
        return detail;
    }

    @Override
    public SurveyResponseOverviewVO getOverview(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        long targetCount = surveyQuestionnaireService.resolveTargetUserIds(questionnaireId).size();
        long submittedCount = countSubmitted(questionnaireId);
        SurveyResponseOverviewVO overview = new SurveyResponseOverviewVO();
        overview.setQuestionnaireId(questionnaireId);
        overview.setTitle(questionnaire.getTitle());
        overview.setAnonymousFlag(questionnaire.getAnonymousFlag());
        overview.setPublishStatus(questionnaire.getPublishStatus());
        overview.setTargetCount(targetCount);
        overview.setSubmittedCount(submittedCount);
        overview.setPendingCount(Math.max(targetCount - submittedCount, 0));
        overview.setRecoveryRate(targetCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(submittedCount)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(targetCount), 2, RoundingMode.HALF_UP));
        overview.setStartTime(questionnaire.getStartTime());
        overview.setEndTime(questionnaire.getEndTime());
        return overview;
    }

    @Override
    public List<SurveyQuestionStatsVO> getQuestionStats(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        SurveyQuestionnaireDetailVO detail = surveyQuestionnaireService.getDetail(questionnaireId);
        List<SurveyResponse> responses = listSubmittedResponses(questionnaireId);
        Map<Long, List<SurveyResponseAnswer>> answerMap = loadAnswersByResponseIds(responses.stream().map(SurveyResponse::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(SurveyResponseAnswer::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        List<SurveyQuestionStatsVO> stats = new ArrayList<>();
        for (SurveyQuestionDetailVO question : detail.getQuestions()) {
            List<SurveyResponseAnswer> answers = answerMap.getOrDefault(question.getId(), List.of());
            stats.add(buildQuestionStats(questionnaire, question, answers));
        }
        return stats;
    }

    @Override
    public List<SurveyResponseDetailVO> exportResponses(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        return listSubmittedResponses(questionnaireId).stream()
                .sorted(Comparator.comparing(SurveyResponse::getSubmittedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SurveyResponse::getId, Comparator.reverseOrder()))
                .map(response -> buildResponseDetail(questionnaire, response))
                .toList();
    }

    @Override
    public List<LinkedHashMap<String, Object>> buildExportRows(Long questionnaireId, List<SurveyResponseDetailVO> details) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        SurveyQuestionnaireDetailVO questionnaireDetail = surveyQuestionnaireService.getDetail(questionnaireId);
        List<LinkedHashMap<String, Object>> rows = new ArrayList<>();
        for (SurveyResponseDetailVO detail : details) {
            LinkedHashMap<String, Object> row = new LinkedHashMap<>();
            row.put("Response ID", detail.getId());
            row.put("Respondent", questionnaire.getAnonymousFlag() != null && questionnaire.getAnonymousFlag() == 1
                    ? "Anonymous" : detail.getRespondentName());
            row.put("Respondent Type", detail.getRespondentType());
            row.put("Submitted At", detail.getSubmittedAt());
            row.put("IP Address", detail.getIpAddress());
            Map<Long, SurveyResponseQuestionAnswerVO> answerMap = detail.getAnswers().stream()
                    .collect(Collectors.toMap(SurveyResponseQuestionAnswerVO::getQuestionId, item -> item, (left, right) -> left));
            for (SurveyQuestionDetailVO question : questionnaireDetail.getQuestions()) {
                SurveyResponseQuestionAnswerVO answer = answerMap.get(question.getId());
                row.put(question.getQuestionCode() + " - " + question.getQuestionText(), formatAnswer(answer));
            }
            rows.add(row);
        }
        return rows;
    }

    private SurveyResponseDetailVO buildResponseDetail(SurveyQuestionnaire questionnaire, SurveyResponse response) {
        SurveyQuestionnaireDetailVO questionnaireDetail = surveyQuestionnaireService.getDetail(questionnaire.getId());
        List<SurveyResponseAnswer> answers = surveyResponseAnswerService.list(new LambdaQueryWrapper<SurveyResponseAnswer>()
                .eq(SurveyResponseAnswer::getResponseId, response.getId())
                .eq(SurveyResponseAnswer::getIsDeleted, 0)
                .orderByAsc(SurveyResponseAnswer::getId));
        Map<Long, List<SurveyResponseAnswer>> answersByQuestion = answers.stream()
                .collect(Collectors.groupingBy(SurveyResponseAnswer::getQuestionId, LinkedHashMap::new, Collectors.toList()));
        SurveyResponseDetailVO detailVO = BeanUtil.copyProperties(response, SurveyResponseDetailVO.class);
        List<SurveyResponseQuestionAnswerVO> answerVOs = new ArrayList<>();
        for (SurveyQuestionDetailVO question : questionnaireDetail.getQuestions()) {
            List<SurveyResponseAnswer> questionAnswers = answersByQuestion.getOrDefault(question.getId(), List.of());
            if (questionAnswers.isEmpty()) {
                continue;
            }
            answerVOs.add(buildResponseQuestionAnswerVO(question, questionAnswers));
        }
        detailVO.setAnswers(answerVOs);
        return detailVO;
    }

    private SurveyResponseQuestionAnswerVO buildResponseQuestionAnswerVO(SurveyQuestionDetailVO question,
                                                                         List<SurveyResponseAnswer> answers) {
        SurveyResponseQuestionAnswerVO vo = new SurveyResponseQuestionAnswerVO();
        vo.setQuestionId(question.getId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setQuestionText(question.getQuestionText());
        vo.setQuestionType(question.getQuestionType());
        String questionType = normalizeEnum(question.getQuestionType());
        if (QUESTION_TEXT.equals(questionType)) {
            SurveyResponseAnswer first = answers.get(0);
            vo.setAnswerText(first.getAnswerText());
            vo.setAnswerNumber(first.getAnswerNumber());
        } else if (QUESTION_SINGLE.equals(questionType) || QUESTION_MULTIPLE.equals(questionType) || QUESTION_SCALE.equals(questionType)) {
            Map<Long, SurveyQuestionOptionVO> optionMap = question.getOptions().stream()
                    .collect(Collectors.toMap(SurveyQuestionOptionVO::getId, item -> item, (left, right) -> left));
            vo.setSelectedOptionTexts(answers.stream()
                    .map(answer -> optionMap.get(answer.getOptionId()))
                    .filter(Objects::nonNull)
                    .map(SurveyQuestionOptionVO::getOptionText)
                    .toList());
        } else if (QUESTION_MATRIX.equals(questionType)) {
            Map<Long, SurveyQuestionMatrixRowVO> rowMap = question.getMatrixRows().stream()
                    .collect(Collectors.toMap(SurveyQuestionMatrixRowVO::getId, item -> item, (left, right) -> left));
            Map<Long, SurveyQuestionMatrixColumnVO> columnMap = question.getMatrixColumns().stream()
                    .collect(Collectors.toMap(SurveyQuestionMatrixColumnVO::getId, item -> item, (left, right) -> left));
            vo.setMatrixAnswers(answers.stream().map(answer -> {
                SurveyResponseMatrixAnswerVO matrixAnswerVO = new SurveyResponseMatrixAnswerVO();
                matrixAnswerVO.setRowId(answer.getRowId());
                matrixAnswerVO.setColumnId(answer.getColumnId());
                SurveyQuestionMatrixRowVO row = rowMap.get(answer.getRowId());
                SurveyQuestionMatrixColumnVO column = columnMap.get(answer.getColumnId());
                matrixAnswerVO.setRowText(row == null ? null : row.getRowText());
                matrixAnswerVO.setColumnText(column == null ? null : column.getColText());
                return matrixAnswerVO;
            }).toList());
        }
        return vo;
    }

    private SurveyQuestionStatsVO buildQuestionStats(SurveyQuestionnaire questionnaire,
                                                     SurveyQuestionDetailVO question,
                                                     List<SurveyResponseAnswer> answers) {
        SurveyQuestionStatsVO statsVO = new SurveyQuestionStatsVO();
        statsVO.setQuestionId(question.getId());
        statsVO.setQuestionCode(question.getQuestionCode());
        statsVO.setQuestionText(question.getQuestionText());
        statsVO.setQuestionType(question.getQuestionType());
        statsVO.setIsRequired(question.getIsRequired());
        long responseCount = answers.stream().map(SurveyResponseAnswer::getResponseId).distinct().count();
        statsVO.setResponseCount(responseCount);
        String questionType = normalizeEnum(question.getQuestionType());
        if (QUESTION_TEXT.equals(questionType)) {
            statsVO.setTextAnswers(answers.stream()
                    .map(SurveyResponseAnswer::getAnswerText)
                    .filter(StringUtils::hasText)
                    .toList());
            return statsVO;
        }
        if (QUESTION_SINGLE.equals(questionType) || QUESTION_MULTIPLE.equals(questionType) || QUESTION_SCALE.equals(questionType)) {
            Map<Long, Long> optionCountMap = answers.stream()
                    .filter(answer -> answer.getOptionId() != null)
                    .collect(Collectors.groupingBy(SurveyResponseAnswer::getOptionId, LinkedHashMap::new, Collectors.counting()));
            statsVO.setOptionStats(question.getOptions().stream().map(option -> {
                SurveyOptionStatsVO optionStatsVO = new SurveyOptionStatsVO();
                optionStatsVO.setOptionId(option.getId());
                optionStatsVO.setOptionCode(option.getOptionCode());
                optionStatsVO.setOptionText(option.getOptionText());
                optionStatsVO.setOptionValue(option.getOptionValue());
                long count = optionCountMap.getOrDefault(option.getId(), 0L);
                optionStatsVO.setCount(count);
                optionStatsVO.setRate(calculateRate(count, responseCount));
                return optionStatsVO;
            }).toList());
            return statsVO;
        }
        if (QUESTION_MATRIX.equals(questionType)) {
            Map<String, Long> cellCountMap = answers.stream()
                    .filter(answer -> answer.getRowId() != null && answer.getColumnId() != null)
                    .collect(Collectors.groupingBy(answer -> answer.getRowId() + ":" + answer.getColumnId(),
                            LinkedHashMap::new, Collectors.counting()));
            List<SurveyMatrixCellStatsVO> matrixStats = new ArrayList<>();
            for (SurveyQuestionMatrixRowVO row : question.getMatrixRows()) {
                for (SurveyQuestionMatrixColumnVO column : question.getMatrixColumns()) {
                    String key = row.getId() + ":" + column.getId();
                    long count = cellCountMap.getOrDefault(key, 0L);
                    SurveyMatrixCellStatsVO cellStatsVO = new SurveyMatrixCellStatsVO();
                    cellStatsVO.setRowId(row.getId());
                    cellStatsVO.setRowText(row.getRowText());
                    cellStatsVO.setColumnId(column.getId());
                    cellStatsVO.setColumnText(column.getColText());
                    cellStatsVO.setCount(count);
                    cellStatsVO.setRate(calculateRate(count, responseCount));
                    matrixStats.add(cellStatsVO);
                }
            }
            statsVO.setMatrixCellStats(matrixStats);
        }
        return statsVO;
    }

    private List<SurveyResponseAnswer> buildResponseAnswers(Long responseId,
                                                            SurveyQuestionDetailVO question,
                                                            SurveySubmitAnswerRequest request) {
        List<SurveyResponseAnswer> answers = new ArrayList<>();
        String questionType = normalizeEnum(question.getQuestionType());
        if (QUESTION_TEXT.equals(questionType)) {
            if (!StringUtils.hasText(request.getAnswerText()) && request.getAnswerNumber() == null) {
                return answers;
            }
            SurveyResponseAnswer answer = new SurveyResponseAnswer();
            answer.setResponseId(responseId);
            answer.setQuestionId(question.getId());
            answer.setAnswerText(normalizeText(request.getAnswerText()));
            answer.setAnswerNumber(request.getAnswerNumber());
            EntityAuditSupport.touchCreate(answer);
            answers.add(answer);
            return answers;
        }
        if (QUESTION_SINGLE.equals(questionType) || QUESTION_MULTIPLE.equals(questionType) || QUESTION_SCALE.equals(questionType)) {
            List<Long> optionIds = request.getOptionIds() == null ? List.of() : request.getOptionIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            Map<Long, SurveyQuestionOptionVO> optionMap = question.getOptions().stream()
                    .collect(Collectors.toMap(SurveyQuestionOptionVO::getId, item -> item, (left, right) -> left));
            for (Long optionId : optionIds) {
                SurveyQuestionOptionVO option = optionMap.get(optionId);
                if (option == null) {
                    throw new IllegalArgumentException("Invalid option selected for question: " + question.getQuestionCode());
                }
                SurveyResponseAnswer answer = new SurveyResponseAnswer();
                answer.setResponseId(responseId);
                answer.setQuestionId(question.getId());
                answer.setOptionId(optionId);
                if (option.getOptionValue() != null) {
                    answer.setAnswerText(option.getOptionValue());
                }
                EntityAuditSupport.touchCreate(answer);
                answers.add(answer);
            }
            return answers;
        }
        if (QUESTION_MATRIX.equals(questionType)) {
            List<SurveyMatrixSelectionRequest> selections = request.getMatrixSelections() == null
                    ? List.of()
                    : request.getMatrixSelections().stream().filter(Objects::nonNull).toList();
            Map<Long, SurveyQuestionMatrixRowVO> rowMap = question.getMatrixRows().stream()
                    .collect(Collectors.toMap(SurveyQuestionMatrixRowVO::getId, item -> item, (left, right) -> left));
            Map<Long, SurveyQuestionMatrixColumnVO> columnMap = question.getMatrixColumns().stream()
                    .collect(Collectors.toMap(SurveyQuestionMatrixColumnVO::getId, item -> item, (left, right) -> left));
            LinkedHashSet<String> uniqueCells = new LinkedHashSet<>();
            for (SurveyMatrixSelectionRequest selection : selections) {
                if (selection.getRowId() == null || selection.getColumnId() == null) {
                    throw new IllegalArgumentException("Matrix selection row and column are required");
                }
                if (!rowMap.containsKey(selection.getRowId()) || !columnMap.containsKey(selection.getColumnId())) {
                    throw new IllegalArgumentException("Invalid matrix selection for question: " + question.getQuestionCode());
                }
                String cellKey = selection.getRowId() + ":" + selection.getColumnId();
                if (!uniqueCells.add(cellKey)) {
                    continue;
                }
                SurveyResponseAnswer answer = new SurveyResponseAnswer();
                answer.setResponseId(responseId);
                answer.setQuestionId(question.getId());
                answer.setRowId(selection.getRowId());
                answer.setColumnId(selection.getColumnId());
                EntityAuditSupport.touchCreate(answer);
                answers.add(answer);
            }
        }
        return answers;
    }

    private void validateSubmitAnswers(List<SurveyQuestionDetailVO> questions,
                                       Map<Long, SurveySubmitAnswerRequest> requestAnswerMap) {
        for (SurveyQuestionDetailVO question : questions) {
            SurveySubmitAnswerRequest answerRequest = requestAnswerMap.get(question.getId());
            validateQuestionAnswer(question, answerRequest);
        }
    }

    private void validateQuestionAnswer(SurveyQuestionDetailVO question, SurveySubmitAnswerRequest request) {
        String questionType = normalizeEnum(question.getQuestionType());
        boolean required = question.getIsRequired() != null && question.getIsRequired() == 1;
        if (QUESTION_TEXT.equals(questionType)) {
            boolean answered = request != null && (StringUtils.hasText(request.getAnswerText()) || request.getAnswerNumber() != null);
            if (required && !answered) {
                throw new IllegalArgumentException("Required question is not answered: " + question.getQuestionCode());
            }
            return;
        }
        if (QUESTION_SINGLE.equals(questionType) || QUESTION_SCALE.equals(questionType)) {
            int count = request == null || request.getOptionIds() == null ? 0 : (int) request.getOptionIds().stream().filter(Objects::nonNull).distinct().count();
            if (required && count == 0) {
                throw new IllegalArgumentException("Required question is not answered: " + question.getQuestionCode());
            }
            if (count > 1) {
                throw new IllegalArgumentException("Single-choice question can only select one option: " + question.getQuestionCode());
            }
            return;
        }
        if (QUESTION_MULTIPLE.equals(questionType)) {
            int count = request == null || request.getOptionIds() == null ? 0 : (int) request.getOptionIds().stream().filter(Objects::nonNull).distinct().count();
            if (required && count == 0) {
                throw new IllegalArgumentException("Required question is not answered: " + question.getQuestionCode());
            }
            if (question.getMinSelect() != null && count > 0 && count < question.getMinSelect()) {
                throw new IllegalArgumentException("Question minimum selection not met: " + question.getQuestionCode());
            }
            if (question.getMaxSelect() != null && count > question.getMaxSelect()) {
                throw new IllegalArgumentException("Question maximum selection exceeded: " + question.getQuestionCode());
            }
            return;
        }
        if (QUESTION_MATRIX.equals(questionType)) {
            int count = request == null || request.getMatrixSelections() == null ? 0 : (int) request.getMatrixSelections().stream().filter(Objects::nonNull).count();
            if (required && count == 0) {
                throw new IllegalArgumentException("Required question is not answered: " + question.getQuestionCode());
            }
        }
    }

    private Map<Long, SurveySubmitAnswerRequest> normalizeAnswerMap(List<SurveySubmitAnswerRequest> answers) {
        Map<Long, SurveySubmitAnswerRequest> answerMap = new LinkedHashMap<>();
        if (answers == null) {
            return answerMap;
        }
        for (SurveySubmitAnswerRequest answer : answers) {
            if (answer == null || answer.getQuestionId() == null) {
                throw new IllegalArgumentException("Question id is required in answers");
            }
            if (answerMap.put(answer.getQuestionId(), answer) != null) {
                throw new IllegalArgumentException("Duplicate answer for question: " + answer.getQuestionId());
            }
        }
        return answerMap;
    }

    private String validateFillAvailability(SurveyQuestionnaire questionnaire, Long respondentUserId, boolean throwIfSubmitted) {
        if (respondentUserId == null) {
            return "Respondent user id is required";
        }
        if (!isQuestionnaireFillable(questionnaire)) {
            return buildUnavailableMessage(questionnaire);
        }
        List<Long> targetUserIds = surveyQuestionnaireService.resolveTargetUserIds(questionnaire.getId());
        if (!targetUserIds.contains(respondentUserId)) {
            return "Current user is not within the questionnaire scope";
        }
        Long submittedResponseId = findSubmittedResponseId(questionnaire.getId(), respondentUserId);
        if (submittedResponseId != null) {
            return throwIfSubmitted ? "Questionnaire has already been submitted" : null;
        }
        return null;
    }

    private boolean isQuestionnaireFillable(SurveyQuestionnaire questionnaire) {
        if (questionnaire == null) {
            return false;
        }
        if (!"PUBLISHED".equalsIgnoreCase(questionnaire.getPublishStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (questionnaire.getStartTime() != null && now.isBefore(questionnaire.getStartTime())) {
            return false;
        }
        if (questionnaire.getEndTime() != null && now.isAfter(questionnaire.getEndTime())) {
            return false;
        }
        return true;
    }

    private String buildUnavailableMessage(SurveyQuestionnaire questionnaire) {
        if (questionnaire == null) {
            return "Questionnaire not found";
        }
        if (!"PUBLISHED".equalsIgnoreCase(questionnaire.getPublishStatus())) {
            return "Questionnaire is not currently open for submission";
        }
        LocalDateTime now = LocalDateTime.now();
        if (questionnaire.getStartTime() != null && now.isBefore(questionnaire.getStartTime())) {
            return "Questionnaire has not started yet";
        }
        if (questionnaire.getEndTime() != null && now.isAfter(questionnaire.getEndTime())) {
            return "Questionnaire has ended";
        }
        return "Questionnaire is not currently open for submission";
    }

    private Long findSubmittedResponseId(Long questionnaireId, Long respondentUserId) {
        if (respondentUserId == null) {
            return null;
        }
        SurveyResponse response = getOne(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                .eq(SurveyResponse::getRespondentUserId, respondentUserId)
                .eq(SurveyResponse::getSubmitStatus, STATUS_SUBMITTED)
                .eq(SurveyResponse::getIsDeleted, 0)
                .orderByDesc(SurveyResponse::getSubmittedAt)
                .orderByDesc(SurveyResponse::getId)
                .last("limit 1"), false);
        return response == null ? null : response.getId();
    }

    private long countSubmitted(Long questionnaireId) {
        return count(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                .eq(SurveyResponse::getSubmitStatus, STATUS_SUBMITTED)
                .eq(SurveyResponse::getIsDeleted, 0));
    }

    private List<SurveyResponse> listSubmittedResponses(Long questionnaireId) {
        return list(new LambdaQueryWrapper<SurveyResponse>()
                .eq(SurveyResponse::getQuestionnaireId, questionnaireId)
                .eq(SurveyResponse::getSubmitStatus, STATUS_SUBMITTED)
                .eq(SurveyResponse::getIsDeleted, 0));
    }

    private List<SurveyResponseAnswer> loadAnswersByResponseIds(List<Long> responseIds) {
        if (responseIds == null || responseIds.isEmpty()) {
            return List.of();
        }
        return surveyResponseAnswerService.list(new LambdaQueryWrapper<SurveyResponseAnswer>()
                .in(SurveyResponseAnswer::getResponseId, responseIds)
                .eq(SurveyResponseAnswer::getIsDeleted, 0));
    }

    private SurveyQuestionnaire getRequiredQuestionnaire(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = surveyQuestionnaireService.getActiveQuestionnaireEntity(questionnaireId);
        if (questionnaire == null) {
            throw new IllegalArgumentException("Questionnaire not found");
        }
        return questionnaire;
    }

    private String resolveRespondentName(SurveySubmitRequest request) {
        if (StringUtils.hasText(request.getRespondentName())) {
            return request.getRespondentName().trim();
        }
        if (request.getRespondentUserId() == null) {
            return null;
        }
        SysUser user = sysUserService.getById(request.getRespondentUserId());
        return user == null ? null : user.getRealName();
    }

    private String resolveRespondentType(SurveyQuestionnaire questionnaire, SurveySubmitRequest request) {
        if (StringUtils.hasText(request.getRespondentType())) {
            return normalizeEnum(request.getRespondentType());
        }
        String targetType = normalizeEnum(questionnaire.getTargetObjectType());
        if (TARGET_TEACHER.equals(targetType)) {
            return TARGET_TEACHER;
        }
        if (TARGET_GRADUATE.equals(targetType)) {
            return TARGET_GRADUATE;
        }
        if (TARGET_IN_SCHOOL.equals(targetType)) {
            return TARGET_IN_SCHOOL;
        }
        return targetType;
    }

    private String buildResponseToken(Long questionnaireId, Long respondentUserId) {
        return "RESP-" + questionnaireId + "-" + (respondentUserId == null ? "NA" : respondentUserId)
                + "-" + UUID.randomUUID().toString().replace("-", "");
    }

    private void maskAnonymous(SurveyResponsePageVO item) {
        item.setRespondentName("Anonymous");
    }

    private boolean isAnonymous(Long questionnaireId) {
        SurveyQuestionnaire questionnaire = getRequiredQuestionnaire(questionnaireId);
        return questionnaire.getAnonymousFlag() != null && questionnaire.getAnonymousFlag() == 1;
    }

    private BigDecimal calculateRate(long count, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(count)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private String formatAnswer(SurveyResponseQuestionAnswerVO answer) {
        if (answer == null) {
            return "";
        }
        String type = normalizeEnum(answer.getQuestionType());
        if (QUESTION_TEXT.equals(type)) {
            if (StringUtils.hasText(answer.getAnswerText())) {
                return answer.getAnswerText();
            }
            return answer.getAnswerNumber() == null ? "" : answer.getAnswerNumber().toPlainString();
        }
        if (QUESTION_SINGLE.equals(type) || QUESTION_MULTIPLE.equals(type) || QUESTION_SCALE.equals(type)) {
            return answer.getSelectedOptionTexts() == null ? "" : String.join(", ", answer.getSelectedOptionTexts());
        }
        if (QUESTION_MATRIX.equals(type)) {
            if (answer.getMatrixAnswers() == null) {
                return "";
            }
            return answer.getMatrixAnswers().stream()
                    .map(item -> (item.getRowText() == null ? "" : item.getRowText())
                            + ":" + (item.getColumnText() == null ? "" : item.getColumnText()))
                    .collect(Collectors.joining("; "));
        }
        return "";
    }

    private String normalizeEnum(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}




