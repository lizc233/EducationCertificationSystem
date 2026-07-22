package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.course.service.EduCourseObjectiveIndicatorPointService;
import com.educationcertificationsystem.course.service.EduTeacherService;
import com.educationcertificationsystem.course.service.TeachingTaskService;
import com.educationcertificationsystem.eval.mapper.EvalGraduationRequirementResultMapper;
import com.educationcertificationsystem.eval.service.EvalCourseTargetResultService;
import com.educationcertificationsystem.eval.service.EvalGraduationRequirementResultService;
import com.educationcertificationsystem.eval.service.EvalModelScopeService;
import com.educationcertificationsystem.eval.service.EvalModelService;
import com.educationcertificationsystem.eval.service.EvalRecalcJobService;
import com.educationcertificationsystem.eval.service.EvalResultDetailService;
import com.educationcertificationsystem.model.dto.eval.EvalGraduationRequirementCalculateRequest;
import com.educationcertificationsystem.model.dto.eval.EvalGraduationWarningNotifyRequest;
import com.educationcertificationsystem.model.dto.notice.NoticeSendRequest;
import com.educationcertificationsystem.model.entity.EduCourseObjectiveIndicatorPoint;
import com.educationcertificationsystem.model.entity.EduTeacher;
import com.educationcertificationsystem.model.entity.EvalCourseTargetResult;
import com.educationcertificationsystem.model.entity.EvalGraduationRequirementResult;
import com.educationcertificationsystem.model.entity.EvalModel;
import com.educationcertificationsystem.model.entity.EvalModelScope;
import com.educationcertificationsystem.model.entity.EvalRecalcJob;
import com.educationcertificationsystem.model.entity.EvalResultDetail;
import com.educationcertificationsystem.model.entity.TeachingTask;
import com.educationcertificationsystem.model.entity.TrGraduationRequirement;
import com.educationcertificationsystem.model.entity.TrProgramVersion;
import com.educationcertificationsystem.model.entity.TrRequirementIndicatorSupport;
import com.educationcertificationsystem.model.vo.eval.EvalCourseTargetResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementContributionVO;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalGraduationRequirementResultPageVO;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.program.service.TrGraduationRequirementService;
import com.educationcertificationsystem.program.service.TrProgramVersionService;
import com.educationcertificationsystem.program.service.TrRequirementIndicatorSupportService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class EvalGraduationRequirementResultServiceImpl
        extends ServiceImpl<EvalGraduationRequirementResultMapper, EvalGraduationRequirementResult>
        implements EvalGraduationRequirementResultService {

    private static final BigDecimal DEFAULT_THRESHOLD = new BigDecimal("60");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final String RESULT_TYPE_GRADUATION_REQUIREMENT = "GRADUATION_REQUIREMENT";

    private final TrProgramVersionService trProgramVersionService;
    private final TrGraduationRequirementService trGraduationRequirementService;
    private final TrRequirementIndicatorSupportService trRequirementIndicatorSupportService;
    private final EduCourseObjectiveIndicatorPointService eduCourseObjectiveIndicatorPointService;
    private final EvalCourseTargetResultService evalCourseTargetResultService;
    private final EvalModelService evalModelService;
    private final EvalModelScopeService evalModelScopeService;
    private final EvalResultDetailService evalResultDetailService;
    private final EvalRecalcJobService evalRecalcJobService;
    private final TeachingTaskService teachingTaskService;
    private final EduTeacherService eduTeacherService;
    private final NoticeMessageService noticeMessageService;

    @Override
    public EvalGraduationRequirementResult getActiveById(Long id) {
        return baseMapper.selectActiveById(id);
    }

    @Override
    public Page<EvalGraduationRequirementResultPageVO> pageByCondition(long pageNum, long pageSize,
                                                                       Long programVersionId, Long majorId,
                                                                       Long requirementId, Long modelId,
                                                                       Integer warningFlag, Integer lockFlag,
                                                                       String keyword) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        long offset = (current - 1) * size;
        long total = baseMapper.countByCondition(programVersionId, majorId, requirementId, modelId, warningFlag,
                lockFlag, keyword);
        List<EvalGraduationRequirementResultPageVO> records = total == 0
                ? List.of()
                : baseMapper.selectPageByCondition(offset, size, programVersionId, majorId, requirementId, modelId,
                warningFlag, lockFlag, keyword);
        Page<EvalGraduationRequirementResultPageVO> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public EvalGraduationRequirementResultDetailVO getDetail(Long id) {
        EvalGraduationRequirementResult result = getActiveById(id);
        if (result == null) {
            return null;
        }

        EvalGraduationRequirementResultPageVO pageVO = baseMapper.selectPageByCondition(
                0L, 1L, result.getProgramVersionId(), null, result.getRequirementId(), result.getModelId(), null, null, null)
                .stream()
                .findFirst()
                .orElse(null);
        TrProgramVersion programVersion = trProgramVersionService.getById(result.getProgramVersionId());
        TrGraduationRequirement requirement = trGraduationRequirementService.getById(result.getRequirementId());
        EvalModel model = evalModelService.getById(result.getModelId());

        EvalGraduationRequirementResultDetailVO detail = new EvalGraduationRequirementResultDetailVO();
        detail.setId(result.getId());
        detail.setProgramVersionId(result.getProgramVersionId());
        detail.setRequirementId(result.getRequirementId());
        detail.setModelId(result.getModelId());
        detail.setAttainmentRate(result.getAttainmentRate());
        detail.setAttainmentValue(result.getAttainmentValue());
        detail.setThresholdValue(result.getThresholdValue());
        detail.setWarningFlag(result.getWarningFlag());
        detail.setCalcTime(result.getCalcTime());
        detail.setLockFlag(result.getLockFlag());
        detail.setRemark(result.getRemark());

        if (pageVO != null) {
            detail.setProgramVersionName(pageVO.getProgramVersionName());
            detail.setMajorId(pageVO.getMajorId());
            detail.setMajorName(pageVO.getMajorName());
            detail.setRequirementCode(pageVO.getRequirementCode());
            detail.setRequirementName(pageVO.getRequirementName());
            detail.setModelName(pageVO.getModelName());
        } else {
            if (programVersion != null) {
                detail.setMajorId(programVersion.getMajorId());
                detail.setProgramVersionName(StringUtils.hasText(programVersion.getVersionName())
                        ? programVersion.getVersionName()
                        : programVersion.getVersionNo());
            }
            if (requirement != null) {
                detail.setRequirementCode(requirement.getRequirementCode());
                detail.setRequirementName(requirement.getRequirementName());
            }
            if (model != null) {
                detail.setModelName(model.getModelName());
            }
        }
        if (model != null) {
            detail.setModelCode(model.getModelCode());
        }

        detail.setContributions(buildContributionVOs(
                evalResultDetailService.listByResult(RESULT_TYPE_GRADUATION_REQUIREMENT, id)));
        return detail;
    }

    @Override
    @Transactional
    public List<EvalGraduationRequirementResult> calculate(EvalGraduationRequirementCalculateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.getProgramVersionId() == null) {
            throw new IllegalArgumentException("Program version id cannot be null");
        }
        if (request.getModelId() == null) {
            throw new IllegalArgumentException("Model id cannot be null");
        }

        EvalRecalcJob job = evalRecalcJobService.createJob("GRADUATION_REQUIREMENT_CALCULATE", "PROGRAM_VERSION",
                request.getProgramVersionId(), request.getRemark());
        try {
            evalRecalcJobService.markRunning(job.getId());
            List<EvalGraduationRequirementResult> results = calculateInternal(request.getProgramVersionId(),
                    request.getModelId(), request.getRequirementIds(), request.getRemark(), false);
            evalRecalcJobService.markSuccess(job.getId());
            return results;
        } catch (RuntimeException ex) {
            evalRecalcJobService.markFailed(job.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public EvalGraduationRequirementResult recalculate(Long resultId, String remark) {
        EvalGraduationRequirementResult existing = getActiveById(resultId);
        if (existing == null) {
            throw new IllegalArgumentException("Result not found");
        }
        if (existing.getLockFlag() != null && existing.getLockFlag() == 1) {
            throw new IllegalStateException("Confirmed result cannot be recalculated");
        }

        EvalRecalcJob job = evalRecalcJobService.createJob("GRADUATION_REQUIREMENT_RECALCULATE", "RESULT", resultId,
                remark);
        try {
            evalRecalcJobService.markRunning(job.getId());
            List<EvalGraduationRequirementResult> results = calculateInternal(existing.getProgramVersionId(),
                    existing.getModelId(), List.of(existing.getRequirementId()), remark, true);
            evalRecalcJobService.markSuccess(job.getId());
            return results.get(0);
        } catch (RuntimeException ex) {
            evalRecalcJobService.markFailed(job.getId(), ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public EvalGraduationRequirementResult confirm(Long resultId) {
        EvalGraduationRequirementResult result = getActiveById(resultId);
        if (result == null) {
            throw new IllegalArgumentException("Result not found");
        }
        result.setLockFlag(1);
        EntityAuditSupport.touchUpdate(result);
        updateById(result);
        return result;
    }

    @Override
    @Transactional
    public EvalGraduationRequirementResult closeWarning(Long resultId, String remark) {
        EvalGraduationRequirementResult result = getActiveById(resultId);
        if (result == null) {
            throw new IllegalArgumentException("Result not found");
        }
        if (result.getWarningFlag() == null || result.getWarningFlag() == 0) {
            throw new IllegalStateException("Warning is already closed");
        }
        result.setWarningFlag(0);
        if (StringUtils.hasText(remark)) {
            result.setRemark(remark.trim());
        }
        EntityAuditSupport.touchUpdate(result);
        updateById(result);
        return result;
    }

    @Override
    @Transactional
    public int notifyWarnings(EvalGraduationWarningNotifyRequest request) {
        if (request == null || request.getResultIds() == null || request.getResultIds().isEmpty()) {
            throw new IllegalArgumentException("Result ids cannot be empty");
        }

        int sentCount = 0;
        for (Long resultId : request.getResultIds()) {
            EvalGraduationRequirementResult result = getActiveById(resultId);
            if (result == null) {
                continue;
            }
            if (result.getWarningFlag() == null || result.getWarningFlag() == 0) {
                continue;
            }

            List<Long> recipientUserIds = resolveRecipientUserIds(result.getProgramVersionId());
            if (recipientUserIds.isEmpty()) {
                continue;
            }

            TrProgramVersion version = trProgramVersionService.getById(result.getProgramVersionId());
            TrGraduationRequirement requirement = trGraduationRequirementService.getById(result.getRequirementId());

            NoticeSendRequest sendRequest = new NoticeSendRequest();
            sendRequest.setNoticeType("ATTAINMENT_WARNING");
            sendRequest.setTitle("Graduation Requirement Warning");
            sendRequest.setContent(buildWarningContent(version, requirement, result));
            sendRequest.setSenderUserId(request.getSenderUserId());
            sendRequest.setBizType("EVAL_GRADUATION_WARNING");
            sendRequest.setBizId(result.getId());
            sendRequest.setChannelType("IN_APP");
            sendRequest.setPriorityLevel(1);
            sendRequest.setRemark(request.getRemark());
            sendRequest.setRecipientUserIds(recipientUserIds);
            sendRequest.setOperatorUserId(request.getOperatorUserId());
            noticeMessageService.sendNotice(sendRequest);
            sentCount++;
        }
        return sentCount;
    }

    private List<EvalGraduationRequirementResult> calculateInternal(Long programVersionId, Long modelId,
                                                                    List<Long> requirementIds, String remark,
                                                                    boolean recalculate) {
        TrProgramVersion programVersion = requireProgramVersion(programVersionId);
        EvalModel model = requireEnabledModel(modelId);
        validateModelScope(programVersionId, modelId, model.getScopeType());

        List<TrGraduationRequirement> requirements = trGraduationRequirementService.list(new LambdaQueryWrapper<TrGraduationRequirement>()
                .eq(TrGraduationRequirement::getProgramVersionId, programVersionId)
                .eq(TrGraduationRequirement::getIsDeleted, 0)
                .in(requirementIds != null && !requirementIds.isEmpty(), TrGraduationRequirement::getId, requirementIds)
                .orderByAsc(TrGraduationRequirement::getSortNo)
                .orderByAsc(TrGraduationRequirement::getId));
        if (requirements.isEmpty()) {
            throw new IllegalStateException("No graduation requirements found for the selected program version");
        }

        List<TeachingTask> tasks = teachingTaskService.list(new LambdaQueryWrapper<TeachingTask>()
                .eq(TeachingTask::getProgramVersionId, programVersionId)
                .eq(TeachingTask::getIsDeleted, 0));
        if (tasks.isEmpty()) {
            throw new IllegalStateException("No teaching tasks found for the selected program version");
        }
        LinkedHashSet<Long> taskIds = new LinkedHashSet<>();
        for (TeachingTask task : tasks) {
            taskIds.add(task.getId());
        }

        List<EvalCourseTargetResult> targetResults = pickLatestTargetResults(
                evalCourseTargetResultService.list(new LambdaQueryWrapper<EvalCourseTargetResult>()
                        .in(EvalCourseTargetResult::getTaskId, taskIds)
                        .eq(EvalCourseTargetResult::getIsDeleted, 0)
                        .orderByDesc(EvalCourseTargetResult::getCalcTime)
                        .orderByDesc(EvalCourseTargetResult::getId)));
        if (targetResults.isEmpty()) {
            throw new IllegalStateException("No course target results found. Please calculate course target results first");
        }

        Map<Long, List<EvalCourseTargetResult>> targetResultsByObjective = new HashMap<>();
        for (EvalCourseTargetResult targetResult : targetResults) {
            targetResultsByObjective.computeIfAbsent(targetResult.getObjectiveId(), key -> new ArrayList<>()).add(targetResult);
        }

        List<TrRequirementIndicatorSupport> requirementSupports = trRequirementIndicatorSupportService.list(
                new LambdaQueryWrapper<TrRequirementIndicatorSupport>()
                        .eq(TrRequirementIndicatorSupport::getIsDeleted, 0)
                        .in(TrRequirementIndicatorSupport::getGraduationRequirementId,
                                requirements.stream().map(TrGraduationRequirement::getId).toList()));
        if (requirementSupports.isEmpty()) {
            throw new IllegalStateException("No requirement-indicator supports found for the selected program version");
        }

        LinkedHashSet<Long> indicatorPointIds = new LinkedHashSet<>();
        for (TrRequirementIndicatorSupport support : requirementSupports) {
            indicatorPointIds.add(support.getIndicatorPointId());
        }
        List<EduCourseObjectiveIndicatorPoint> objectiveIndicatorPoints = eduCourseObjectiveIndicatorPointService.list(
                new LambdaQueryWrapper<EduCourseObjectiveIndicatorPoint>()
                        .eq(EduCourseObjectiveIndicatorPoint::getIsDeleted, 0)
                        .in(EduCourseObjectiveIndicatorPoint::getIndicatorPointId, indicatorPointIds));
        if (objectiveIndicatorPoints.isEmpty()) {
            throw new IllegalStateException("No course objective mappings found for the linked indicator points");
        }

        Map<Long, List<EduCourseObjectiveIndicatorPoint>> objectiveMappingsByIndicator = new HashMap<>();
        for (EduCourseObjectiveIndicatorPoint mapping : objectiveIndicatorPoints) {
            objectiveMappingsByIndicator.computeIfAbsent(mapping.getIndicatorPointId(), key -> new ArrayList<>()).add(mapping);
        }

        Map<Long, List<TrRequirementIndicatorSupport>> supportsByRequirement = new LinkedHashMap<>();
        for (TrRequirementIndicatorSupport support : requirementSupports) {
            supportsByRequirement.computeIfAbsent(support.getGraduationRequirementId(), key -> new ArrayList<>()).add(support);
        }

        List<EvalGraduationRequirementResult> results = new ArrayList<>();
        for (TrGraduationRequirement requirement : requirements) {
            List<TrRequirementIndicatorSupport> supports = supportsByRequirement.get(requirement.getId());
            if (supports == null || supports.isEmpty()) {
                throw new IllegalStateException("No indicator supports found for requirement: " + requirement.getId());
            }
            results.add(calculateOneRequirement(programVersion, model, requirement, supports, objectiveMappingsByIndicator,
                    targetResultsByObjective, remark, recalculate));
        }
        return results;
    }

    private EvalGraduationRequirementResult calculateOneRequirement(TrProgramVersion programVersion, EvalModel model,
                                                                    TrGraduationRequirement requirement,
                                                                    List<TrRequirementIndicatorSupport> supports,
                                                                    Map<Long, List<EduCourseObjectiveIndicatorPoint>> mappingsByIndicator,
                                                                    Map<Long, List<EvalCourseTargetResult>> targetResultsByObjective,
                                                                    String remark, boolean recalculate) {
        BigDecimal totalCombinedWeight = BigDecimal.ZERO;
        BigDecimal weightedValueSum = BigDecimal.ZERO;
        List<ContributionBuffer> buffers = new ArrayList<>();

        for (TrRequirementIndicatorSupport support : supports) {
            BigDecimal requirementWeight = normalizeWeight(support.getSupportWeight());
            if (requirementWeight.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            List<EduCourseObjectiveIndicatorPoint> mappings = mappingsByIndicator.get(support.getIndicatorPointId());
            if (mappings == null || mappings.isEmpty()) {
                continue;
            }
            for (EduCourseObjectiveIndicatorPoint mapping : mappings) {
                BigDecimal objectiveWeight = normalizeWeight(mapping.getSupportWeight());
                if (objectiveWeight.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                List<EvalCourseTargetResult> sourceResults = targetResultsByObjective.get(mapping.getCourseObjectiveId());
                if (sourceResults == null || sourceResults.isEmpty()) {
                    continue;
                }
                for (EvalCourseTargetResult sourceResult : sourceResults) {
                    BigDecimal combinedWeight = requirementWeight.multiply(objectiveWeight)
                            .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP);
                    if (combinedWeight.compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }
                    weightedValueSum = weightedValueSum.add(sourceResult.getAttainmentValue().multiply(combinedWeight));
                    totalCombinedWeight = totalCombinedWeight.add(combinedWeight);

                    ContributionBuffer buffer = new ContributionBuffer();
                    buffer.sourceResultId = sourceResult.getId();
                    buffer.sourceValue = sourceResult.getAttainmentValue();
                    buffer.combinedWeight = combinedWeight;
                    buffer.remark = buildContributionRemark(support, mapping);
                    buffers.add(buffer);
                }
            }
        }

        if (buffers.isEmpty() || totalCombinedWeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("No course target results can be aggregated for requirement: " + requirement.getId());
        }

        BigDecimal attainmentValue = scale(weightedValueSum.divide(totalCombinedWeight, 4, RoundingMode.HALF_UP));
        BigDecimal thresholdValue = model.getThresholdValue() == null || model.getThresholdValue().compareTo(BigDecimal.ZERO) <= 0
                ? DEFAULT_THRESHOLD
                : scale(model.getThresholdValue());
        BigDecimal attainmentRate = scale(attainmentValue.multiply(ONE_HUNDRED).divide(thresholdValue, 4, RoundingMode.HALF_UP));
        int warningFlag = attainmentValue.compareTo(thresholdValue) < 0 ? 1 : 0;

        EvalGraduationRequirementResult result = baseMapper.selectByUnique(programVersion.getId(), requirement.getId(), model.getId());
        boolean isNewResult = result == null;
        if (isNewResult) {
            result = new EvalGraduationRequirementResult();
            result.setProgramVersionId(programVersion.getId());
            result.setRequirementId(requirement.getId());
            result.setModelId(model.getId());
            result.setLockFlag(0);
            EntityAuditSupport.touchCreate(result);
        } else {
            if (result.getLockFlag() != null && result.getLockFlag() == 1) {
                throw new IllegalStateException("Result has been confirmed and cannot be recalculated");
            }
            EntityAuditSupport.touchUpdate(result);
        }

        result.setAttainmentValue(attainmentValue);
        result.setThresholdValue(thresholdValue);
        result.setAttainmentRate(attainmentRate);
        result.setWarningFlag(warningFlag);
        result.setCalcTime(LocalDateTime.now());
        result.setRemark(StringUtils.hasText(remark) ? remark : (recalculate ? "Recalculated" : "Calculated"));
        if (isNewResult) {
            save(result);
        } else {
            updateById(result);
        }

        List<EvalResultDetail> details = new ArrayList<>(buffers.size());
        for (ContributionBuffer buffer : buffers) {
            EvalResultDetail detail = new EvalResultDetail();
            detail.setSourceType("COURSE_TARGET_RESULT");
            detail.setSourceId(buffer.sourceResultId);
            detail.setSourceValue(scale(buffer.sourceValue));
            BigDecimal normalizedWeight = scale(buffer.combinedWeight.multiply(ONE_HUNDRED)
                    .divide(totalCombinedWeight, 4, RoundingMode.HALF_UP));
            detail.setWeightPercent(normalizedWeight);
            detail.setContributionValue(scale(buffer.sourceValue.multiply(normalizedWeight)
                    .divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP)));
            detail.setRemark(buffer.remark);
            details.add(detail);
        }
        evalResultDetailService.replaceDetails(RESULT_TYPE_GRADUATION_REQUIREMENT, result.getId(), details);
        return result;
    }

    private TrProgramVersion requireProgramVersion(Long programVersionId) {
        TrProgramVersion version = trProgramVersionService.getById(programVersionId);
        if (version == null || (version.getIsDeleted() != null && version.getIsDeleted() != 0)) {
            throw new IllegalArgumentException("Program version not found");
        }
        return version;
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

    private void validateModelScope(Long programVersionId, Long modelId, String scopeType) {
        if (!"PROGRAM_VERSION".equalsIgnoreCase(scopeType)) {
            return;
        }
        List<EvalModelScope> scopes = evalModelScopeService.listActiveByModelId(modelId);
        if (scopes.isEmpty()) {
            throw new IllegalStateException("Model scope is not configured");
        }
        for (EvalModelScope scope : scopes) {
            if (programVersionId.equals(scope.getScopeId())) {
                return;
            }
        }
        throw new IllegalStateException("Selected model is not applicable to the current program version");
    }

    private BigDecimal normalizeWeight(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 同一授课任务下的同一课程目标可能已经按多个课程目标模型计算过结果。
     * 毕业要求汇总不能把这些不同模型的结果全部累加，否则同一目标会被重复计权。
     * 这里按 calcTime/id 取最新的一条，保证每个 task + objective 只参与一次汇总。
     */
    private List<EvalCourseTargetResult> pickLatestTargetResults(List<EvalCourseTargetResult> sourceResults) {
        if (sourceResults == null || sourceResults.isEmpty()) {
            return List.of();
        }
        Map<String, EvalCourseTargetResult> latestByTaskObjective = new LinkedHashMap<>();
        for (EvalCourseTargetResult sourceResult : sourceResults) {
            if (sourceResult == null || sourceResult.getTaskId() == null || sourceResult.getObjectiveId() == null) {
                continue;
            }
            String key = sourceResult.getTaskId() + ":" + sourceResult.getObjectiveId();
            latestByTaskObjective.putIfAbsent(key, sourceResult);
        }
        return new ArrayList<>(latestByTaskObjective.values());
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String buildContributionRemark(TrRequirementIndicatorSupport support,
                                           EduCourseObjectiveIndicatorPoint mapping) {
        return "indicatorWeight=" + scale(normalizeWeight(support.getSupportWeight()))
                + ", objectiveWeight=" + scale(normalizeWeight(mapping.getSupportWeight()));
    }

    private List<Long> resolveRecipientUserIds(Long programVersionId) {
        List<TeachingTask> tasks = teachingTaskService.list(new LambdaQueryWrapper<TeachingTask>()
                .eq(TeachingTask::getProgramVersionId, programVersionId)
                .eq(TeachingTask::getIsDeleted, 0));
        LinkedHashSet<Long> teacherIds = new LinkedHashSet<>();
        for (TeachingTask task : tasks) {
            if (task.getTeacherId() != null) {
                teacherIds.add(task.getTeacherId());
            }
        }
        if (teacherIds.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        for (EduTeacher teacher : eduTeacherService.listByIds(teacherIds)) {
            if (teacher != null && (teacher.getIsDeleted() == null || teacher.getIsDeleted() == 0)
                    && teacher.getUserId() != null) {
                userIds.add(teacher.getUserId());
            }
        }
        return new ArrayList<>(userIds);
    }

    private String buildWarningContent(TrProgramVersion version, TrGraduationRequirement requirement,
                                       EvalGraduationRequirementResult result) {
        String versionName = version == null
                ? String.valueOf(result.getProgramVersionId())
                : StringUtils.hasText(version.getVersionName()) ? version.getVersionName() : version.getVersionNo();
        String requirementName = requirement == null
                ? String.valueOf(result.getRequirementId())
                : StringUtils.hasText(requirement.getRequirementName()) ? requirement.getRequirementName() : requirement.getRequirementCode();
        return "Program version [" + versionName + "] requirement [" + requirementName + "] is below threshold. "
                + "Current value: " + result.getAttainmentValue() + ", threshold: " + result.getThresholdValue() + ".";
    }

    private List<EvalGraduationRequirementContributionVO> buildContributionVOs(List<EvalResultDetail> details) {
        List<EvalGraduationRequirementContributionVO> result = new ArrayList<>(details.size());
        for (EvalResultDetail detail : details) {
            EvalGraduationRequirementContributionVO vo = new EvalGraduationRequirementContributionVO();
            vo.setDetailId(detail.getId());
            vo.setSourceType(detail.getSourceType());
            vo.setSourceId(detail.getSourceId());
            vo.setWeightPercent(detail.getWeightPercent());
            vo.setSourceValue(detail.getSourceValue());
            vo.setContributionValue(detail.getContributionValue());
            vo.setRemark(detail.getRemark());
            if ("COURSE_TARGET_RESULT".equals(detail.getSourceType()) && detail.getSourceId() != null) {
                EvalCourseTargetResultDetailVO sourceDetail = evalCourseTargetResultService.getDetail(detail.getSourceId());
                if (sourceDetail != null) {
                    vo.setSourceName(sourceDetail.getCourseName() + " / " + sourceDetail.getObjectiveName());
                }
            }
            result.add(vo);
        }
        return result;
    }

    private static final class ContributionBuffer {
        private Long sourceResultId;
        private BigDecimal sourceValue;
        private BigDecimal combinedWeight;
        private String remark;
    }
}
