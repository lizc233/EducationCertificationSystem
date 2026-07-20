package com.educationcertificationsystem.program.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.service.EduCourseObjectiveIndicatorPointService;
import com.educationcertificationsystem.course.service.EduCourseService;
import com.educationcertificationsystem.course.service.TeachingTaskService;
import com.educationcertificationsystem.eval.service.EvalGraduationRequirementResultService;
import com.educationcertificationsystem.model.dto.ProgramVersionCopyRequest;
import com.educationcertificationsystem.model.entity.*;
import com.educationcertificationsystem.org.service.OrgGradeService;
import com.educationcertificationsystem.program.service.*;
import com.educationcertificationsystem.support.EntityAuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/program")
@RequiredArgsConstructor
public class ProgramController {

    private final TrProgramVersionService versionService;
    private final TrProgramTargetService targetService;
    private final TrGraduationRequirementService requirementService;
    private final TrRequirementIndicatorPointService indicatorPointService;
    private final TrRequirementIndicatorSupportService indicatorSupportService;
    private final TrTargetRequirementSupportService targetSupportService;
    private final TrProgramCourseService programCourseService;
    private final TrCourseRequirementSupportService courseSupportService;
    private final TrProgramApplyGradeService applyGradeService;
    private final OrgGradeService orgGradeService;
    private final EduCourseService courseService;
    private final EduCourseObjectiveIndicatorPointService objectiveIndicatorPointService;
    private final TeachingTaskService teachingTaskService;
    private final EvalGraduationRequirementResultService evalGraduationRequirementResultService;

    @GetMapping("/versions")
    public Result<Page<TrProgramVersion>> versions(@RequestParam(defaultValue = "1") long page,
                                                   @RequestParam(defaultValue = "10") long size,
                                                   @RequestParam(required = false) Long majorId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String keyword) {
        QueryWrapper<TrProgramVersion> wrapper = activeWrapper();
        if (majorId != null) {
            wrapper.eq("major_id", majorId);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq("status", status);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("version_no", keyword)
                    .or().like("version_name", keyword)
                    .or().like("remark", keyword));
        }
        wrapper.orderByDesc("id");
        return Result.success(versionService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/versions/{id}")
    public Result<TrProgramVersion> version(@PathVariable Long id) {
        return Result.success(versionService.getById(id));
    }

    @PostMapping("/versions")
    public Result<TrProgramVersion> createVersion(@RequestBody TrProgramVersion version) {
        validateVersionUnique(version.getMajorId(), version.getVersionNo(), null);
        if (version.getStatus() == null || version.getStatus().isBlank()) {
            version.setStatus("DRAFT");
        }
        if (version.getEffectiveDate() == null) {
            version.setEffectiveDate(java.time.LocalDate.now());
        }
        EntityAuditSupport.touchCreate(version);
        versionService.save(version);
        return Result.success(version);
    }

    @PutMapping("/versions/{id}")
    public Result<TrProgramVersion> updateVersion(@PathVariable Long id, @RequestBody TrProgramVersion version) {
        TrProgramVersion current = versionService.getById(id);
        if (current == null) {
            return Result.error("版本不存在");
        }
        BeanUtil.copyProperties(version, current, CopyOptions.create()
                .setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted", "releasedAt", "copyFromVersionId"));
        validateVersionUnique(current.getMajorId(), current.getVersionNo(), current.getId());
        EntityAuditSupport.touchUpdate(current);
        versionService.updateById(current);
        return Result.success(current);
    }

    @PostMapping("/versions/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        TrProgramVersion current = versionService.getById(id);
        if (current == null) {
            return Result.error("版本不存在");
        }
        current.setStatus(status);
        if ("RELEASED".equalsIgnoreCase(status)) {
            current.setReleasedAt(LocalDateTime.now());
        }
        EntityAuditSupport.touchUpdate(current);
        versionService.updateById(current);
        return Result.success();
    }

    @DeleteMapping("/versions/{id}")
    @Transactional
    public Result<Void> deleteVersion(@PathVariable Long id) {
        TrProgramVersion current = versionService.getById(id);
        if (current == null) {
            return Result.error("版本不存在");
        }
        validateVersionDeleteAllowed(id);
        EntityAuditSupport.touchDelete(current);
        versionService.updateById(current);

        List<Long> targetIds = targetService.list(new QueryWrapper<TrProgramTarget>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                        .eq("program_version_id", id))
                .stream().map(TrProgramTarget::getId).toList();
        markDeleted(targetService.listByIds(targetIds), targetService);

        List<Long> requirementIds = requirementService.list(new QueryWrapper<TrGraduationRequirement>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                        .eq("program_version_id", id))
                .stream().map(TrGraduationRequirement::getId).toList();
        markDeleted(requirementService.listByIds(requirementIds), requirementService);

        List<Long> indicatorIds = indicatorPointService.list(new QueryWrapper<TrRequirementIndicatorPoint>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                        .in(!requirementIds.isEmpty(), "graduation_requirement_id", requirementIds))
                .stream().map(TrRequirementIndicatorPoint::getId).toList();
        markDeleted(indicatorPointService.listByIds(indicatorIds), indicatorPointService);

        markDeleted(targetSupportService.list(new QueryWrapper<TrTargetRequirementSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .in(!targetIds.isEmpty(), "program_target_id", targetIds)), targetSupportService);

        markDeleted(indicatorSupportService.list(new QueryWrapper<TrRequirementIndicatorSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .in(!requirementIds.isEmpty(), "graduation_requirement_id", requirementIds)), indicatorSupportService);

        markDeleted(programCourseService.list(new QueryWrapper<TrProgramCourse>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", id)), programCourseService);

        markDeleted(courseSupportService.list(new QueryWrapper<TrCourseRequirementSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", id)), courseSupportService);

        markDeleted(applyGradeService.list(new QueryWrapper<TrProgramApplyGrade>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", id)), applyGradeService);
        return Result.success();
    }

    @PostMapping("/versions/{id}/copy")
    @Transactional
    public Result<TrProgramVersion> copyVersion(@PathVariable Long id, @RequestBody(required = false) ProgramVersionCopyRequest request) {
        TrProgramVersion source = versionService.getById(id);
        if (source == null) {
            return Result.error("版本不存在");
        }
        TrProgramVersion copy = new TrProgramVersion();
        BeanUtil.copyProperties(source, copy, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "versionNo", "versionName", "status", "copyFromVersionId", "releasedAt", "createdAt", "updatedAt", "isDeleted"));
        copy.setMajorId(Optional.ofNullable(request).map(ProgramVersionCopyRequest::getMajorId).orElse(source.getMajorId()));
        String baseVersionNo = Optional.ofNullable(source.getVersionNo()).filter(s -> !s.isBlank()).orElse("VERSION");
        String baseVersionName = Optional.ofNullable(source.getVersionName()).filter(s -> !s.isBlank()).orElse(baseVersionNo);
        copy.setVersionNo(Optional.ofNullable(request).map(ProgramVersionCopyRequest::getVersionNo).filter(s -> !s.isBlank()).orElse(baseVersionNo + "-COPY"));
        copy.setVersionName(Optional.ofNullable(request).map(ProgramVersionCopyRequest::getVersionName).filter(s -> !s.isBlank()).orElse(baseVersionName + " 副本"));
        copy.setStatus("DRAFT");
        copy.setCopyFromVersionId(source.getId());
        copy.setReleasedAt(null);
        validateVersionUnique(copy.getMajorId(), copy.getVersionNo(), null);
        EntityAuditSupport.touchCreate(copy);
        versionService.save(copy);

        Map<Long, Long> targetMap = new HashMap<>();
        Map<Long, Long> requirementMap = new HashMap<>();
        Map<Long, Long> indicatorMap = new HashMap<>();

        List<TrProgramTarget> targets = targetService.list(new QueryWrapper<TrProgramTarget>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", source.getId())
                .orderByAsc("sort_no")
                .orderByAsc("id"));
        for (TrProgramTarget target : targets) {
            Long oldId = target.getId();
            target.setId(null);
            target.setProgramVersionId(copy.getId());
            EntityAuditSupport.touchCreate(target);
            targetService.save(target);
            targetMap.put(oldId, target.getId());
        }

        List<TrGraduationRequirement> requirements = requirementService.list(new QueryWrapper<TrGraduationRequirement>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", source.getId())
                .orderByAsc("sort_no")
                .orderByAsc("id"));
        for (TrGraduationRequirement requirement : requirements) {
            Long oldId = requirement.getId();
            requirement.setId(null);
            requirement.setProgramVersionId(copy.getId());
            EntityAuditSupport.touchCreate(requirement);
            requirementService.save(requirement);
            requirementMap.put(oldId, requirement.getId());
        }

        List<TrRequirementIndicatorPoint> points = indicatorPointService.list(new QueryWrapper<TrRequirementIndicatorPoint>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .in(!requirementMap.isEmpty(), "graduation_requirement_id", requirementMap.keySet())
                .orderByAsc("sort_no")
                .orderByAsc("id"));
        for (TrRequirementIndicatorPoint point : points) {
            Long oldId = point.getId();
            point.setId(null);
            point.setGraduationRequirementId(requirementMap.get(point.getGraduationRequirementId()));
            EntityAuditSupport.touchCreate(point);
            indicatorPointService.save(point);
            indicatorMap.put(oldId, point.getId());
        }

        List<TrTargetRequirementSupport> targetSupports = targetSupportService.list(new QueryWrapper<TrTargetRequirementSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .in(!targetMap.isEmpty(), "program_target_id", targetMap.keySet()));
        for (TrTargetRequirementSupport support : targetSupports) {
            Long newTargetId = targetMap.get(support.getProgramTargetId());
            Long newRequirementId = requirementMap.get(support.getGraduationRequirementId());
            if (newTargetId == null || newRequirementId == null) {
                continue;
            }
            support.setId(null);
            support.setProgramTargetId(newTargetId);
            support.setGraduationRequirementId(newRequirementId);
            EntityAuditSupport.touchCreate(support);
            targetSupportService.save(support);
        }

        List<TrRequirementIndicatorSupport> indicatorSupports = indicatorSupportService.list(new QueryWrapper<TrRequirementIndicatorSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .in(!requirementMap.isEmpty(), "graduation_requirement_id", requirementMap.keySet()));
        for (TrRequirementIndicatorSupport support : indicatorSupports) {
            Long newRequirementId = requirementMap.get(support.getGraduationRequirementId());
            Long newIndicatorId = indicatorMap.get(support.getIndicatorPointId());
            if (newRequirementId == null || newIndicatorId == null) {
                continue;
            }
            support.setId(null);
            support.setGraduationRequirementId(newRequirementId);
            support.setIndicatorPointId(newIndicatorId);
            EntityAuditSupport.touchCreate(support);
            indicatorSupportService.save(support);
        }

        List<TrProgramCourse> programCourses = programCourseService.list(new QueryWrapper<TrProgramCourse>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", source.getId()));
        for (TrProgramCourse item : programCourses) {
            item.setId(null);
            item.setProgramVersionId(copy.getId());
            EntityAuditSupport.touchCreate(item);
            programCourseService.save(item);
        }

        List<TrCourseRequirementSupport> courseSupports = courseSupportService.list(new QueryWrapper<TrCourseRequirementSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", source.getId()));
        for (TrCourseRequirementSupport item : courseSupports) {
            item.setId(null);
            item.setProgramVersionId(copy.getId());
            EntityAuditSupport.touchCreate(item);
            courseSupportService.save(item);
        }

        List<TrProgramApplyGrade> applyGrades = applyGradeService.list(new QueryWrapper<TrProgramApplyGrade>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", source.getId()));
        for (TrProgramApplyGrade item : applyGrades) {
            item.setId(null);
            item.setProgramVersionId(copy.getId());
            EntityAuditSupport.touchCreate(item);
            applyGradeService.save(item);
        }
        return Result.success(copy);
    }

    @GetMapping("/versions/{id}/grades")
    public Result<List<Long>> appliedGrades(@PathVariable Long id) {
        List<Long> gradeIds = applyGradeService.list(new QueryWrapper<TrProgramApplyGrade>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                        .eq("program_version_id", id))
                .stream()
                .map(TrProgramApplyGrade::getGradeId)
                .toList();
        return Result.success(gradeIds);
    }

    @PutMapping("/versions/{id}/grades")
    @Transactional
    public Result<Void> replaceGrades(@PathVariable Long id, @RequestBody List<Long> gradeIds) {
        TrProgramVersion version = requireActiveVersion(id);
        List<Long> distinctGradeIds = CollectionUtils.isEmpty(gradeIds)
                ? List.of()
                : gradeIds.stream().filter(Objects::nonNull).distinct().toList();
        validateGradeIdsExist(distinctGradeIds);
        validateGradeIdsMatchVersionMajor(version, distinctGradeIds);
        applyGradeService.remove(new QueryWrapper<TrProgramApplyGrade>().eq("program_version_id", id));
        if (!distinctGradeIds.isEmpty()) {
            List<TrProgramApplyGrade> rows = distinctGradeIds.stream().map(gradeId -> {
                TrProgramApplyGrade row = new TrProgramApplyGrade();
                row.setProgramVersionId(id);
                row.setGradeId(gradeId);
                EntityAuditSupport.touchCreate(row);
                return row;
            }).collect(Collectors.toList());
            applyGradeService.saveBatch(rows);
        }
        return Result.success();
    }

    @GetMapping("/targets")
    public Result<Page<TrProgramTarget>> targets(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) Long programVersionId,
                                                 @RequestParam(required = false) String keyword) {
        QueryWrapper<TrProgramTarget> wrapper = activeWrapper();
        if (programVersionId != null) {
            wrapper.eq("program_version_id", programVersionId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("target_code", keyword)
                    .or().like("target_name", keyword)
                    .or().like("target_desc", keyword));
        }
        wrapper.orderByAsc("sort_no").orderByAsc("id");
        return Result.success(targetService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/targets")
    public Result<TrProgramTarget> createTarget(@RequestBody TrProgramTarget target) {
        validateTargetCodeUnique(target.getProgramVersionId(), target.getTargetCode(), null);
        if (target.getEnabled() == null) {
            target.setEnabled(1);
        }
        EntityAuditSupport.touchCreate(target);
        targetService.save(target);
        return Result.success(target);
    }

    @PutMapping("/targets/{id}")
    public Result<TrProgramTarget> updateTarget(@PathVariable Long id, @RequestBody TrProgramTarget target) {
        TrProgramTarget current = targetService.getById(id);
        if (current == null) {
            return Result.error("培养目标不存在");
        }
        validateTargetCodeUnique(
                target.getProgramVersionId() != null ? target.getProgramVersionId() : current.getProgramVersionId(),
                StringUtils.hasText(target.getTargetCode()) ? target.getTargetCode() : current.getTargetCode(),
                current.getId());
        BeanUtil.copyProperties(target, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        targetService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/targets/{id}")
    @Transactional
    public Result<Void> deleteTarget(@PathVariable Long id) {
        TrProgramTarget current = targetService.getById(id);
        if (current == null) {
            return Result.error("培养目标不存在");
        }
        validateTargetDeleteAllowed(id);
        EntityAuditSupport.touchDelete(current);
        targetService.updateById(current);
        markDeleted(targetSupportService.list(new QueryWrapper<TrTargetRequirementSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_target_id", id)), targetSupportService);
        return Result.success();
    }

    @GetMapping("/graduation-requirements")
    public Result<Page<TrGraduationRequirement>> requirements(@RequestParam(defaultValue = "1") long page,
                                                              @RequestParam(defaultValue = "10") long size,
                                                              @RequestParam(required = false) Long programVersionId,
                                                              @RequestParam(required = false) String keyword) {
        QueryWrapper<TrGraduationRequirement> wrapper = activeWrapper();
        if (programVersionId != null) {
            wrapper.eq("program_version_id", programVersionId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("requirement_code", keyword)
                    .or().like("requirement_name", keyword)
                    .or().like("requirement_desc", keyword));
        }
        wrapper.orderByAsc("sort_no").orderByAsc("id");
        return Result.success(requirementService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/graduation-requirements")
    public Result<TrGraduationRequirement> createRequirement(@RequestBody TrGraduationRequirement requirement) {
        validateRequirementCodeUnique(requirement.getProgramVersionId(), requirement.getRequirementCode(), null);
        if (requirement.getEnabled() == null) {
            requirement.setEnabled(1);
        }
        EntityAuditSupport.touchCreate(requirement);
        requirementService.save(requirement);
        return Result.success(requirement);
    }

    @PutMapping("/graduation-requirements/{id}")
    public Result<TrGraduationRequirement> updateRequirement(@PathVariable Long id, @RequestBody TrGraduationRequirement requirement) {
        TrGraduationRequirement current = requirementService.getById(id);
        if (current == null) {
            return Result.error("毕业要求不存在");
        }
        validateRequirementCodeUnique(
                requirement.getProgramVersionId() != null ? requirement.getProgramVersionId() : current.getProgramVersionId(),
                StringUtils.hasText(requirement.getRequirementCode()) ? requirement.getRequirementCode() : current.getRequirementCode(),
                current.getId());
        BeanUtil.copyProperties(requirement, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        requirementService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/graduation-requirements/{id}")
    @Transactional
    public Result<Void> deleteRequirement(@PathVariable Long id) {
        TrGraduationRequirement current = requirementService.getById(id);
        if (current == null) {
            return Result.error("毕业要求不存在");
        }
        validateRequirementDeleteAllowed(id);
        EntityAuditSupport.touchDelete(current);
        requirementService.updateById(current);
        markDeleted(indicatorPointService.list(new QueryWrapper<TrRequirementIndicatorPoint>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("graduation_requirement_id", id)), indicatorPointService);
        markDeleted(indicatorSupportService.list(new QueryWrapper<TrRequirementIndicatorSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("graduation_requirement_id", id)), indicatorSupportService);
        return Result.success();
    }

    @GetMapping("/indicator-points")
    public Result<Page<TrRequirementIndicatorPoint>> indicatorPoints(@RequestParam(defaultValue = "1") long page,
                                                                     @RequestParam(defaultValue = "10") long size,
                                                                     @RequestParam(required = false) Long graduationRequirementId,
                                                                     @RequestParam(required = false) String keyword) {
        QueryWrapper<TrRequirementIndicatorPoint> wrapper = activeWrapper();
        if (graduationRequirementId != null) {
            wrapper.eq("graduation_requirement_id", graduationRequirementId);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("indicator_code", keyword)
                    .or().like("indicator_name", keyword)
                    .or().like("indicator_desc", keyword));
        }
        wrapper.orderByAsc("sort_no").orderByAsc("id");
        return Result.success(indicatorPointService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/indicator-points")
    public Result<TrRequirementIndicatorPoint> createIndicatorPoint(@RequestBody TrRequirementIndicatorPoint indicatorPoint) {
        validateIndicatorCodeUnique(indicatorPoint.getGraduationRequirementId(), indicatorPoint.getIndicatorCode(), null);
        if (indicatorPoint.getEnabled() == null) {
            indicatorPoint.setEnabled(1);
        }
        EntityAuditSupport.touchCreate(indicatorPoint);
        indicatorPointService.save(indicatorPoint);
        return Result.success(indicatorPoint);
    }

    @PutMapping("/indicator-points/{id}")
    public Result<TrRequirementIndicatorPoint> updateIndicatorPoint(@PathVariable Long id, @RequestBody TrRequirementIndicatorPoint indicatorPoint) {
        TrRequirementIndicatorPoint current = indicatorPointService.getById(id);
        if (current == null) {
            return Result.error("指标点不存在");
        }
        validateIndicatorCodeUnique(
                indicatorPoint.getGraduationRequirementId() != null ? indicatorPoint.getGraduationRequirementId() : current.getGraduationRequirementId(),
                StringUtils.hasText(indicatorPoint.getIndicatorCode()) ? indicatorPoint.getIndicatorCode() : current.getIndicatorCode(),
                current.getId());
        BeanUtil.copyProperties(indicatorPoint, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        indicatorPointService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/indicator-points/{id}")
    public Result<Void> deleteIndicatorPoint(@PathVariable Long id) {
        TrRequirementIndicatorPoint current = indicatorPointService.getById(id);
        if (current == null) {
            return Result.error("指标点不存在");
        }
        validateIndicatorDeleteAllowed(id);
        EntityAuditSupport.touchDelete(current);
        indicatorPointService.updateById(current);
        return Result.success();
    }

    @GetMapping("/target-supports")
    public Result<Page<TrTargetRequirementSupport>> targetSupports(@RequestParam(defaultValue = "1") long page,
                                                                   @RequestParam(defaultValue = "10") long size,
                                                                   @RequestParam(required = false) Long programVersionId,
                                                                   @RequestParam(required = false) Long programTargetId,
                                                                   @RequestParam(required = false) Long graduationRequirementId) {
        QueryWrapper<TrTargetRequirementSupport> wrapper = activeWrapper();
        if (programVersionId != null) {
            List<Long> targetIds = targetService.list(new QueryWrapper<TrProgramTarget>()
                            .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                            .eq("program_version_id", programVersionId))
                    .stream().map(TrProgramTarget::getId).toList();
            if (targetIds.isEmpty()) {
                return Result.success(new Page<>());
            }
            wrapper.in("program_target_id", targetIds);
        }
        if (programTargetId != null) {
            wrapper.eq("program_target_id", programTargetId);
        }
        if (graduationRequirementId != null) {
            wrapper.eq("graduation_requirement_id", graduationRequirementId);
        }
        wrapper.orderByDesc("id");
        return Result.success(targetSupportService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/target-supports")
    public Result<TrTargetRequirementSupport> createTargetSupport(@RequestBody TrTargetRequirementSupport support) {
        validateTargetSupportUnique(support.getProgramTargetId(), support.getGraduationRequirementId(), null);
        if (support.getSupportWeight() == null) {
            support.setSupportWeight(BigDecimal.ZERO);
        }
        EntityAuditSupport.touchCreate(support);
        targetSupportService.save(support);
        return Result.success(support);
    }

    @PutMapping("/target-supports/{id}")
    public Result<TrTargetRequirementSupport> updateTargetSupport(@PathVariable Long id, @RequestBody TrTargetRequirementSupport support) {
        TrTargetRequirementSupport current = targetSupportService.getById(id);
        if (current == null) {
            return Result.error("支撑关系不存在");
        }
        validateTargetSupportUnique(
                support.getProgramTargetId() != null ? support.getProgramTargetId() : current.getProgramTargetId(),
                support.getGraduationRequirementId() != null ? support.getGraduationRequirementId() : current.getGraduationRequirementId(),
                current.getId());
        BeanUtil.copyProperties(support, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        targetSupportService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/target-supports/{id}")
    public Result<Void> deleteTargetSupport(@PathVariable Long id) {
        TrTargetRequirementSupport current = targetSupportService.getById(id);
        if (current == null) {
            return Result.error("支撑关系不存在");
        }
        EntityAuditSupport.touchDelete(current);
        targetSupportService.updateById(current);
        return Result.success();
    }

    @GetMapping("/indicator-supports")
    public Result<Page<TrRequirementIndicatorSupport>> indicatorSupports(@RequestParam(defaultValue = "1") long page,
                                                                         @RequestParam(defaultValue = "10") long size,
                                                                         @RequestParam(required = false) Long programVersionId,
                                                                         @RequestParam(required = false) Long graduationRequirementId,
                                                                         @RequestParam(required = false) Long indicatorPointId) {
        QueryWrapper<TrRequirementIndicatorSupport> wrapper = activeWrapper();
        if (programVersionId != null) {
            List<Long> requirementIds = requirementService.list(new QueryWrapper<TrGraduationRequirement>()
                            .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                            .eq("program_version_id", programVersionId))
                    .stream().map(TrGraduationRequirement::getId).toList();
            if (requirementIds.isEmpty()) {
                return Result.success(new Page<>());
            }
            wrapper.in("graduation_requirement_id", requirementIds);
        }
        if (graduationRequirementId != null) {
            wrapper.eq("graduation_requirement_id", graduationRequirementId);
        }
        if (indicatorPointId != null) {
            wrapper.eq("indicator_point_id", indicatorPointId);
        }
        wrapper.orderByDesc("id");
        return Result.success(indicatorSupportService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/indicator-supports")
    public Result<TrRequirementIndicatorSupport> createIndicatorSupport(@RequestBody TrRequirementIndicatorSupport support) {
        validateIndicatorSupportUnique(support.getGraduationRequirementId(), support.getIndicatorPointId(), null);
        if (support.getSupportWeight() == null) {
            support.setSupportWeight(BigDecimal.ZERO);
        }
        EntityAuditSupport.touchCreate(support);
        indicatorSupportService.save(support);
        return Result.success(support);
    }

    @PutMapping("/indicator-supports/{id}")
    public Result<TrRequirementIndicatorSupport> updateIndicatorSupport(@PathVariable Long id, @RequestBody TrRequirementIndicatorSupport support) {
        TrRequirementIndicatorSupport current = indicatorSupportService.getById(id);
        if (current == null) {
            return Result.error("指标支撑关系不存在");
        }
        validateIndicatorSupportUnique(
                support.getGraduationRequirementId() != null ? support.getGraduationRequirementId() : current.getGraduationRequirementId(),
                support.getIndicatorPointId() != null ? support.getIndicatorPointId() : current.getIndicatorPointId(),
                current.getId());
        BeanUtil.copyProperties(support, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        indicatorSupportService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/indicator-supports/{id}")
    public Result<Void> deleteIndicatorSupport(@PathVariable Long id) {
        TrRequirementIndicatorSupport current = indicatorSupportService.getById(id);
        if (current == null) {
            return Result.error("指标支撑关系不存在");
        }
        EntityAuditSupport.touchDelete(current);
        indicatorSupportService.updateById(current);
        return Result.success();
    }

    @GetMapping("/courses")
    public Result<Page<TrProgramCourse>> programCourses(@RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size,
                                                        @RequestParam(required = false) Long programVersionId,
                                                        @RequestParam(required = false) Long courseId) {
        QueryWrapper<TrProgramCourse> wrapper = activeWrapper();
        if (programVersionId != null) {
            wrapper.eq("program_version_id", programVersionId);
        }
        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        wrapper.orderByAsc("sort_no").orderByAsc("id");
        return Result.success(programCourseService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/courses")
    public Result<TrProgramCourse> createProgramCourse(@RequestBody TrProgramCourse course) {
        validateProgramCourseUnique(course.getProgramVersionId(), course.getCourseId(), null);
        validateCourseSelectable(course.getCourseId());
        if (course.getIsRequired() == null) {
            course.setIsRequired(1);
        }
        EntityAuditSupport.touchCreate(course);
        programCourseService.save(course);
        return Result.success(course);
    }

    @PutMapping("/courses/{id}")
    public Result<TrProgramCourse> updateProgramCourse(@PathVariable Long id, @RequestBody TrProgramCourse course) {
        TrProgramCourse current = programCourseService.getById(id);
        if (current == null) {
            return Result.error("课程配置不存在");
        }
        Long targetProgramVersionId = course.getProgramVersionId() != null ? course.getProgramVersionId() : current.getProgramVersionId();
        Long targetCourseId = course.getCourseId() != null ? course.getCourseId() : current.getCourseId();
        validateProgramCourseUnique(targetProgramVersionId, targetCourseId, current.getId());
        if (!Objects.equals(targetCourseId, current.getCourseId())) {
            validateCourseSelectable(targetCourseId);
        }
        BeanUtil.copyProperties(course, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        programCourseService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/courses/{id}")
    public Result<Void> deleteProgramCourse(@PathVariable Long id) {
        TrProgramCourse current = programCourseService.getById(id);
        if (current == null) {
            return Result.error("课程配置不存在");
        }
        EntityAuditSupport.touchDelete(current);
        programCourseService.updateById(current);
        markDeleted(courseSupportService.list(new QueryWrapper<TrCourseRequirementSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("program_version_id", current.getProgramVersionId())
                .eq("course_id", current.getCourseId())), courseSupportService);
        return Result.success();
    }

    @GetMapping("/course-supports")
    public Result<Page<TrCourseRequirementSupport>> courseSupports(@RequestParam(defaultValue = "1") long page,
                                                                   @RequestParam(defaultValue = "10") long size,
                                                                   @RequestParam(required = false) Long programVersionId,
                                                                   @RequestParam(required = false) Long courseId,
                                                                   @RequestParam(required = false) Long graduationRequirementId) {
        QueryWrapper<TrCourseRequirementSupport> wrapper = activeWrapper();
        if (programVersionId != null) {
            wrapper.eq("program_version_id", programVersionId);
        }
        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        if (graduationRequirementId != null) {
            wrapper.eq("graduation_requirement_id", graduationRequirementId);
        }
        wrapper.orderByDesc("id");
        return Result.success(courseSupportService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping("/course-supports")
    public Result<TrCourseRequirementSupport> createCourseSupport(@RequestBody TrCourseRequirementSupport support) {
        validateCourseSupportUnique(support.getProgramVersionId(), support.getCourseId(), support.getGraduationRequirementId(), null);
        if (support.getSupportWeight() == null) {
            support.setSupportWeight(BigDecimal.ZERO);
        }
        EntityAuditSupport.touchCreate(support);
        courseSupportService.save(support);
        return Result.success(support);
    }

    @PutMapping("/course-supports/{id}")
    public Result<TrCourseRequirementSupport> updateCourseSupport(@PathVariable Long id, @RequestBody TrCourseRequirementSupport support) {
        TrCourseRequirementSupport current = courseSupportService.getById(id);
        if (current == null) {
            return Result.error("课程支撑关系不存在");
        }
        validateCourseSupportUnique(
                support.getProgramVersionId() != null ? support.getProgramVersionId() : current.getProgramVersionId(),
                support.getCourseId() != null ? support.getCourseId() : current.getCourseId(),
                support.getGraduationRequirementId() != null ? support.getGraduationRequirementId() : current.getGraduationRequirementId(),
                current.getId());
        BeanUtil.copyProperties(support, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        courseSupportService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/course-supports/{id}")
    public Result<Void> deleteCourseSupport(@PathVariable Long id) {
        TrCourseRequirementSupport current = courseSupportService.getById(id);
        if (current == null) {
            return Result.error("课程支撑关系不存在");
        }
        EntityAuditSupport.touchDelete(current);
        courseSupportService.updateById(current);
        return Result.success();
    }

    private <T> QueryWrapper<T> activeWrapper() {
        return new QueryWrapper<T>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private void validateVersionUnique(Long majorId, String versionNo, Long currentId) {
        if (majorId == null) {
            throw new IllegalArgumentException("专业ID不能为空");
        }
        if (!StringUtils.hasText(versionNo)) {
            throw new IllegalArgumentException("方案版本号不能为空");
        }
        long count = versionService.count(this.<TrProgramVersion>activeWrapper()
                .eq("major_id", majorId)
                .eq("version_no", versionNo)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("同一专业下方案版本号已存在");
        }
    }

    private void validateTargetCodeUnique(Long programVersionId, String targetCode, Long currentId) {
        requireActiveVersion(programVersionId);
        if (!StringUtils.hasText(targetCode)) {
            throw new IllegalArgumentException("培养目标编号不能为空");
        }
        long count = targetService.count(this.<TrProgramTarget>activeWrapper()
                .eq("program_version_id", programVersionId)
                .eq("target_code", targetCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("培养目标编号已存在");
        }
    }

    private void validateRequirementCodeUnique(Long programVersionId, String requirementCode, Long currentId) {
        requireActiveVersion(programVersionId);
        if (!StringUtils.hasText(requirementCode)) {
            throw new IllegalArgumentException("毕业要求编号不能为空");
        }
        long count = requirementService.count(this.<TrGraduationRequirement>activeWrapper()
                .eq("program_version_id", programVersionId)
                .eq("requirement_code", requirementCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("毕业要求编号已存在");
        }
    }

    private void validateIndicatorCodeUnique(Long graduationRequirementId, String indicatorCode, Long currentId) {
        requireActiveRequirement(graduationRequirementId);
        if (!StringUtils.hasText(indicatorCode)) {
            throw new IllegalArgumentException("指标点编号不能为空");
        }
        long count = indicatorPointService.count(this.<TrRequirementIndicatorPoint>activeWrapper()
                .eq("graduation_requirement_id", graduationRequirementId)
                .eq("indicator_code", indicatorCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("指标点编号已存在");
        }
    }

    private void validateTargetSupportUnique(Long programTargetId, Long graduationRequirementId, Long currentId) {
        if (programTargetId == null || graduationRequirementId == null) {
            throw new IllegalArgumentException("培养目标支撑关系字段不能为空");
        }
        TrProgramTarget target = requireActiveTarget(programTargetId);
        TrGraduationRequirement requirement = requireActiveRequirement(graduationRequirementId);
        if (!Objects.equals(target.getProgramVersionId(), requirement.getProgramVersionId())) {
            throw new IllegalArgumentException("培养目标与毕业要求必须属于同一方案版本");
        }
        long count = targetSupportService.count(this.<TrTargetRequirementSupport>activeWrapper()
                .eq("program_target_id", programTargetId)
                .eq("graduation_requirement_id", graduationRequirementId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("培养目标与毕业要求的支撑关系已存在");
        }
    }

    private void validateIndicatorSupportUnique(Long graduationRequirementId, Long indicatorPointId, Long currentId) {
        if (graduationRequirementId == null || indicatorPointId == null) {
            throw new IllegalArgumentException("毕业要求与指标点支撑字段不能为空");
        }
        TrGraduationRequirement requirement = requireActiveRequirement(graduationRequirementId);
        TrRequirementIndicatorPoint indicatorPoint = requireActiveIndicatorPoint(indicatorPointId);
        if (!Objects.equals(indicatorPoint.getGraduationRequirementId(), requirement.getId())) {
            throw new IllegalArgumentException("指标点必须归属当前毕业要求");
        }
        long count = indicatorSupportService.count(this.<TrRequirementIndicatorSupport>activeWrapper()
                .eq("graduation_requirement_id", graduationRequirementId)
                .eq("indicator_point_id", indicatorPointId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("毕业要求与指标点支撑关系已存在");
        }
    }

    private void validateProgramCourseUnique(Long programVersionId, Long courseId, Long currentId) {
        requireActiveVersion(programVersionId);
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        long count = programCourseService.count(this.<TrProgramCourse>activeWrapper()
                .eq("program_version_id", programVersionId)
                .eq("course_id", courseId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("该方案版本内课程已存在");
        }
    }

    private void validateCourseSupportUnique(Long programVersionId, Long courseId, Long graduationRequirementId, Long currentId) {
        requireActiveVersion(programVersionId);
        if (courseId == null || graduationRequirementId == null) {
            throw new IllegalArgumentException("课程支撑关系字段不能为空");
        }
        TrGraduationRequirement requirement = requireActiveRequirement(graduationRequirementId);
        if (!Objects.equals(requirement.getProgramVersionId(), programVersionId)) {
            throw new IllegalArgumentException("课程支撑的毕业要求必须属于当前方案版本");
        }
        long courseInVersionCount = programCourseService.count(this.<TrProgramCourse>activeWrapper()
                .eq("program_version_id", programVersionId)
                .eq("course_id", courseId));
        if (courseInVersionCount == 0) {
            throw new IllegalArgumentException("请先将课程加入方案后再配置支撑关系");
        }
        long count = courseSupportService.count(this.<TrCourseRequirementSupport>activeWrapper()
                .eq("program_version_id", programVersionId)
                .eq("course_id", courseId)
                .eq("graduation_requirement_id", graduationRequirementId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("课程与毕业要求的支撑关系已存在");
        }
    }

    private void validateVersionDeleteAllowed(Long versionId) {
        long taskCount = teachingTaskService.count(this.<TeachingTask>activeWrapper().eq("program_version_id", versionId));
        if (taskCount > 0) {
            throw new IllegalArgumentException("方案版本已被授课任务引用，无法删除");
        }
        long resultCount = evalGraduationRequirementResultService.count(this.<EvalGraduationRequirementResult>activeWrapper().eq("program_version_id", versionId));
        if (resultCount > 0) {
            throw new IllegalArgumentException("方案版本已被毕业要求达成结果引用，无法删除");
        }
    }

    private void validateTargetDeleteAllowed(Long targetId) {
        long supportCount = targetSupportService.count(this.<TrTargetRequirementSupport>activeWrapper().eq("program_target_id", targetId));
        if (supportCount > 0) {
            throw new IllegalArgumentException("培养目标已被支撑矩阵引用，无法删除");
        }
    }

    private void validateRequirementDeleteAllowed(Long requirementId) {
        long targetSupportCount = targetSupportService.count(this.<TrTargetRequirementSupport>activeWrapper().eq("graduation_requirement_id", requirementId));
        if (targetSupportCount > 0) {
            throw new IllegalArgumentException("毕业要求已被培养目标支撑矩阵引用，无法删除");
        }
        long indicatorSupportCount = indicatorSupportService.count(this.<TrRequirementIndicatorSupport>activeWrapper().eq("graduation_requirement_id", requirementId));
        if (indicatorSupportCount > 0) {
            throw new IllegalArgumentException("毕业要求已被指标点支撑矩阵引用，无法删除");
        }
        long courseSupportCount = courseSupportService.count(this.<TrCourseRequirementSupport>activeWrapper().eq("graduation_requirement_id", requirementId));
        if (courseSupportCount > 0) {
            throw new IllegalArgumentException("毕业要求已被课程支撑矩阵引用，无法删除");
        }
        long resultCount = evalGraduationRequirementResultService.count(this.<EvalGraduationRequirementResult>activeWrapper().eq("requirement_id", requirementId));
        if (resultCount > 0) {
            throw new IllegalArgumentException("毕业要求已被达成结果引用，无法删除");
        }
        List<Long> indicatorIds = indicatorPointService.list(this.<TrRequirementIndicatorPoint>activeWrapper()
                        .eq("graduation_requirement_id", requirementId))
                .stream()
                .map(TrRequirementIndicatorPoint::getId)
                .toList();
        if (!indicatorIds.isEmpty()) {
            long objectiveMappingCount = objectiveIndicatorPointService.count(this.<EduCourseObjectiveIndicatorPoint>activeWrapper()
                    .in("indicator_point_id", indicatorIds));
            if (objectiveMappingCount > 0) {
                throw new IllegalArgumentException("毕业要求下的指标点已被课程目标映射引用，无法删除");
            }
        }
    }

    private void validateIndicatorDeleteAllowed(Long indicatorPointId) {
        long supportCount = indicatorSupportService.count(this.<TrRequirementIndicatorSupport>activeWrapper().eq("indicator_point_id", indicatorPointId));
        if (supportCount > 0) {
            throw new IllegalArgumentException("指标点已被支撑矩阵引用，无法删除");
        }
        long objectiveMappingCount = objectiveIndicatorPointService.count(this.<EduCourseObjectiveIndicatorPoint>activeWrapper().eq("indicator_point_id", indicatorPointId));
        if (objectiveMappingCount > 0) {
            throw new IllegalArgumentException("指标点已被课程目标映射引用，无法删除");
        }
    }

    private void validateGradeIdsExist(List<Long> gradeIds) {
        if (CollectionUtils.isEmpty(gradeIds)) {
            return;
        }
        long count = orgGradeService.count(this.<OrgGrade>activeWrapper().in("id", gradeIds));
        if (count != gradeIds.size()) {
            throw new IllegalArgumentException("年级数据不存在或已删除");
        }
    }

    private void validateGradeIdsMatchVersionMajor(TrProgramVersion version, List<Long> gradeIds) {
        if (version == null || CollectionUtils.isEmpty(gradeIds) || version.getMajorId() == null) {
            return;
        }
        List<OrgGrade> grades = orgGradeService.list(this.<OrgGrade>activeWrapper().in("id", gradeIds));
        boolean hasMismatchedGrade = grades.stream()
                .anyMatch(item -> item.getMajorId() != null && !Objects.equals(item.getMajorId(), version.getMajorId()));
        if (hasMismatchedGrade) {
            throw new IllegalArgumentException("所选年级与培养方案所属专业不一致");
        }
    }

    private void validateCourseSelectable(Long courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        EduCourse course = courseService.getById(courseId);
        if (course == null || isDeleted(course.getIsDeleted())) {
            throw new IllegalArgumentException("课程不存在");
        }
        if (!Integer.valueOf(1).equals(course.getStatus())) {
            throw new IllegalArgumentException("停用课程不可再被新方案引用");
        }
    }

    private TrProgramVersion requireActiveVersion(Long versionId) {
        if (versionId == null) {
            throw new IllegalArgumentException("方案版本ID不能为空");
        }
        TrProgramVersion version = versionService.getById(versionId);
        if (version == null || isDeleted(version.getIsDeleted())) {
            throw new IllegalArgumentException("方案版本不存在");
        }
        return version;
    }

    private TrProgramTarget requireActiveTarget(Long targetId) {
        if (targetId == null) {
            throw new IllegalArgumentException("培养目标ID不能为空");
        }
        TrProgramTarget target = targetService.getById(targetId);
        if (target == null || isDeleted(target.getIsDeleted())) {
            throw new IllegalArgumentException("培养目标不存在");
        }
        return target;
    }

    private TrGraduationRequirement requireActiveRequirement(Long requirementId) {
        if (requirementId == null) {
            throw new IllegalArgumentException("毕业要求ID不能为空");
        }
        TrGraduationRequirement requirement = requirementService.getById(requirementId);
        if (requirement == null || isDeleted(requirement.getIsDeleted())) {
            throw new IllegalArgumentException("毕业要求不存在");
        }
        return requirement;
    }

    private TrRequirementIndicatorPoint requireActiveIndicatorPoint(Long indicatorPointId) {
        if (indicatorPointId == null) {
            throw new IllegalArgumentException("指标点ID不能为空");
        }
        TrRequirementIndicatorPoint indicatorPoint = indicatorPointService.getById(indicatorPointId);
        if (indicatorPoint == null || isDeleted(indicatorPoint.getIsDeleted())) {
            throw new IllegalArgumentException("指标点不存在");
        }
        return indicatorPoint;
    }

    private boolean isDeleted(Integer isDeleted) {
        return isDeleted != null && isDeleted != 0;
    }

    private <T> void markDeleted(List<T> records, Object service) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (T item : records) {
            EntityAuditSupport.touchDelete(item);
        }
        if (service instanceof TrProgramTargetService targetServiceImpl) {
            targetServiceImpl.updateBatchById((List<TrProgramTarget>) records);
        } else if (service instanceof TrGraduationRequirementService requirementServiceImpl) {
            requirementServiceImpl.updateBatchById((List<TrGraduationRequirement>) records);
        } else if (service instanceof TrRequirementIndicatorPointService indicatorPointServiceImpl) {
            indicatorPointServiceImpl.updateBatchById((List<TrRequirementIndicatorPoint>) records);
        } else if (service instanceof TrTargetRequirementSupportService targetSupportServiceImpl) {
            targetSupportServiceImpl.updateBatchById((List<TrTargetRequirementSupport>) records);
        } else if (service instanceof TrRequirementIndicatorSupportService indicatorSupportServiceImpl) {
            indicatorSupportServiceImpl.updateBatchById((List<TrRequirementIndicatorSupport>) records);
        } else if (service instanceof TrProgramCourseService programCourseServiceImpl) {
            programCourseServiceImpl.updateBatchById((List<TrProgramCourse>) records);
        } else if (service instanceof TrCourseRequirementSupportService courseSupportServiceImpl) {
            courseSupportServiceImpl.updateBatchById((List<TrCourseRequirementSupport>) records);
        } else if (service instanceof TrProgramApplyGradeService applyGradeServiceImpl) {
            applyGradeServiceImpl.updateBatchById((List<TrProgramApplyGrade>) records);
        }
    }
}
