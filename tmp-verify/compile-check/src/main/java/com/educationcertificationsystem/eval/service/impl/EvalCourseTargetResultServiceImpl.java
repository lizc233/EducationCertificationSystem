package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.course.service.CourseScoreBatchService;
import com.educationcertificationsystem.course.service.CourseScoreDetailService;
import com.educationcertificationsystem.course.service.EduCourseAssessmentMethodService;
import com.educationcertificationsystem.course.service.EduCourseObjectiveService;
import com.educationcertificationsystem.course.service.TeachingTaskService;
import com.educationcertificationsystem.eval.mapper.EvalCourseTargetResultMapper;
import com.educationcertificationsystem.eval.service.EvalCourseTargetResultService;
import com.educationcertificationsystem.eval.service.EvalModelScopeService;
import com.educationcertificationsystem.eval.service.EvalModelService;
import com.educationcertificationsystem.eval.service.EvalRecalcJobService;
import com.educationcertificationsystem.eval.service.EvalResultDetailService;
import com.educationcertificationsystem.model.dto.eval.EvalCourseTargetCalculateRequest;
import com.educationcertificationsystem.model.entity.CourseScoreBatch;
import com.educationcertificationsystem.model.entity.CourseScoreDetail;
import com.educationcertificationsystem.model.entity.EduCourseAssessmentMethod;
import com.educationcertificationsystem.model.entity.EduCourseObjective;
import com.educationcertificationsystem.model.entity.EvalCourseTargetResult;
import com.educationcertificationsystem.model.entity.EvalModel;
import com.educationcertificationsystem.model.entity.EvalModelItem;
import com.educationcertificationsystem.model.entity.EvalModelScope;
import com.educationcertificationsystem.model.entity.EvalRecalcJob;
import com.educationcertificationsystem.model.entity.EvalResultDetail;
import com.educationcertificationsystem.model.entity.TeachingTask;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultContributionVO;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultPageVO;
import com.educationcertificationsystem.support.EntityAuditSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EvalCourseTargetResultServiceImpl
        extends ServiceImpl<EvalCourseTargetResultMapper, EvalCourseTargetResult>
        implements EvalCourseTargetResultService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String RESULT_TYPE_COURSE_TARGET = "COURSE_TARGET";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)");

    private final CourseScoreBatchService courseScoreBatchService;
    private final CourseScoreDetailService courseScoreDetailService;
    private final EduCourseObjectiveService eduCourseObjectiveService;
    private final EduCourseAssessmentMethodService eduCourseAssessmentMethodService;
    private final TeachingTaskService teachingTaskService;
    private final EvalModelService evalModelService;
    private final EvalModelScopeService evalModelScopeService;
    private final EvalResultDetailService evalResultDetailService;
    private final EvalRecalcJobService evalRecalcJobService;

    @Override
    public EvalCourseTargetResult getActiveById(Long id) {
        return baseMapper.selectActiveById(id);
    }

    @Override
    public Page<EvalCourseTargetResultPageVO> pageByCondition(long pageNum, long pageSize, Long taskId, Long semesterId,
                                                              Long courseId, Long classId, Long objectiveId, Long modelId,
                                                              Integer lockedFlag, String keyword) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        long offset = (current - 1) * size;
        long total = baseMapper.countByCondition(taskId, semesterId, courseId, classId, objectiveId, modelId,
                lockedFlag, keyword);
        List<EvalCourseTargetResultPageVO> records = total == 0
                ? List.of()
                : baseMapper.selectPageByCondition(offset, size, taskId, semesterId, courseId, classId, objectiveId,
                modelId, lockedFlag, keyword);
        Page<EvalCourseTargetResultPageVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public EvalCourseTargetResultDetailVO getDetail(Long id) {
        EvalCourseTargetResult result = getActiveById(id);
        if (result == null) {
            return null;
        }

        TeachingTask task = teachingTaskService.getById(result.getTaskId());
        EduCourseObjective objective = eduCourseObjectiveService.getById(result.getObjectiveId());
        EvalModel model = evalModelService.getById(result.getModelId());

        EvalCourseTargetResultPageVO pageVO = baseMapper.selectPageByCondition(0L, 1L, result.getTaskId(), null, null,
                null, result.getObjectiveId(), result.getModelId(), null, null).stream().findFirst().orElse(null);

        EvalCourseTargetResultDetailVO detail = new EvalCourseTargetResultDetailVO();
        detail.setId(result.getId());
        detail.setTaskId(result.getTaskId());
        detail.setObjectiveId(result.getObjectiveId());
        detail.setModelId(result.getModelId());
        detail.setAttainmentRate(result.getAttainmentRate());
        detail.setAttainmentValue(result.getAttainmentValue());
        detail.setTargetValue(result.getTargetValue());
        detail.setResultLevel(result.getResultLevel());
        detail.setCalcTime(result.getCalcTime());
        detail.setRecalculationCount(result.getRecalculationCount());
        detail.setLockedFlag(result.getLockedFlag());
        detail.setRemark(result.getRemark());

        if (pageVO != null) {
            detail.setTaskCode(pageVO.getTaskCode());
            detail.setCourseId(pageVO.getCourseId());
            detail.setCourseName(pageVO.getCourseName());
            detail.setSemesterId(pageVO.getSemesterId());
            detail.setSemesterName(pageVO.getSemesterName());
            detail.setClassId(pageVO.getClassId());
            detail.setClassName(pageVO.getClassName());
            detail.setObjectiveCode(pageVO.getObjectiveCode());
            detail.setObjectiveName(pageVO.getObjectiveName());
            detail.setModelName(pageVO.getModelName());
        } else {
            if (task != null) {
                detail.setTaskCode(task.getTaskCode());
                detail.setCourseId(task.getCourseId());
                detail.setSemesterId(task.getSemesterId());
                detail.setClassId(task.getClassId());
            }
            if (objective != null) {
                detail.setObjectiveCode(objective.getObjectiveCode());
                detail.setObjectiveName(objective.getObjectiveName());
            }
            if (model != null) {
                detail.setModelName(model.getModelName());
            }
        }
        if (model != null) {
            detail.setModelCode(model.getModelCode());
        }
        if (objective != null) {
            detail.setObjectiveStandard(objective.getAchievementStandard());
        }

        detail.setContributions(buildContributionVOs(evalResultDetailService.listByResult(RESULT_TYPE_COURSE_TARGET, id)));
        return detail;
    }

    @Override
    @Transactional
    public List<EvalCourseTargetResult> calculate(EvalCourseTargetCalculateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.getTaskId() == null) {
            throw new IllegalArgumentException("Task id cannot be null");
        }
        if (request.getModelId() == null) {
            throw new IllegalArgumentException("Model id cannot be null");
        }

        EvalRecalcJob job = evalRecalcJobService.createJob("COURSE_TARGET_CALCULATE", "TASK", request.getTaskId(),
                request.getRemark());
        try {
            evalRecalcJobService.markRunning(job.getId());
            List<EvalCourseTargetResult> results = calculateInternal(request.getTaskId(), request.getModelId(),
                    request.getObjectiveIds(), request.getRemark(), false);
            evalRecalcJobService.markSuccess(job.getId());
            return results;
        } catch (RuntimeException ex) {
            evalRecalcJobService.markFailed(job.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public EvalCourseTargetResult recalculate(Long resultId, String remark) {
        EvalCourseTargetResult existing = getActiveById(resultId);
        if (existing == null) {
            throw new IllegalArgumentException("Result not found");
        }
        if (existing.getLockedFlag() != null && existing.getLockedFlag() == 1) {
            throw new IllegalStateException("Confirmed result cannot be recalculated");
        }

        EvalRecalcJob job = evalRecalcJobService.createJob("COURSE_TARGET_RECALCULATE", "RESULT", resultId, remark);
        try {
            evalRecalcJobService.markRunning(job.getId());
            List<EvalCourseTargetResult> results = calculateInternal(existing.getTaskId(), existing.getModelId(),
                    List.of(existing.getObjectiveId()), remark, true);
            evalRecalcJobService.markSuccess(job.getId());
            return results.get(0);
        } catch (RuntimeException ex) {
            evalRecalcJobService.markFailed(job.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public EvalCourseTargetResult confirm(Long resultId) {
        EvalCourseTargetResult result = getActiveById(resultId);
        if (result == null) {
            throw new IllegalArgumentException("Result not found");
        }
        result.setLockedFlag(1);
        EntityAuditSupport.touchUpdate(result);
        updateById(result);
        return result;
    }

    private List<EvalCourseTargetResult> calculateInternal(Long taskId, Long modelId, List<Long> objectiveIds,
                                                           String remark, boolean recalculate) {
        TeachingTask task = requireTask(taskId);
        EvalModel model = requireEnabledModel(modelId);
        validateModelScope(task, modelId, model.getScopeType());

        List<CourseScoreBatch> batches = courseScoreBatchService.list(new LambdaQueryWrapper<CourseScoreBatch>()
                .eq(CourseScoreBatch::getTaskId, taskId)
                .eq(CourseScoreBatch::getIsDeleted, 0)
                .in(objectiveIds != null && !objectiveIds.isEmpty(), CourseScoreBatch::getObjectiveId, objectiveIds)
                .orderByAsc(CourseScoreBatch::getObjectiveId)
                .orderByAsc(CourseScoreBatch::getMethodId)
                .orderByAsc(CourseScoreBatch::getId));
        if (batches.isEmpty()) {
            throw new IllegalStateException("No score batches found for the selected task and objectives");
        }

        LinkedHashMap<Long, List<CourseScoreBatch>> batchesByObjective = new LinkedHashMap<>();
        for (CourseScoreBatch batch : batches) {
            batchesByObjective.computeIfAbsent(batch.getObjectiveId(), key -> new ArrayList<>()).add(batch);
        }

        if (objectiveIds != null && !objectiveIds.isEmpty()) {
            for (Long objectiveId : objectiveIds) {
                if (!batchesByObjective.containsKey(objectiveId)) {
                    throw new IllegalStateException("No score batches found for objective: " + objectiveId);
                }
            }
        }

        List<EvalModelItem> modelItems = evalModelService.getDetail(modelId) == null
                ? List.of()
                : evalModelService.getDetail(modelId).getItems().stream().map(itemVO -> {
                    EvalModelItem item = new EvalModelItem();
                    item.setItemCode(itemVO.getItemCode());
                    item.setItemName(itemVO.getItemName());
                    item.setItemType(itemVO.getItemType());
                    item.setWeightPercent(itemVO.getWeightPercent());
                    item.setEnabled(itemVO.getEnabled());
                    return item;
                }).toList();

        List<EvalCourseTargetResult> results = new ArrayList<>();
        for (Map.Entry<Long, List<CourseScoreBatch>> entry : batchesByObjective.entrySet()) {
            results.add(calculateOneObjective(task, model, modelItems, entry.getKey(), entry.getValue(), remark,
                    recalculate));
        }
        return results;
    }

    private EvalCourseTargetResult calculateOneObjective(TeachingTask task, EvalModel model, List<EvalModelItem> modelItems,
                                                         Long objectiveId, List<CourseScoreBatch> batches, String remark,
                                                         boolean recalculate) {
        EduCourseObjective objective = eduCourseObjectiveService.getById(objectiveId);
        if (objective == null || (objective.getIsDeleted() != null && objective.getIsDeleted() != 0)) {
            throw new IllegalStateException("Course objective not found: " + objectiveId);
        }
        if (task.getCourseId() != null && objective.getCourseId() != null
                && !task.getCourseId().equals(objective.getCourseId())) {
            throw new IllegalStateException("Objective does not belong to the selected task course");
        }

        BigDecimal targetValue = resolveTargetValue(objective, model);
        List<EvalResultDetail> details = new ArrayList<>();
        BigDecimal attainmentValue = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (CourseScoreBatch batch : batches) {
            EduCourseAssessmentMethod method = batch.getMethodId() == null
                    ? null
                    : eduCourseAssessmentMethodService.getById(batch.getMethodId());
            BigDecimal weight = resolveWeight(modelItems, method, batch.getMethodId());
            if (weight.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal sourceValue = calculateBatchAverage(batch.getId());
            BigDecimal contributionValue = sourceValue.multiply(weight)
                    .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);

            EvalResultDetail detail = new EvalResultDetail();
            detail.setSourceType("SCORE_BATCH");
            detail.setSourceId(batch.getId());
            detail.setWeightPercent(scale(weight));
            detail.setSourceValue(scale(sourceValue));
            detail.setContributionValue(scale(contributionValue));
            detail.setRemark(method == null ? batch.getBatchNo() : method.getMethodName());
            details.add(detail);

            attainmentValue = attainmentValue.add(contributionValue);
            totalWeight = totalWeight.add(weight);
        }

        if (details.isEmpty()) {
            throw new IllegalStateException("No submitted scores found for objective: " + objectiveId);
        }
        if (totalWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No valid calculation weight found for objective: " + objectiveId);
        }

        attainmentValue = scale(attainmentValue);
        BigDecimal attainmentRate = targetValue.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : scale(attainmentValue.multiply(ONE_HUNDRED).divide(targetValue, 4, RoundingMode.HALF_UP));
        String resultLevel = resolveResultLevel(attainmentRate);

        EvalCourseTargetResult result = baseMapper.selectByUnique(task.getId(), objectiveId, model.getId());
        boolean isNewResult = result == null;
        if (isNewResult) {
            result = new EvalCourseTargetResult();
            result.setTaskId(task.getId());
            result.setObjectiveId(objectiveId);
            result.setModelId(model.getId());
            result.setRecalculationCount(0);
            result.setLockedFlag(0);
            EntityAuditSupport.touchCreate(result);
        } else {
            if (result.getLockedFlag() != null && result.getLockedFlag() == 1) {
                throw new IllegalStateException("Result has been confirmed and cannot be recalculated");
            }
            result.setRecalculationCount(result.getRecalculationCount() == null
                    ? 1
                    : result.getRecalculationCount() + 1);
            EntityAuditSupport.touchUpdate(result);
        }

        result.setAttainmentValue(attainmentValue);
        result.setTargetValue(targetValue);
        result.setAttainmentRate(attainmentRate);
        result.setResultLevel(resultLevel);
        result.setCalcTime(LocalDateTime.now());
        result.setRemark(StringUtils.hasText(remark) ? remark : (recalculate ? "Recalculated" : "Calculated"));
        if (isNewResult) {
            save(result);
        } else {
            updateById(result);
        }

        evalResultDetailService.replaceDetails(RESULT_TYPE_COURSE_TARGET, result.getId(), details);
        return result;
    }

    private TeachingTask requireTask(Long taskId) {
        TeachingTask task = teachingTaskService.getById(taskId);
        if (task == null || (task.getIsDeleted() != null && task.getIsDeleted() != 0)) {
            throw new IllegalArgumentException("Task not found");
        }
        return task;
    }

    private EvalModel requireEnabledModel(Long modelId) {
        EvalModel model = evalModelService.getActiveById(modelId);
        if (model == null) {
            throw new IllegalArgumentException("Model not found");
        }
        if (model.getEnabled() == null || model.getEnabled() == 0) {
            throw new IllegalStateException("Selected model is not enabled");
        }
        return model;
    }

    private void validateModelScope(TeachingTask task, Long modelId, String scopeType) {
        List<EvalModelScope> scopes = evalModelScopeService.listActiveByModelId(modelId);
        if (scopes.isEmpty()) {
            throw new IllegalStateException("Model scope is not configured");
        }
        Long scopeValue = null;
        if ("COURSE".equalsIgnoreCase(scopeType)) {
            scopeValue = task.getCourseId();
        } else if ("PROGRAM_VERSION".equalsIgnoreCase(scopeType)) {
            scopeValue = task.getProgramVersionId();
        }
        if (scopeValue == null) {
            throw new IllegalStateException("Task does not have the required scope field for the selected model");
        }
        for (EvalModelScope scope : scopes) {
            if (scopeValue.equals(scope.getScopeId())) {
                return;
            }
        }
        throw new IllegalStateException("Selected model is not applicable to the current task");
    }

    private BigDecimal calculateBatchAverage(Long batchId) {
        List<CourseScoreDetail> details = courseScoreDetailService.list(new LambdaQueryWrapper<CourseScoreDetail>()
                .eq(CourseScoreDetail::getBatchId, batchId)
                .eq(CourseScoreDetail::getIsDeleted, 0)
                .and(wrapper -> wrapper.eq(CourseScoreDetail::getSubmitStatus, "SUBMITTED")
                        .or()
                        .eq(CourseScoreDetail::getLockedFlag, 1)));
        if (details.isEmpty()) {
            throw new IllegalStateException("No submitted scores found for batch: " + batchId);
        }

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        for (CourseScoreDetail detail : details) {
            BigDecimal score = detail.getTotalScore() != null
                    ? detail.getTotalScore()
                    : detail.getWeightedScore() != null ? detail.getWeightedScore() : detail.getRawScore();
            if (score != null) {
                total = total.add(score);
                count++;
            }
        }
        if (count == 0) {
            throw new IllegalStateException("No valid scores found for batch: " + batchId);
        }
        return total.divide(BigDecimal.valueOf(count), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveWeight(List<EvalModelItem> modelItems, EduCourseAssessmentMethod method, Long methodId) {
        if (method != null) {
            for (EvalModelItem item : modelItems) {
                if (item == null || item.getEnabled() != null && item.getEnabled() == 0) {
                    continue;
                }
                if (!"ASSESSMENT_METHOD".equalsIgnoreCase(item.getItemType())
                        && !"METHOD".equalsIgnoreCase(item.getItemType())) {
                    continue;
                }
                String itemCode = item.getItemCode();
                if (!StringUtils.hasText(itemCode)) {
                    continue;
                }
                if (itemCode.trim().equalsIgnoreCase(method.getMethodCode())
                        || itemCode.trim().equals(String.valueOf(methodId))) {
                    return item.getWeightPercent() == null ? BigDecimal.ZERO : item.getWeightPercent();
                }
            }
            if (method.getRatioPercent() != null) {
                return method.getRatioPercent();
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal resolveTargetValue(EduCourseObjective objective, EvalModel model) {
        BigDecimal parsedObjectiveTarget = extractNumericValue(objective.getAchievementStandard());
        if (parsedObjectiveTarget != null && parsedObjectiveTarget.compareTo(BigDecimal.ZERO) > 0) {
            return scale(parsedObjectiveTarget);
        }
        if (model.getThresholdValue() != null && model.getThresholdValue().compareTo(BigDecimal.ZERO) > 0) {
            return scale(model.getThresholdValue());
        }
        return ONE_HUNDRED;
    }

    private BigDecimal extractNumericValue(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        Matcher matcher = NUMBER_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group(1));
    }

    private String resolveResultLevel(BigDecimal attainmentRate) {
        if (attainmentRate.compareTo(ONE_HUNDRED) >= 0) {
            return "ACHIEVED";
        }
        if (attainmentRate.compareTo(new BigDecimal("90")) >= 0) {
            return "NEARLY_ACHIEVED";
        }
        return "NOT_ACHIEVED";
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private List<EvalCourseTargetResultContributionVO> buildContributionVOs(List<EvalResultDetail> details) {
        Map<Long, CourseScoreBatch> batchMap = new HashMap<>();
        Map<Long, EduCourseAssessmentMethod> methodMap = new HashMap<>();
        LinkedHashSet<Long> batchIds = new LinkedHashSet<>();
        for (EvalResultDetail detail : details) {
            if ("SCORE_BATCH".equals(detail.getSourceType()) && detail.getSourceId() != null) {
                batchIds.add(detail.getSourceId());
            }
        }
        if (!batchIds.isEmpty()) {
            for (CourseScoreBatch batch : courseScoreBatchService.listByIds(batchIds)) {
                if (batch != null) {
                    batchMap.put(batch.getId(), batch);
                    if (batch.getMethodId() != null && !methodMap.containsKey(batch.getMethodId())) {
                        EduCourseAssessmentMethod method = eduCourseAssessmentMethodService.getById(batch.getMethodId());
                        if (method != null) {
                            methodMap.put(method.getId(), method);
                        }
                    }
                }
            }
        }

        List<EvalCourseTargetResultContributionVO> result = new ArrayList<>(details.size());
        for (EvalResultDetail detail : details) {
            EvalCourseTargetResultContributionVO vo = new EvalCourseTargetResultContributionVO();
            vo.setDetailId(detail.getId());
            vo.setSourceType(detail.getSourceType());
            vo.setSourceId(detail.getSourceId());
            vo.setWeightPercent(detail.getWeightPercent());
            vo.setSourceValue(detail.getSourceValue());
            vo.setContributionValue(detail.getContributionValue());
            vo.setRemark(detail.getRemark());
            if ("SCORE_BATCH".equals(detail.getSourceType()) && detail.getSourceId() != null) {
                CourseScoreBatch batch = batchMap.get(detail.getSourceId());
                if (batch != null) {
                    EduCourseAssessmentMethod method = batch.getMethodId() == null ? null : methodMap.get(batch.getMethodId());
                    vo.setSourceName(method == null
                            ? batch.getBatchNo()
                            : method.getMethodName() + (StringUtils.hasText(batch.getBatchNo()) ? " / " + batch.getBatchNo() : ""));
                }
            }
            result.add(vo);
        }
        return result;
    }
}
