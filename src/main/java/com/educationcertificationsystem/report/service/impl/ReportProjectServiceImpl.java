package com.educationcertificationsystem.report.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.course.service.EduCourseService;
import com.educationcertificationsystem.course.service.EduSemesterService;
import com.educationcertificationsystem.model.dto.notice.NoticeSendRequest;
import com.educationcertificationsystem.model.dto.report.ReportChapterLockRequest;
import com.educationcertificationsystem.model.dto.report.ReportChapterSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportDraftSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportDraftUploadRequest;
import com.educationcertificationsystem.model.dto.report.ReportInitialDraftRequest;
import com.educationcertificationsystem.model.dto.report.ReportProgressSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportProjectSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportTaskAssignmentSaveRequest;
import com.educationcertificationsystem.model.entity.EduCourse;
import com.educationcertificationsystem.model.entity.EduSemester;
import com.educationcertificationsystem.model.entity.ReportChapter;
import com.educationcertificationsystem.model.entity.ReportDraft;
import com.educationcertificationsystem.model.entity.ReportProgressLog;
import com.educationcertificationsystem.model.entity.ReportProject;
import com.educationcertificationsystem.model.entity.ReportTaskAssignment;
import com.educationcertificationsystem.model.entity.SurveyQuestionnaire;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.entity.TrGraduationRequirement;
import com.educationcertificationsystem.model.entity.TrProgramTarget;
import com.educationcertificationsystem.model.vo.report.ReportChapterNodeVO;
import com.educationcertificationsystem.model.vo.report.ReportDraftVO;
import com.educationcertificationsystem.model.vo.report.ReportProgressBoardVO;
import com.educationcertificationsystem.model.vo.report.ReportProgressLogVO;
import com.educationcertificationsystem.model.vo.report.ReportProjectDetailVO;
import com.educationcertificationsystem.model.vo.report.ReportProjectPageVO;
import com.educationcertificationsystem.model.vo.report.ReportTaskAssignmentVO;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.program.service.TrGraduationRequirementService;
import com.educationcertificationsystem.program.service.TrProgramTargetService;
import com.educationcertificationsystem.report.mapper.ReportProjectMapper;
import com.educationcertificationsystem.report.service.ReportChapterService;
import com.educationcertificationsystem.report.service.ReportDraftService;
import com.educationcertificationsystem.report.service.ReportProgressLogService;
import com.educationcertificationsystem.report.service.ReportProjectService;
import com.educationcertificationsystem.report.service.ReportTaskAssignmentService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireService;
import com.educationcertificationsystem.user.service.SysUserService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ReportProjectServiceImpl extends ServiceImpl<ReportProjectMapper, ReportProject>
        implements ReportProjectService {

    private static final String PROJECT_DRAFT = "DRAFT";
    private static final String PROJECT_IN_PROGRESS = "IN_PROGRESS";
    private static final String PROJECT_COMPLETED = "COMPLETED";

    private static final String CHAPTER_TODO = "TODO";
    private static final String CHAPTER_IN_PROGRESS = "IN_PROGRESS";
    private static final String CHAPTER_COMPLETED = "COMPLETED";

    private static final String ASSIGNMENT_PENDING = "PENDING";
    private static final String ASSIGNMENT_IN_PROGRESS = "IN_PROGRESS";
    private static final String ASSIGNMENT_COMPLETED = "COMPLETED";

    private static final Set<String> TEXT_EXTENSIONS = Set.of("txt", "md", "csv");

    private final ReportChapterService reportChapterService;
    private final ReportTaskAssignmentService reportTaskAssignmentService;
    private final ReportDraftService reportDraftService;
    private final ReportProgressLogService reportProgressLogService;
    private final SysUserService sysUserService;
    private final EduSemesterService eduSemesterService;
    private final EduCourseService eduCourseService;
    private final TrProgramTargetService trProgramTargetService;
    private final TrGraduationRequirementService trGraduationRequirementService;
    private final SurveyQuestionnaireService surveyQuestionnaireService;
    private final NoticeMessageService noticeMessageService;

    @Override
    public Page<ReportProjectPageVO> pageByCondition(long pageNum,
                                                     long pageSize,
                                                     String status,
                                                     Long ownerUserId,
                                                     Long viewerUserId,
                                                     String keyword) {
        long current = Math.max(pageNum, 1L);
        long size = Math.max(pageSize, 1L);
        long offset = (current - 1L) * size;
        long total = baseMapper.countByCondition(
                normalizeEnum(status),
                ownerUserId,
                viewerUserId,
                normalizeText(keyword));
        Page<ReportProjectPageVO> page = new Page<>(current, size);
        page.setTotal(total);
        if (total == 0) {
            page.setRecords(List.of());
            return page;
        }
        List<ReportProject> projects = baseMapper.selectPageByCondition(
                offset,
                size,
                normalizeEnum(status),
                ownerUserId,
                viewerUserId,
                normalizeText(keyword));
        page.setRecords(projects.stream().map(project -> toPageVO(project, viewerUserId)).toList());
        return page;
    }

    @Override
    public ReportProjectDetailVO getDetail(Long projectId, Long viewerUserId) {
        return buildDetail(getRequiredProject(projectId), viewerUserId);
    }

    @Override
    @Transactional
    public ReportProjectDetailVO createProject(ReportProjectSaveRequest request) {
        validateProjectRequest(request, null);
        ReportProject project = new ReportProject();
        applyProjectFields(project, request);
        project.setStatus(PROJECT_DRAFT);
        project.setTotalChapters(0);
        project.setLockedFlag(0);
        project.setExportedAt(null);
        EntityAuditSupport.touchCreate(project);
        save(project);
        if (request.getChapters() != null && !request.getChapters().isEmpty()) {
            saveChapterTree(project.getId(), request.getChapters());
        }
        return buildDetail(getRequiredProject(project.getId()), null);
    }

    @Override
    @Transactional
    public ReportProjectDetailVO updateProject(Long projectId, ReportProjectSaveRequest request) {
        ReportProject project = getRequiredProject(projectId);
        validateProjectRequest(request, projectId);
        applyProjectFields(project, request);
        EntityAuditSupport.touchUpdate(project);
        updateById(project);
        if (request.getChapters() != null) {
            saveChapterTree(projectId, request.getChapters());
        }
        return buildDetail(getRequiredProject(projectId), null);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId) {
        ReportProject project = getRequiredProject(projectId);
        EntityAuditSupport.touchDelete(project);
        updateById(project);

        List<ReportChapter> chapters = listActiveChapters(projectId);
        List<Long> chapterIds = chapters.stream().map(ReportChapter::getId).toList();
        List<ReportTaskAssignment> assignments = listActiveAssignmentsByProject(projectId);
        List<ReportDraft> drafts = listActiveDrafts(chapterIds);
        List<ReportProgressLog> logs = listActiveLogs(projectId);

        assignments.forEach(EntityAuditSupport::touchDelete);
        drafts.forEach(EntityAuditSupport::touchDelete);
        logs.forEach(EntityAuditSupport::touchDelete);
        chapters.forEach(EntityAuditSupport::touchDelete);

        if (!assignments.isEmpty()) {
            reportTaskAssignmentService.updateBatchById(assignments);
        }
        if (!drafts.isEmpty()) {
            reportDraftService.updateBatchById(drafts);
        }
        if (!logs.isEmpty()) {
            reportProgressLogService.updateBatchById(logs);
        }
        if (!chapters.isEmpty()) {
            reportChapterService.updateBatchById(chapters);
        }
    }

    @Override
    @Transactional
    public ReportProjectDetailVO saveChapterTree(Long projectId, List<ReportChapterSaveRequest> requests) {
        getRequiredProject(projectId);
        List<ReportChapter> existing = listActiveChapters(projectId);
        Map<Long, ReportChapter> existingMap = existing.stream()
                .collect(Collectors.toMap(ReportChapter::getId, chapter -> chapter, (left, right) -> left, LinkedHashMap::new));
        validateChapterRequests(requests, existingMap, new HashSet<>(), new HashSet<>());
        LinkedHashSet<Long> retainedIds = new LinkedHashSet<>();
        int sortNo = 1;
        if (requests != null) {
            for (ReportChapterSaveRequest request : requests) {
                sortNo = upsertChapterNode(projectId, null, request, sortNo, existingMap, retainedIds);
            }
        }

        List<ReportChapter> removable = existing.stream()
                .filter(chapter -> !retainedIds.contains(chapter.getId()))
                .toList();
        if (!removable.isEmpty()) {
            ensureChaptersRemovable(removable.stream().map(ReportChapter::getId).toList());
            removable.forEach(EntityAuditSupport::touchDelete);
            reportChapterService.updateBatchById(removable);
        }

        syncProjectSummary(projectId);
        return buildDetail(getRequiredProject(projectId), null);
    }

    @Override
    @Transactional
    public List<ReportTaskAssignment> saveAssignments(Long projectId, List<ReportTaskAssignmentSaveRequest> requests) {
        ReportProject project = getRequiredProject(projectId);
        Map<Long, ReportChapter> chapterMap = listActiveChapters(projectId).stream()
                .collect(Collectors.toMap(ReportChapter::getId, chapter -> chapter, (left, right) -> left, LinkedHashMap::new));
        validateAssignmentRequests(requests, chapterMap);

        List<ReportTaskAssignment> existing = listActiveAssignmentsByProject(projectId);
        Map<String, ReportTaskAssignment> existingMap = existing.stream()
                .collect(Collectors.toMap(this::assignmentKey, assignment -> assignment, (left, right) -> left, LinkedHashMap::new));
        LinkedHashSet<String> retainedKeys = new LinkedHashSet<>();
        List<ReportTaskAssignment> saved = new ArrayList<>();

        if (requests != null) {
            for (ReportTaskAssignmentSaveRequest request : requests) {
                String key = assignmentKey(request.getChapterId(), request.getAssigneeUserId());
                retainedKeys.add(key);
                ReportTaskAssignment assignment = existingMap.get(key);
                boolean created = assignment == null;
                if (created) {
                    assignment = new ReportTaskAssignment();
                    assignment.setProjectId(projectId);
                    assignment.setChapterId(request.getChapterId());
                    assignment.setAssigneeUserId(request.getAssigneeUserId());
                    EntityAuditSupport.touchCreate(assignment);
                } else {
                    EntityAuditSupport.touchUpdate(assignment);
                }
                assignment.setRoleType(request.getRoleType().trim());
                assignment.setDueDate(request.getDueDate());
                assignment.setAssignmentStatus(normalizeAssignmentStatus(request.getAssignmentStatus()));
                assignment.setCompletedAt(ASSIGNMENT_COMPLETED.equals(assignment.getAssignmentStatus()) ? LocalDateTime.now() : null);
                assignment.setRemark(normalizeText(request.getRemark()));
                if (created) {
                    reportTaskAssignmentService.save(assignment);
                } else {
                    reportTaskAssignmentService.updateById(assignment);
                }
                saved.add(assignment);
            }
        }

        List<ReportTaskAssignment> removable = existing.stream()
                .filter(assignment -> !retainedKeys.contains(assignmentKey(assignment)))
                .toList();
        if (!removable.isEmpty()) {
            removable.forEach(EntityAuditSupport::touchDelete);
            reportTaskAssignmentService.updateBatchById(removable);
        }

        sendAssignmentNotice(project, saved, chapterMap);
        syncProjectSummary(projectId);
        return saved;
    }

    @Override
    public List<ReportDraftVO> listDrafts(Long chapterId) {
        ReportChapter chapter = getRequiredChapter(chapterId);
        List<ReportDraft> drafts = reportDraftService.list(new LambdaQueryWrapper<ReportDraft>()
                .eq(ReportDraft::getChapterId, chapterId)
                .eq(ReportDraft::getIsDeleted, 0)
                .orderByDesc(ReportDraft::getVersionNo)
                .orderByDesc(ReportDraft::getId));
        Map<Long, SysUser> userMap = buildUserMap(chapter.getProjectId(), List.of(), List.of(), drafts, List.of());
        return drafts.stream().map(draft -> toDraftVO(draft, userMap)).toList();
    }

    @Override
    @Transactional
    public ReportDraft saveDraft(Long chapterId, ReportDraftSaveRequest request) {
        ReportChapter chapter = getRequiredChapter(chapterId);
        validateDraftRequest(request, chapter);
        ReportProject project = getRequiredProject(chapter.getProjectId());

        ReportDraft draft = new ReportDraft();
        draft.setChapterId(chapterId);
        draft.setVersionNo(nextDraftVersion(chapterId));
        draft.setDraftContent(request.getDraftContent().trim());
        draft.setEditedBy(request.getEditedBy());
        draft.setEditedAt(LocalDateTime.now());
        draft.setLockFlag(chapter.getLockedFlag() == null ? 0 : chapter.getLockedFlag());
        draft.setRemark(normalizeText(request.getRemark()));
        EntityAuditSupport.touchCreate(draft);
        reportDraftService.save(draft);

        chapter.setContentText(draft.getDraftContent());
        chapter.setChapterStatus(resolveDraftChapterStatus(request.getChapterStatus(), request.getProgressPercent()));
        EntityAuditSupport.touchUpdate(chapter);
        reportChapterService.updateById(chapter);

        BigDecimal effectiveProgress = resolveEffectiveProgress(request.getProgressPercent(), chapter.getChapterStatus());
        if (effectiveProgress != null || StringUtils.hasText(request.getComment())) {
            createProgressLog(project.getId(), chapter.getId(), request.getEditedBy(), effectiveProgress, request.getComment(), request.getRemark());
            syncAssignmentsByProgress(chapter.getId(), request.getEditedBy(), effectiveProgress, chapter.getChapterStatus());
        }

        syncProjectSummary(project.getId());
        return draft;
    }

    @Override
    @Transactional
    public ReportDraft uploadDraft(Long chapterId, ReportDraftUploadRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Draft file is required");
        }
        String extension = fileExtension(file.getOriginalFilename());
        if (!TEXT_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only txt, md and csv draft files are supported");
        }
        String content;
        try {
            content = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new IllegalStateException("Read draft file failed");
        }
        ReportDraftSaveRequest draftRequest = new ReportDraftSaveRequest();
        draftRequest.setEditedBy(request == null ? null : request.getEditedBy());
        draftRequest.setDraftContent(content);
        draftRequest.setChapterStatus(request == null ? null : request.getChapterStatus());
        draftRequest.setProgressPercent(request == null ? null : request.getProgressPercent());
        draftRequest.setComment(request == null ? null : request.getComment());
        draftRequest.setRemark(request == null ? null : request.getRemark());
        return saveDraft(chapterId, draftRequest);
    }

    @Override
    @Transactional
    public ReportChapter lockChapter(Long chapterId, ReportChapterLockRequest request) {
        ReportChapter chapter = getRequiredChapter(chapterId);
        int lockedFlag = request == null || request.getLockedFlag() == null ? 1 : request.getLockedFlag();
        if (lockedFlag != 0 && lockedFlag != 1) {
            throw new IllegalArgumentException("Locked flag must be 0 or 1");
        }
        chapter.setLockedFlag(lockedFlag);
        if (request != null && StringUtils.hasText(request.getRemark())) {
            chapter.setRemark(request.getRemark().trim());
        }
        EntityAuditSupport.touchUpdate(chapter);
        reportChapterService.updateById(chapter);

        List<ReportDraft> drafts = reportDraftService.list(new LambdaQueryWrapper<ReportDraft>()
                .eq(ReportDraft::getChapterId, chapterId)
                .eq(ReportDraft::getIsDeleted, 0));
        if (!drafts.isEmpty()) {
            drafts.forEach(draft -> {
                draft.setLockFlag(lockedFlag);
                EntityAuditSupport.touchUpdate(draft);
            });
            reportDraftService.updateBatchById(drafts);
        }

        syncProjectSummary(chapter.getProjectId());
        return chapter;
    }

    @Override
    @Transactional
    public ReportProgressLog saveProgress(Long projectId, ReportProgressSaveRequest request) {
        getRequiredProject(projectId);
        validateProgressRequest(request, projectId);
        ReportProgressLog log = createProgressLog(
                projectId,
                request.getChapterId(),
                request.getUserId(),
                request.getProgressPercent(),
                request.getComment(),
                request.getRemark());
        if (request.getChapterId() != null) {
            ReportChapter chapter = getRequiredChapter(request.getChapterId());
            chapter.setChapterStatus(resolveStatusFromProgress(request.getProgressPercent()));
            EntityAuditSupport.touchUpdate(chapter);
            reportChapterService.updateById(chapter);
            syncAssignmentsByProgress(chapter.getId(), request.getUserId(), request.getProgressPercent(), chapter.getChapterStatus());
        }
        syncProjectSummary(projectId);
        return log;
    }

    @Override
    public ReportProgressBoardVO getProgressBoard(Long projectId) {
        ReportProject project = getRequiredProject(projectId);
        List<ReportChapter> chapters = listActiveChapters(projectId);
        List<ReportTaskAssignment> assignments = listActiveAssignmentsByProject(projectId);
        List<ReportProgressLog> logs = listActiveLogs(projectId);
        Set<Long> allChapterIds = chapters.stream().map(ReportChapter::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        return buildProgressBoard(project, chapters, assignments, logs, allChapterIds, allChapterIds);
    }

    @Override
    @Transactional
    public ReportProjectDetailVO generateInitialDrafts(Long projectId, ReportInitialDraftRequest request) {
        getRequiredProject(projectId);
        if (request == null || request.getEditedBy() == null || !isActiveUserId(request.getEditedBy())) {
            throw new IllegalArgumentException("Edited user is invalid");
        }
        boolean overwriteEmptyOnly = request.getOverwriteEmptyOnly() == null || request.getOverwriteEmptyOnly() != 0;
        Set<Long> targetIds = request.getChapterIds() == null ? null : new LinkedHashSet<>(request.getChapterIds());
        List<ReportChapter> chapters = listActiveChapters(projectId);
        for (ReportChapter chapter : chapters) {
            if (targetIds != null && !targetIds.contains(chapter.getId())) {
                continue;
            }
            if (chapter.getLockedFlag() != null && chapter.getLockedFlag() == 1) {
                continue;
            }
            if (overwriteEmptyOnly && StringUtils.hasText(chapter.getContentText())) {
                continue;
            }
            String content = buildInitialDraftContent(chapter);
            if (!StringUtils.hasText(content)) {
                continue;
            }
            ReportDraft draft = new ReportDraft();
            draft.setChapterId(chapter.getId());
            draft.setVersionNo(nextDraftVersion(chapter.getId()));
            draft.setDraftContent(content);
            draft.setEditedBy(request.getEditedBy());
            draft.setEditedAt(LocalDateTime.now());
            draft.setLockFlag(chapter.getLockedFlag() == null ? 0 : chapter.getLockedFlag());
            draft.setRemark(normalizeText(request.getRemark()));
            EntityAuditSupport.touchCreate(draft);
            reportDraftService.save(draft);

            chapter.setContentText(content);
            if (!CHAPTER_COMPLETED.equals(chapter.getChapterStatus())) {
                chapter.setChapterStatus(CHAPTER_IN_PROGRESS);
            }
            EntityAuditSupport.touchUpdate(chapter);
            reportChapterService.updateById(chapter);
        }
        syncProjectSummary(projectId);
        return buildDetail(getRequiredProject(projectId), null);
    }

    @Override
    @Transactional
    public String buildMergedReport(Long projectId) {
        ReportProject project = getRequiredProject(projectId);
        List<ReportChapter> chapters = listActiveChapters(projectId);
        Map<Long, List<ReportChapter>> childrenMap = chapters.stream()
                .collect(Collectors.groupingBy(
                        chapter -> chapter.getParentId() == null ? 0L : chapter.getParentId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        childrenMap.values().forEach(list -> list.sort(this::compareChapter));

        StringBuilder builder = new StringBuilder();
        builder.append("# ").append(project.getProjectName()).append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("- Report Code: ").append(project.getReportCode()).append(System.lineSeparator());
        builder.append("- Academic Year: ").append(project.getAcademicYear()).append(System.lineSeparator());
        builder.append("- Generation Mode: ").append(project.getGenerationMode()).append(System.lineSeparator()).append(System.lineSeparator());
        appendChapterMarkdown(builder, childrenMap, 0L, 2);

        project.setExportedAt(LocalDateTime.now());
        EntityAuditSupport.touchUpdate(project);
        updateById(project);
        return builder.toString();
    }

    private ReportProjectPageVO toPageVO(ReportProject project, Long viewerUserId) {
        ReportProjectPageVO vo = BeanUtil.copyProperties(project, ReportProjectPageVO.class);
        EduSemester semester = project.getSemesterId() == null ? null : eduSemesterService.getById(project.getSemesterId());
        SysUser owner = project.getOwnerUserId() == null ? null : sysUserService.getById(project.getOwnerUserId());
        List<ReportChapter> chapters = listActiveChapters(project.getId());
        List<ReportTaskAssignment> assignments = listActiveAssignmentsByProject(project.getId());
        List<ReportProgressLog> logs = listActiveLogs(project.getId());
        Set<Long> responsibleIds = resolveResponsibleChapterIds(project, chapters, assignments, viewerUserId);
        ReportProgressBoardVO board = buildProgressBoard(project, chapters, assignments, logs, responsibleIds, responsibleIds);
        vo.setSemesterName(semester == null ? null : semester.getSemesterName());
        vo.setOwnerUserName(owner == null ? null : owner.getRealName());
        vo.setVisibleChapterCount(board.getVisibleChapterCount());
        vo.setCompletedChapterCount(board.getCompletedChapterCount());
        vo.setProgressPercent(board.getProgressPercent());
        return vo;
    }

    private ReportProjectDetailVO buildDetail(ReportProject project, Long viewerUserId) {
        List<ReportChapter> chapters = listActiveChapters(project.getId());
        List<ReportTaskAssignment> assignments = listActiveAssignmentsByProject(project.getId());
        List<ReportProgressLog> logs = listActiveLogs(project.getId());
        List<ReportDraft> drafts = listActiveDrafts(chapters.stream().map(ReportChapter::getId).toList());
        Map<Long, SysUser> userMap = buildUserMap(project.getId(), chapters, assignments, drafts, logs);

        Set<Long> responsibleIds = resolveResponsibleChapterIds(project, chapters, assignments, viewerUserId);
        boolean viewAll = viewerUserId == null || Objects.equals(project.getOwnerUserId(), viewerUserId);
        Set<Long> displayIds = resolveDisplayChapterIds(chapters, responsibleIds, viewAll);
        Map<Long, BigDecimal> progressMap = buildLatestChapterProgressMap(chapters, assignments, logs);

        Map<Long, List<ReportTaskAssignmentVO>> assignmentMap = assignments.stream()
                .collect(Collectors.groupingBy(
                        ReportTaskAssignment::getChapterId,
                        LinkedHashMap::new,
                        Collectors.mapping(assignment -> toAssignmentVO(assignment, userMap), Collectors.toList())));
        assignmentMap.values().forEach(list -> list.sort(
                Comparator.comparing(ReportTaskAssignmentVO::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(ReportTaskAssignmentVO::getId)));

        Map<Long, List<ReportDraftVO>> draftMap = drafts.stream()
                .collect(Collectors.groupingBy(
                        ReportDraft::getChapterId,
                        LinkedHashMap::new,
                        Collectors.mapping(draft -> toDraftVO(draft, userMap), Collectors.toList())));
        draftMap.values().forEach(list -> list.sort(
                Comparator.comparing(ReportDraftVO::getVersionNo, Comparator.reverseOrder())));

        Map<Long, List<ReportChapter>> childrenMap = chapters.stream()
                .filter(chapter -> displayIds.contains(chapter.getId()))
                .collect(Collectors.groupingBy(
                        chapter -> chapter.getParentId() == null ? 0L : chapter.getParentId(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        childrenMap.values().forEach(list -> list.sort(this::compareChapter));

        ReportProjectDetailVO detail = BeanUtil.copyProperties(project, ReportProjectDetailVO.class);
        EduSemester semester = project.getSemesterId() == null ? null : eduSemesterService.getById(project.getSemesterId());
        SysUser owner = project.getOwnerUserId() == null ? null : sysUserService.getById(project.getOwnerUserId());
        ReportProgressBoardVO board = buildProgressBoard(project, chapters, assignments, logs, responsibleIds, displayIds);

        detail.setSemesterName(semester == null ? null : semester.getSemesterName());
        detail.setOwnerUserName(owner == null ? null : owner.getRealName());
        detail.setVisibleChapterCount(board.getVisibleChapterCount());
        detail.setProgressPercent(board.getProgressPercent());
        detail.setProgressBoard(board);
        detail.setChapters(buildChapterNodes(childrenMap, 0L, responsibleIds, assignmentMap, draftMap, progressMap));
        return detail;
    }

    private List<ReportChapterNodeVO> buildChapterNodes(Map<Long, List<ReportChapter>> childrenMap,
                                                        Long parentId,
                                                        Set<Long> responsibleIds,
                                                        Map<Long, List<ReportTaskAssignmentVO>> assignmentMap,
                                                        Map<Long, List<ReportDraftVO>> draftMap,
                                                        Map<Long, BigDecimal> progressMap) {
        List<ReportChapter> children = childrenMap.getOrDefault(parentId, List.of());
        List<ReportChapterNodeVO> result = new ArrayList<>(children.size());
        for (ReportChapter chapter : children) {
            ReportChapterNodeVO vo = BeanUtil.copyProperties(chapter, ReportChapterNodeVO.class);
            vo.setSourceDisplayName(resolveSourceDisplayName(chapter.getSourceType(), chapter.getSourceRefId()));
            vo.setProgressPercent(progressMap.getOrDefault(chapter.getId(), BigDecimal.ZERO));
            vo.setEditableFlag(responsibleIds.contains(chapter.getId()) ? 1 : 0);
            vo.setAssignments(assignmentMap.getOrDefault(chapter.getId(), List.of()));
            vo.setDrafts(draftMap.getOrDefault(chapter.getId(), List.of()));
            vo.setChildren(buildChapterNodes(childrenMap, chapter.getId(), responsibleIds, assignmentMap, draftMap, progressMap));
            result.add(vo);
        }
        return result;
    }

    private ReportProgressBoardVO buildProgressBoard(ReportProject project,
                                                     List<ReportChapter> chapters,
                                                     List<ReportTaskAssignment> assignments,
                                                     List<ReportProgressLog> logs,
                                                     Set<Long> responsibleIds,
                                                     Set<Long> displayIds) {
        Map<Long, BigDecimal> progressMap = buildLatestChapterProgressMap(chapters, assignments, logs);
        int visibleChapterCount = responsibleIds.isEmpty()
                ? (displayIds.isEmpty() ? 0 : displayIds.size())
                : responsibleIds.size();
        int completedChapterCount = (int) chapters.stream()
                .filter(chapter -> progressMap.getOrDefault(chapter.getId(), BigDecimal.ZERO).compareTo(new BigDecimal("100")) >= 0)
                .count();
        int completedAssignmentCount = (int) assignments.stream()
                .filter(assignment -> ASSIGNMENT_COMPLETED.equals(assignment.getAssignmentStatus()))
                .count();
        int overdueAssignmentCount = (int) assignments.stream()
                .filter(assignment -> assignment.getDueDate() != null
                        && assignment.getDueDate().isBefore(LocalDate.now())
                        && !ASSIGNMENT_COMPLETED.equals(assignment.getAssignmentStatus()))
                .count();
        int lockedChapterCount = (int) chapters.stream()
                .filter(chapter -> chapter.getLockedFlag() != null && chapter.getLockedFlag() == 1)
                .count();

        Map<Long, ReportChapter> chapterMap = chapters.stream()
                .collect(Collectors.toMap(ReportChapter::getId, chapter -> chapter, (left, right) -> left, LinkedHashMap::new));
        Map<Long, SysUser> userMap = buildUserMap(project.getId(), chapters, assignments, List.of(), logs);

        ReportProgressBoardVO board = new ReportProgressBoardVO();
        board.setProjectId(project.getId());
        board.setTotalChapters(chapters.size());
        board.setVisibleChapterCount(visibleChapterCount);
        board.setCompletedChapterCount(completedChapterCount);
        board.setAssignmentCount(assignments.size());
        board.setCompletedAssignmentCount(completedAssignmentCount);
        board.setOverdueAssignmentCount(overdueAssignmentCount);
        board.setLockedChapterCount(lockedChapterCount);
        board.setProgressPercent(calculateProjectProgress(chapters, progressMap));
        board.setLatestLogs(logs.stream()
                .sorted(Comparator.comparing(ReportProgressLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ReportProgressLog::getId, Comparator.reverseOrder()))
                .limit(10)
                .map(log -> toProgressLogVO(log, chapterMap.get(log.getChapterId()), userMap))
                .toList());
        return board;
    }

    private ReportTaskAssignmentVO toAssignmentVO(ReportTaskAssignment assignment, Map<Long, SysUser> userMap) {
        ReportTaskAssignmentVO vo = BeanUtil.copyProperties(assignment, ReportTaskAssignmentVO.class);
        SysUser user = userMap.get(assignment.getAssigneeUserId());
        vo.setAssigneeUserName(user == null ? null : user.getRealName());
        return vo;
    }

    private ReportDraftVO toDraftVO(ReportDraft draft, Map<Long, SysUser> userMap) {
        ReportDraftVO vo = BeanUtil.copyProperties(draft, ReportDraftVO.class);
        SysUser user = userMap.get(draft.getEditedBy());
        vo.setEditedByName(user == null ? null : user.getRealName());
        return vo;
    }

    private ReportProgressLogVO toProgressLogVO(ReportProgressLog log, ReportChapter chapter, Map<Long, SysUser> userMap) {
        ReportProgressLogVO vo = BeanUtil.copyProperties(log, ReportProgressLogVO.class);
        SysUser user = userMap.get(log.getUserId());
        vo.setUserName(user == null ? null : user.getRealName());
        if (chapter != null) {
            vo.setChapterCode(chapter.getChapterCode());
            vo.setChapterTitle(chapter.getChapterTitle());
        }
        return vo;
    }

    private void validateProjectRequest(ReportProjectSaveRequest request, Long currentId) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (!StringUtils.hasText(request.getReportCode())) {
            throw new IllegalArgumentException("Report code is required");
        }
        if (!StringUtils.hasText(request.getProjectName())) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (!StringUtils.hasText(request.getAcademicYear())) {
            throw new IllegalArgumentException("Academic year is required");
        }
        if (request.getSemesterId() == null || !isActiveSemesterId(request.getSemesterId())) {
            throw new IllegalArgumentException("Semester is invalid");
        }
        if (request.getOwnerUserId() == null || !isActiveUserId(request.getOwnerUserId())) {
            throw new IllegalArgumentException("Owner user is invalid");
        }
        boolean duplicated = lambdaQuery()
                .eq(ReportProject::getReportCode, request.getReportCode().trim())
                .eq(ReportProject::getIsDeleted, 0)
                .ne(currentId != null, ReportProject::getId, currentId)
                .exists();
        if (duplicated) {
            throw new IllegalStateException("Report code already exists");
        }
    }

    private void validateChapterRequests(List<ReportChapterSaveRequest> requests,
                                         Map<Long, ReportChapter> existingMap,
                                         Set<Long> visitedIds,
                                         Set<String> codes) {
        if (requests == null) {
            return;
        }
        for (ReportChapterSaveRequest request : requests) {
            if (request == null) {
                throw new IllegalArgumentException("Chapter request cannot be null");
            }
            if (request.getId() != null) {
                if (!existingMap.containsKey(request.getId())) {
                    throw new IllegalArgumentException("Chapter does not belong to project: " + request.getId());
                }
                if (!visitedIds.add(request.getId())) {
                    throw new IllegalArgumentException("Duplicate chapter id: " + request.getId());
                }
            }
            if (!StringUtils.hasText(request.getChapterCode())) {
                throw new IllegalArgumentException("Chapter code is required");
            }
            if (!StringUtils.hasText(request.getChapterTitle())) {
                throw new IllegalArgumentException("Chapter title is required");
            }
            String code = request.getChapterCode().trim().toUpperCase(Locale.ROOT);
            if (!codes.add(code)) {
                throw new IllegalArgumentException("Duplicate chapter code: " + request.getChapterCode());
            }
            validateChapterRequests(request.getChildren(), existingMap, visitedIds, codes);
        }
    }

    private void validateAssignmentRequests(List<ReportTaskAssignmentSaveRequest> requests, Map<Long, ReportChapter> chapterMap) {
        if (requests == null) {
            return;
        }
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (ReportTaskAssignmentSaveRequest request : requests) {
            if (request == null) {
                throw new IllegalArgumentException("Assignment request cannot be null");
            }
            if (request.getChapterId() == null || !chapterMap.containsKey(request.getChapterId())) {
                throw new IllegalArgumentException("Assignment chapter is invalid");
            }
            if (request.getAssigneeUserId() == null || !isActiveUserId(request.getAssigneeUserId())) {
                throw new IllegalArgumentException("Assignment user is invalid");
            }
            if (!StringUtils.hasText(request.getRoleType())) {
                throw new IllegalArgumentException("Assignment role type is required");
            }
            if (request.getDueDate() == null) {
                throw new IllegalArgumentException("Assignment due date is required");
            }
            if (!keys.add(assignmentKey(request.getChapterId(), request.getAssigneeUserId()))) {
                throw new IllegalArgumentException("Duplicate chapter assignment exists");
            }
        }
    }

    private void validateDraftRequest(ReportDraftSaveRequest request, ReportChapter chapter) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (chapter.getLockedFlag() != null && chapter.getLockedFlag() == 1) {
            throw new IllegalStateException("Locked chapter cannot be edited");
        }
        if (request.getEditedBy() == null || !isActiveUserId(request.getEditedBy())) {
            throw new IllegalArgumentException("Editor is invalid");
        }
        if (!StringUtils.hasText(request.getDraftContent())) {
            throw new IllegalArgumentException("Draft content is required");
        }
        if (request.getProgressPercent() != null) {
            validateProgress(request.getProgressPercent());
        }
        if (StringUtils.hasText(request.getChapterStatus())) {
            normalizeChapterStatus(request.getChapterStatus());
        }
    }

    private void validateProgressRequest(ReportProgressSaveRequest request, Long projectId) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUserId() == null || !isActiveUserId(request.getUserId())) {
            throw new IllegalArgumentException("Progress user is invalid");
        }
        if (request.getProgressPercent() == null) {
            throw new IllegalArgumentException("Progress percent is required");
        }
        validateProgress(request.getProgressPercent());
        if (request.getChapterId() != null) {
            ReportChapter chapter = getRequiredChapter(request.getChapterId());
            if (!Objects.equals(chapter.getProjectId(), projectId)) {
                throw new IllegalArgumentException("Progress chapter does not belong to project");
            }
            if (chapter.getLockedFlag() != null && chapter.getLockedFlag() == 1) {
                throw new IllegalStateException("Locked chapter cannot update progress");
            }
        }
    }

    private void applyProjectFields(ReportProject project, ReportProjectSaveRequest request) {
        project.setReportCode(request.getReportCode().trim());
        project.setProjectName(request.getProjectName().trim());
        project.setAcademicYear(request.getAcademicYear().trim());
        project.setSemesterId(request.getSemesterId());
        project.setOwnerUserId(request.getOwnerUserId());
        project.setGenerationMode(StringUtils.hasText(request.getGenerationMode())
                ? request.getGenerationMode().trim().toUpperCase(Locale.ROOT)
                : "MANUAL");
        project.setRemark(normalizeText(request.getRemark()));
    }

    private int upsertChapterNode(Long projectId,
                                  Long parentId,
                                  ReportChapterSaveRequest request,
                                  int sortNo,
                                  Map<Long, ReportChapter> existingMap,
                                  Set<Long> retainedIds) {
        ReportChapter chapter = request.getId() == null ? null : existingMap.get(request.getId());
        boolean created = chapter == null;
        if (created) {
            chapter = new ReportChapter();
            chapter.setProjectId(projectId);
            chapter.setChapterStatus(CHAPTER_TODO);
            chapter.setLockedFlag(0);
            EntityAuditSupport.touchCreate(chapter);
        } else {
            EntityAuditSupport.touchUpdate(chapter);
        }
        chapter.setParentId(parentId);
        chapter.setChapterCode(request.getChapterCode().trim());
        chapter.setChapterTitle(request.getChapterTitle().trim());
        chapter.setSourceType(normalizeEnum(request.getSourceType()));
        chapter.setSourceRefId(request.getSourceRefId());
        chapter.setSortNo(request.getSortNo() == null ? sortNo : request.getSortNo());
        chapter.setRemark(normalizeText(request.getRemark()));
        if (created) {
            reportChapterService.save(chapter);
        } else {
            reportChapterService.updateById(chapter);
        }
        retainedIds.add(chapter.getId());

        if (request.getChildren() != null) {
            int childSortNo = 1;
            for (ReportChapterSaveRequest child : request.getChildren()) {
                childSortNo = upsertChapterNode(projectId, chapter.getId(), child, childSortNo, existingMap, retainedIds);
            }
        }
        return sortNo + 1;
    }

    private void ensureChaptersRemovable(List<Long> chapterIds) {
        if (chapterIds.isEmpty()) {
            return;
        }
        boolean hasAssignments = reportTaskAssignmentService.count(new LambdaQueryWrapper<ReportTaskAssignment>()
                .in(ReportTaskAssignment::getChapterId, chapterIds)
                .eq(ReportTaskAssignment::getIsDeleted, 0)) > 0;
        boolean hasDrafts = reportDraftService.count(new LambdaQueryWrapper<ReportDraft>()
                .in(ReportDraft::getChapterId, chapterIds)
                .eq(ReportDraft::getIsDeleted, 0)) > 0;
        boolean hasLogs = reportProgressLogService.count(new LambdaQueryWrapper<ReportProgressLog>()
                .in(ReportProgressLog::getChapterId, chapterIds)
                .eq(ReportProgressLog::getIsDeleted, 0)) > 0;
        if (hasAssignments || hasDrafts || hasLogs) {
            throw new IllegalStateException("Referenced chapters cannot be removed");
        }
    }

    private ReportProgressLog createProgressLog(Long projectId,
                                                Long chapterId,
                                                Long userId,
                                                BigDecimal progressPercent,
                                                String comment,
                                                String remark) {
        ReportProgressLog log = new ReportProgressLog();
        log.setProjectId(projectId);
        log.setChapterId(chapterId);
        log.setUserId(userId);
        log.setProgressPercent(progressPercent == null ? BigDecimal.ZERO : progressPercent.setScale(2, RoundingMode.HALF_UP));
        log.setComment(normalizeText(comment));
        log.setRemark(normalizeText(remark));
        EntityAuditSupport.touchCreate(log);
        reportProgressLogService.save(log);
        return log;
    }

    private void syncAssignmentsByProgress(Long chapterId, Long userId, BigDecimal progressPercent, String chapterStatus) {
        List<ReportTaskAssignment> assignments = reportTaskAssignmentService.list(new LambdaQueryWrapper<ReportTaskAssignment>()
                .eq(ReportTaskAssignment::getChapterId, chapterId)
                .eq(ReportTaskAssignment::getIsDeleted, 0)
                .eq(userId != null, ReportTaskAssignment::getAssigneeUserId, userId));
        if (assignments.isEmpty()) {
            return;
        }
        for (ReportTaskAssignment assignment : assignments) {
            if (CHAPTER_COMPLETED.equals(chapterStatus)
                    || (progressPercent != null && progressPercent.compareTo(new BigDecimal("100")) >= 0)) {
                assignment.setAssignmentStatus(ASSIGNMENT_COMPLETED);
                assignment.setCompletedAt(LocalDateTime.now());
            } else if (progressPercent != null && progressPercent.compareTo(BigDecimal.ZERO) > 0) {
                assignment.setAssignmentStatus(ASSIGNMENT_IN_PROGRESS);
                assignment.setCompletedAt(null);
            }
            EntityAuditSupport.touchUpdate(assignment);
        }
        reportTaskAssignmentService.updateBatchById(assignments);
    }

    private void syncProjectSummary(Long projectId) {
        ReportProject project = getRequiredProject(projectId);
        List<ReportChapter> chapters = listActiveChapters(projectId);
        List<ReportTaskAssignment> assignments = listActiveAssignmentsByProject(projectId);
        List<ReportProgressLog> logs = listActiveLogs(projectId);
        Map<Long, BigDecimal> progressMap = buildLatestChapterProgressMap(chapters, assignments, logs);
        project.setTotalChapters(chapters.size());
        project.setLockedFlag(!chapters.isEmpty() && chapters.stream()
                .allMatch(chapter -> chapter.getLockedFlag() != null && chapter.getLockedFlag() == 1) ? 1 : 0);

        if (chapters.isEmpty()) {
            project.setStatus(PROJECT_DRAFT);
        } else {
            boolean allCompleted = chapters.stream()
                    .allMatch(chapter -> progressMap.getOrDefault(chapter.getId(), BigDecimal.ZERO).compareTo(new BigDecimal("100")) >= 0);
            boolean anyProgress = chapters.stream().anyMatch(chapter ->
                    progressMap.getOrDefault(chapter.getId(), BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0
                            || StringUtils.hasText(chapter.getContentText()))
                    || assignments.stream().anyMatch(assignment -> !ASSIGNMENT_PENDING.equals(assignment.getAssignmentStatus()));
            project.setStatus(allCompleted ? PROJECT_COMPLETED : (anyProgress ? PROJECT_IN_PROGRESS : PROJECT_DRAFT));
        }

        EntityAuditSupport.touchUpdate(project);
        updateById(project);
    }

    private Map<Long, BigDecimal> buildLatestChapterProgressMap(List<ReportChapter> chapters,
                                                                List<ReportTaskAssignment> assignments,
                                                                List<ReportProgressLog> logs) {
        Map<Long, BigDecimal> latest = new LinkedHashMap<>();
        logs.stream()
                .filter(log -> log.getChapterId() != null)
                .sorted(Comparator.comparing(ReportProgressLog::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ReportProgressLog::getId, Comparator.reverseOrder()))
                .forEach(log -> latest.putIfAbsent(log.getChapterId(), safeProgress(log.getProgressPercent())));

        Map<Long, List<ReportTaskAssignment>> assignmentMap = assignments.stream()
                .collect(Collectors.groupingBy(ReportTaskAssignment::getChapterId, LinkedHashMap::new, Collectors.toList()));
        for (ReportChapter chapter : chapters) {
            latest.computeIfAbsent(chapter.getId(), key -> {
                List<ReportTaskAssignment> chapterAssignments = assignmentMap.getOrDefault(key, List.of());
                if (!chapterAssignments.isEmpty() && chapterAssignments.stream()
                        .allMatch(assignment -> ASSIGNMENT_COMPLETED.equals(assignment.getAssignmentStatus()))) {
                    return new BigDecimal("100");
                }
                if (CHAPTER_COMPLETED.equals(chapter.getChapterStatus())) {
                    return new BigDecimal("100");
                }
                if (CHAPTER_IN_PROGRESS.equals(chapter.getChapterStatus()) || StringUtils.hasText(chapter.getContentText())) {
                    return new BigDecimal("50");
                }
                return BigDecimal.ZERO;
            });
        }
        return latest;
    }

    private BigDecimal calculateProjectProgress(List<ReportChapter> chapters, Map<Long, BigDecimal> progressMap) {
        if (chapters.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal sum = BigDecimal.ZERO;
        for (ReportChapter chapter : chapters) {
            sum = sum.add(progressMap.getOrDefault(chapter.getId(), BigDecimal.ZERO));
        }
        return sum.divide(BigDecimal.valueOf(chapters.size()), 2, RoundingMode.HALF_UP);
    }

    private ReportProject getRequiredProject(Long projectId) {
        ReportProject project = projectId == null ? null : lambdaQuery()
                .eq(ReportProject::getId, projectId)
                .eq(ReportProject::getIsDeleted, 0)
                .one();
        if (project == null) {
            throw new IllegalArgumentException("Report project not found");
        }
        return project;
    }

    private ReportChapter getRequiredChapter(Long chapterId) {
        ReportChapter chapter = chapterId == null ? null : reportChapterService.getOne(new LambdaQueryWrapper<ReportChapter>()
                .eq(ReportChapter::getId, chapterId)
                .eq(ReportChapter::getIsDeleted, 0), false);
        if (chapter == null) {
            throw new IllegalArgumentException("Report chapter not found");
        }
        return chapter;
    }

    private List<ReportChapter> listActiveChapters(Long projectId) {
        return reportChapterService.list(new LambdaQueryWrapper<ReportChapter>()
                .eq(ReportChapter::getProjectId, projectId)
                .eq(ReportChapter::getIsDeleted, 0)
                .orderByAsc(ReportChapter::getSortNo)
                .orderByAsc(ReportChapter::getId));
    }

    private List<ReportTaskAssignment> listActiveAssignmentsByProject(Long projectId) {
        return reportTaskAssignmentService.list(new LambdaQueryWrapper<ReportTaskAssignment>()
                .eq(ReportTaskAssignment::getProjectId, projectId)
                .eq(ReportTaskAssignment::getIsDeleted, 0)
                .orderByAsc(ReportTaskAssignment::getDueDate)
                .orderByAsc(ReportTaskAssignment::getId));
    }

    private List<ReportDraft> listActiveDrafts(List<Long> chapterIds) {
        if (chapterIds == null || chapterIds.isEmpty()) {
            return List.of();
        }
        return reportDraftService.list(new LambdaQueryWrapper<ReportDraft>()
                .in(ReportDraft::getChapterId, chapterIds)
                .eq(ReportDraft::getIsDeleted, 0));
    }

    private List<ReportProgressLog> listActiveLogs(Long projectId) {
        return reportProgressLogService.list(new LambdaQueryWrapper<ReportProgressLog>()
                .eq(ReportProgressLog::getProjectId, projectId)
                .eq(ReportProgressLog::getIsDeleted, 0));
    }

    private Map<Long, SysUser> buildUserMap(Long projectId,
                                            List<ReportChapter> chapters,
                                            List<ReportTaskAssignment> assignments,
                                            List<ReportDraft> drafts,
                                            List<ReportProgressLog> logs) {
        LinkedHashSet<Long> userIds = new LinkedHashSet<>();
        ReportProject project = getRequiredProject(projectId);
        if (project.getOwnerUserId() != null) {
            userIds.add(project.getOwnerUserId());
        }
        assignments.stream().map(ReportTaskAssignment::getAssigneeUserId).filter(Objects::nonNull).forEach(userIds::add);
        drafts.stream().map(ReportDraft::getEditedBy).filter(Objects::nonNull).forEach(userIds::add);
        logs.stream().map(ReportProgressLog::getUserId).filter(Objects::nonNull).forEach(userIds::add);
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return sysUserService.listByIds(userIds).stream()
                .filter(this::isActiveUser)
                .collect(Collectors.toMap(SysUser::getId, user -> user, (left, right) -> left, LinkedHashMap::new));
    }

    private Set<Long> resolveResponsibleChapterIds(ReportProject project,
                                                   List<ReportChapter> chapters,
                                                   List<ReportTaskAssignment> assignments,
                                                   Long viewerUserId) {
        if (viewerUserId == null || Objects.equals(project.getOwnerUserId(), viewerUserId)) {
            return chapters.stream().map(ReportChapter::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        return assignments.stream()
                .filter(assignment -> Objects.equals(assignment.getAssigneeUserId(), viewerUserId))
                .map(ReportTaskAssignment::getChapterId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Long> resolveDisplayChapterIds(List<ReportChapter> chapters, Set<Long> responsibleIds, boolean viewAll) {
        if (viewAll) {
            return chapters.stream().map(ReportChapter::getId).collect(Collectors.toCollection(LinkedHashSet::new));
        }
        Map<Long, ReportChapter> chapterMap = chapters.stream()
                .collect(Collectors.toMap(ReportChapter::getId, chapter -> chapter, (left, right) -> left, LinkedHashMap::new));
        LinkedHashSet<Long> displayIds = new LinkedHashSet<>(responsibleIds);
        ArrayDeque<Long> stack = new ArrayDeque<>(responsibleIds);
        while (!stack.isEmpty()) {
            ReportChapter chapter = chapterMap.get(stack.pop());
            if (chapter != null && chapter.getParentId() != null && displayIds.add(chapter.getParentId())) {
                stack.push(chapter.getParentId());
            }
        }
        return displayIds;
    }

    private int nextDraftVersion(Long chapterId) {
        List<ReportDraft> drafts = reportDraftService.list(new LambdaQueryWrapper<ReportDraft>()
                .eq(ReportDraft::getChapterId, chapterId)
                .eq(ReportDraft::getIsDeleted, 0)
                .orderByDesc(ReportDraft::getVersionNo)
                .last("LIMIT 1"));
        return drafts.isEmpty() ? 1 : drafts.getFirst().getVersionNo() + 1;
    }

    private void sendAssignmentNotice(ReportProject project,
                                      List<ReportTaskAssignment> assignments,
                                      Map<Long, ReportChapter> chapterMap) {
        if (assignments == null || assignments.isEmpty()) {
            return;
        }
        Map<Long, List<ReportTaskAssignment>> grouped = assignments.stream()
                .filter(assignment -> assignment.getAssigneeUserId() != null)
                .collect(Collectors.groupingBy(
                        ReportTaskAssignment::getAssigneeUserId,
                        LinkedHashMap::new,
                        Collectors.toList()));
        for (Map.Entry<Long, List<ReportTaskAssignment>> entry : grouped.entrySet()) {
            if (!isActiveUserId(entry.getKey())) {
                continue;
            }
            String content = entry.getValue().stream()
                    .map(assignment -> {
                        ReportChapter chapter = chapterMap.get(assignment.getChapterId());
                        String title = chapter == null ? String.valueOf(assignment.getChapterId()) : chapter.getChapterTitle();
                        return title + " (due " + assignment.getDueDate() + ")";
                    })
                    .collect(Collectors.joining("; "));
            NoticeSendRequest noticeRequest = new NoticeSendRequest();
            noticeRequest.setNoticeType("REPORT_TASK_ASSIGNMENT");
            noticeRequest.setTitle("Report chapter assignment: " + project.getProjectName());
            noticeRequest.setContent("Assigned chapters: " + content);
            noticeRequest.setSenderUserId(project.getOwnerUserId());
            noticeRequest.setBizType("REPORT_PROJECT");
            noticeRequest.setBizId(project.getId());
            noticeRequest.setChannelType("INTERNAL");
            noticeRequest.setPriorityLevel(1);
            noticeRequest.setSendAt(LocalDateTime.now());
            noticeRequest.setRecipientUserIds(List.of(entry.getKey()));
            noticeMessageService.sendNotice(noticeRequest);
        }
    }

    private String buildInitialDraftContent(ReportChapter chapter) {
        String type = normalizeEnum(chapter.getSourceType());
        if ("PROGRAM_TARGET".equals(type) && chapter.getSourceRefId() != null) {
            TrProgramTarget target = trProgramTargetService.getById(chapter.getSourceRefId());
            if (target != null) {
                return """
                        %s

                        Related program target: %s %s

                        Description:
                        %s

                        Suggested writing points: target positioning, supporting courses, evidence of attainment,
                        and continuous improvement.
                        """.formatted(
                        chapter.getChapterTitle(),
                        defaultText(target.getTargetCode()),
                        defaultText(target.getTargetName()),
                        defaultText(target.getTargetDesc()));
            }
        }
        if ("GRADUATION_REQUIREMENT".equals(type) && chapter.getSourceRefId() != null) {
            TrGraduationRequirement requirement = trGraduationRequirementService.getById(chapter.getSourceRefId());
            if (requirement != null) {
                return """
                        %s

                        Related graduation requirement: %s %s

                        Description:
                        %s

                        Suggested writing points: indicator support, course mapping, recent attainment evidence,
                        and improvement actions.
                        """.formatted(
                        chapter.getChapterTitle(),
                        defaultText(requirement.getRequirementCode()),
                        defaultText(requirement.getRequirementName()),
                        defaultText(requirement.getRequirementDesc()));
            }
        }
        if ("COURSE".equals(type) && chapter.getSourceRefId() != null) {
            EduCourse course = eduCourseService.getById(chapter.getSourceRefId());
            if (course != null) {
                return """
                        %s

                        Related course: %s %s

                        Course type: %s
                        Credit: %s
                        Total hours: %s

                        Suggested writing points: course objectives, teaching delivery, assessment methods,
                        and attainment analysis.
                        """.formatted(
                        chapter.getChapterTitle(),
                        defaultText(course.getCourseCode()),
                        defaultText(course.getCourseName()),
                        defaultText(course.getCourseType()),
                        course.getCredit() == null ? "-" : course.getCredit().toPlainString(),
                        course.getTotalHours() == null ? "-" : course.getTotalHours());
            }
        }
        if ("SURVEY_QUESTIONNAIRE".equals(type) && chapter.getSourceRefId() != null) {
            SurveyQuestionnaire questionnaire = surveyQuestionnaireService.getActiveQuestionnaireEntity(chapter.getSourceRefId());
            if (questionnaire != null) {
                return """
                        %s

                        Related questionnaire: %s
                        Publish status: %s
                        Questionnaire type: %s

                        Suggested writing points: objective, target respondents, response summary,
                        and conclusions for improvement.
                        """.formatted(
                        chapter.getChapterTitle(),
                        defaultText(questionnaire.getTitle()),
                        defaultText(questionnaire.getPublishStatus()),
                        defaultText(questionnaire.getQuestionnaireType()));
            }
        }
        return """
                %s

                Related source type: %s
                Related source id: %s

                Suggested writing points: current situation, key data, issue analysis,
                improvement actions, and conclusion.
                """.formatted(
                chapter.getChapterTitle(),
                defaultText(type),
                chapter.getSourceRefId() == null ? "-" : chapter.getSourceRefId());
    }

    private void appendChapterMarkdown(StringBuilder builder,
                                       Map<Long, List<ReportChapter>> childrenMap,
                                       Long parentId,
                                       int level) {
        for (ReportChapter chapter : childrenMap.getOrDefault(parentId, List.of())) {
            int headerLevel = Math.min(level, 6);
            builder.append("#".repeat(headerLevel)).append(' ')
                    .append(chapter.getChapterCode()).append(' ')
                    .append(chapter.getChapterTitle()).append(System.lineSeparator()).append(System.lineSeparator());
            builder.append(StringUtils.hasText(chapter.getContentText()) ? chapter.getContentText().trim() : "Content pending")
                    .append(System.lineSeparator()).append(System.lineSeparator());
            appendChapterMarkdown(builder, childrenMap, chapter.getId(), level + 1);
        }
    }

    private String resolveSourceDisplayName(String sourceType, Long sourceRefId) {
        if (!StringUtils.hasText(sourceType) || sourceRefId == null) {
            return null;
        }
        String type = normalizeEnum(sourceType);
        if ("PROGRAM_TARGET".equals(type)) {
            TrProgramTarget target = trProgramTargetService.getById(sourceRefId);
            return target == null ? type + "#" + sourceRefId : target.getTargetName();
        }
        if ("GRADUATION_REQUIREMENT".equals(type)) {
            TrGraduationRequirement requirement = trGraduationRequirementService.getById(sourceRefId);
            return requirement == null ? type + "#" + sourceRefId : requirement.getRequirementName();
        }
        if ("COURSE".equals(type)) {
            EduCourse course = eduCourseService.getById(sourceRefId);
            return course == null ? type + "#" + sourceRefId : course.getCourseName();
        }
        if ("SURVEY_QUESTIONNAIRE".equals(type)) {
            SurveyQuestionnaire questionnaire = surveyQuestionnaireService.getActiveQuestionnaireEntity(sourceRefId);
            return questionnaire == null ? type + "#" + sourceRefId : questionnaire.getTitle();
        }
        return type + "#" + sourceRefId;
    }

    private String resolveDraftChapterStatus(String chapterStatus, BigDecimal progressPercent) {
        if (StringUtils.hasText(chapterStatus)) {
            return normalizeChapterStatus(chapterStatus);
        }
        if (progressPercent != null) {
            return resolveStatusFromProgress(progressPercent);
        }
        return CHAPTER_IN_PROGRESS;
    }

    private String resolveStatusFromProgress(BigDecimal progressPercent) {
        if (progressPercent == null || progressPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return CHAPTER_TODO;
        }
        if (progressPercent.compareTo(new BigDecimal("100")) >= 0) {
            return CHAPTER_COMPLETED;
        }
        return CHAPTER_IN_PROGRESS;
    }

    private BigDecimal resolveEffectiveProgress(BigDecimal progressPercent, String chapterStatus) {
        if (progressPercent != null) {
            return safeProgress(progressPercent);
        }
        if (CHAPTER_COMPLETED.equals(chapterStatus)) {
            return new BigDecimal("100");
        }
        if (CHAPTER_IN_PROGRESS.equals(chapterStatus)) {
            return new BigDecimal("50");
        }
        return null;
    }

    private String normalizeChapterStatus(String status) {
        String normalized = normalizeEnum(status);
        if (!CHAPTER_TODO.equals(normalized)
                && !CHAPTER_IN_PROGRESS.equals(normalized)
                && !CHAPTER_COMPLETED.equals(normalized)) {
            throw new IllegalArgumentException("Unsupported chapter status: " + status);
        }
        return normalized;
    }

    private String normalizeAssignmentStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return ASSIGNMENT_PENDING;
        }
        String normalized = normalizeEnum(status);
        if (!ASSIGNMENT_PENDING.equals(normalized)
                && !ASSIGNMENT_IN_PROGRESS.equals(normalized)
                && !ASSIGNMENT_COMPLETED.equals(normalized)) {
            throw new IllegalArgumentException("Unsupported assignment status: " + status);
        }
        return normalized;
    }

    private void validateProgress(BigDecimal progressPercent) {
        if (progressPercent.compareTo(BigDecimal.ZERO) < 0 || progressPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Progress percent must be between 0 and 100");
        }
    }

    private boolean isActiveUserId(Long userId) {
        return isActiveUser(sysUserService.getById(userId));
    }

    private boolean isActiveUser(SysUser user) {
        return user != null && (user.getIsDeleted() == null || user.getIsDeleted() == 0);
    }

    private boolean isActiveSemesterId(Long semesterId) {
        EduSemester semester = eduSemesterService.getById(semesterId);
        return semester != null && (semester.getIsDeleted() == null || semester.getIsDeleted() == 0);
    }

    private String normalizeEnum(String value) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String normalizeText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String assignmentKey(ReportTaskAssignment assignment) {
        return assignmentKey(assignment.getChapterId(), assignment.getAssigneeUserId());
    }

    private String assignmentKey(Long chapterId, Long assigneeUserId) {
        return chapterId + "_" + assigneeUserId;
    }

    private BigDecimal safeProgress(BigDecimal progressPercent) {
        return progressPercent == null ? BigDecimal.ZERO : progressPercent.setScale(2, RoundingMode.HALF_UP);
    }

    private int compareChapter(ReportChapter left, ReportChapter right) {
        int bySort = Comparator.nullsLast(Integer::compareTo).compare(left.getSortNo(), right.getSortNo());
        if (bySort != 0) {
            return bySort;
        }
        return Comparator.nullsLast(Long::compareTo).compare(left.getId(), right.getId());
    }

    private String defaultText(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }

    private String fileExtension(String fileName) {
        if (!StringUtils.hasText(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
    }
}
