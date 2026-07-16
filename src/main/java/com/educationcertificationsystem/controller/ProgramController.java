package com.educationcertificationsystem.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.dto.ProgramVersionCopyRequest;
import com.educationcertificationsystem.entity.*;
import com.educationcertificationsystem.service.*;
import com.educationcertificationsystem.support.EntityAuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
        applyGradeService.remove(new QueryWrapper<TrProgramApplyGrade>().eq("program_version_id", id));
        if (!CollectionUtils.isEmpty(gradeIds)) {
            List<TrProgramApplyGrade> rows = gradeIds.stream().filter(Objects::nonNull).map(gradeId -> {
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
        EntityAuditSupport.touchDelete(current);
        indicatorPointService.updateById(current);
        markDeleted(indicatorSupportService.list(new QueryWrapper<TrRequirementIndicatorSupport>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("indicator_point_id", id)), indicatorSupportService);
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
