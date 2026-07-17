package com.educationcertificationsystem.course.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.service.CourseEvidenceMaterialService;
import com.educationcertificationsystem.course.service.CourseScoreBatchService;
import com.educationcertificationsystem.course.service.EduCourseAssessmentMethodService;
import com.educationcertificationsystem.course.service.EduCourseAssessmentStandardService;
import com.educationcertificationsystem.course.service.EduCourseContentObjectiveRelService;
import com.educationcertificationsystem.course.service.EduCourseContentService;
import com.educationcertificationsystem.course.service.EduCourseObjectiveIndicatorPointService;
import com.educationcertificationsystem.course.service.EduCourseObjectiveService;
import com.educationcertificationsystem.course.service.EduCourseService;
import com.educationcertificationsystem.model.entity.CourseEvidenceMaterial;
import com.educationcertificationsystem.model.entity.CourseScoreBatch;
import com.educationcertificationsystem.model.entity.EduCourse;
import com.educationcertificationsystem.model.entity.EduCourseAssessmentMethod;
import com.educationcertificationsystem.model.entity.EduCourseAssessmentStandard;
import com.educationcertificationsystem.model.entity.EduCourseContent;
import com.educationcertificationsystem.model.entity.EduCourseContentObjectiveRel;
import com.educationcertificationsystem.model.entity.EduCourseObjective;
import com.educationcertificationsystem.model.entity.EduCourseObjectiveIndicatorPoint;
import com.educationcertificationsystem.support.CsvExportSupport;
import com.educationcertificationsystem.support.EntityAuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final EduCourseService courseService;
    private final EduCourseObjectiveService objectiveService;
    private final EduCourseObjectiveIndicatorPointService objectiveIndicatorPointService;
    private final EduCourseContentService contentService;
    private final EduCourseContentObjectiveRelService contentObjectiveRelService;
    private final EduCourseAssessmentMethodService assessmentMethodService;
    private final EduCourseAssessmentStandardService assessmentStandardService;
    private final CourseScoreBatchService batchService;
    private final CourseEvidenceMaterialService evidenceMaterialService;

    @GetMapping
    public Result<Page<EduCourse>> courses(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) Long offeringUnitId,
                                           @RequestParam(required = false) Integer status) {
        QueryWrapper<EduCourse> wrapper = buildCourseWrapper(keyword, offeringUnitId, status);
        wrapper.orderByDesc("id");
        return Result.success(courseService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/export")
    public ResponseEntity<ByteArrayResource> exportCourses(@RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) Long offeringUnitId,
                                                           @RequestParam(required = false) Integer status) {
        List<EduCourse> records = courseService.list(buildCourseWrapper(keyword, offeringUnitId, status).orderByDesc("id"));
        List<List<?>> rows = new ArrayList<>();
        for (EduCourse record : records) {
            rows.add(List.of(
                    record.getId(),
                    record.getCourseCode(),
                    record.getCourseName(),
                    record.getCourseType(),
                    record.getCredit(),
                    record.getTotalHours(),
                    record.getTheoryHours(),
                    record.getPracticeHours(),
                    nullable(record.getOfferingUnitId()),
                    record.getStatus(),
                    nullable(record.getRemark())
            ));
        }
        return CsvExportSupport.csv("course-list.csv",
                List.of("id", "courseCode", "courseName", "courseType", "credit", "totalHours",
                        "theoryHours", "practiceHours", "offeringUnitId", "status", "remark"),
                rows);
    }

    @GetMapping("/{id}")
    public Result<EduCourse> course(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @PostMapping
    public Result<EduCourse> createCourse(@RequestBody EduCourse course) {
        validateCourseCodeUnique(course.getCourseCode(), null);
        if (course.getStatus() == null) {
            course.setStatus(1);
        }
        EntityAuditSupport.touchCreate(course);
        courseService.save(course);
        return Result.success(course);
    }

    @PutMapping("/{id}")
    public Result<EduCourse> updateCourse(@PathVariable Long id, @RequestBody EduCourse course) {
        EduCourse current = courseService.getById(id);
        if (current == null) {
            return Result.error("课程不存在");
        }
        validateCourseCodeUnique(defaultIfBlank(course.getCourseCode(), current.getCourseCode()), current.getId());
        BeanUtil.copyProperties(course, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        courseService.updateById(current);
        return Result.success(current);
    }

    @PostMapping("/{id}/status")
    public Result<EduCourse> updateCourseStatus(@PathVariable Long id, @RequestParam Integer status) {
        EduCourse current = courseService.getById(id);
        if (current == null) {
            return Result.error("课程不存在");
        }
        current.setStatus(status);
        EntityAuditSupport.touchUpdate(current);
        courseService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result<Void> deleteCourse(@PathVariable Long id) {
        EduCourse current = courseService.getById(id);
        if (current == null) {
            return Result.error("课程不存在");
        }
        EntityAuditSupport.touchDelete(current);
        courseService.updateById(current);

        List<Long> methodIds = assessmentMethodService.list(new QueryWrapper<EduCourseAssessmentMethod>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                        .eq("course_id", id))
                .stream().map(EduCourseAssessmentMethod::getId).toList();
        markDeleted(objectiveService.list(new QueryWrapper<EduCourseObjective>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_id", id)), objectiveService);
        markDeleted(contentService.list(new QueryWrapper<EduCourseContent>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_id", id)), contentService);
        markDeleted(assessmentMethodService.list(new QueryWrapper<EduCourseAssessmentMethod>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_id", id)), assessmentMethodService);
        markDeleted(assessmentStandardService.list(new QueryWrapper<EduCourseAssessmentStandard>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .in(!methodIds.isEmpty(), "method_id", methodIds)), assessmentStandardService);
        return Result.success();
    }

    @GetMapping("/{courseId}/objectives")
    public Result<List<EduCourseObjective>> objectives(@PathVariable Long courseId) {
        return Result.success(objectiveService.list(new QueryWrapper<EduCourseObjective>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_id", courseId)
                .orderByAsc("sort_no")
                .orderByAsc("id")));
    }

    @PostMapping("/{courseId}/objectives")
    public Result<EduCourseObjective> createObjective(@PathVariable Long courseId, @RequestBody EduCourseObjective objective) {
        objective.setCourseId(courseId);
        validateObjectiveCodeUnique(courseId, objective.getObjectiveCode(), null);
        if (objective.getEnabled() == null) {
            objective.setEnabled(1);
        }
        EntityAuditSupport.touchCreate(objective);
        objectiveService.save(objective);
        return Result.success(objective);
    }

    @PutMapping("/objectives/{id}")
    public Result<EduCourseObjective> updateObjective(@PathVariable Long id, @RequestBody EduCourseObjective objective) {
        EduCourseObjective current = objectiveService.getById(id);
        if (current == null) {
            return Result.error("课程目标不存在");
        }
        validateObjectiveCodeUnique(current.getCourseId(),
                defaultIfBlank(objective.getObjectiveCode(), current.getObjectiveCode()),
                current.getId());
        BeanUtil.copyProperties(objective, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "courseId", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        objectiveService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/objectives/{id}")
    @Transactional
    public Result<Void> deleteObjective(@PathVariable Long id) {
        EduCourseObjective current = objectiveService.getById(id);
        if (current == null) {
            return Result.error("课程目标不存在");
        }
        validateObjectiveDeleteAllowed(id);
        EntityAuditSupport.touchDelete(current);
        objectiveService.updateById(current);
        markDeleted(objectiveIndicatorPointService.list(new QueryWrapper<EduCourseObjectiveIndicatorPoint>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_objective_id", id)), objectiveIndicatorPointService);
        markDeleted(contentObjectiveRelService.list(new QueryWrapper<EduCourseContentObjectiveRel>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("objective_id", id)), contentObjectiveRelService);
        return Result.success();
    }

    @GetMapping("/objective-supports")
    public Result<List<EduCourseObjectiveIndicatorPoint>> objectiveSupports(@RequestParam(required = false) Long courseId,
                                                                            @RequestParam(required = false) Long objectiveId) {
        QueryWrapper<EduCourseObjectiveIndicatorPoint> wrapper = activeWrapper();
        if (objectiveId != null) {
            wrapper.eq("course_objective_id", objectiveId);
        } else if (courseId != null) {
            List<Long> objectiveIds = objectiveService.list(new QueryWrapper<EduCourseObjective>()
                            .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                            .eq("course_id", courseId))
                    .stream().map(EduCourseObjective::getId).toList();
            if (objectiveIds.isEmpty()) {
                return Result.success(List.of());
            }
            wrapper.in("course_objective_id", objectiveIds);
        }
        wrapper.orderByDesc("id");
        return Result.success(objectiveIndicatorPointService.list(wrapper));
    }

    @PostMapping("/objective-supports")
    public Result<EduCourseObjectiveIndicatorPoint> createObjectiveSupport(@RequestBody EduCourseObjectiveIndicatorPoint support) {
        validateObjectiveSupportUnique(support.getCourseObjectiveId(), support.getIndicatorPointId(), null);
        if (support.getSupportWeight() == null) {
            support.setSupportWeight(BigDecimal.ZERO);
        }
        EntityAuditSupport.touchCreate(support);
        objectiveIndicatorPointService.save(support);
        return Result.success(support);
    }

    @PutMapping("/objective-supports/{id}")
    public Result<EduCourseObjectiveIndicatorPoint> updateObjectiveSupport(@PathVariable Long id,
                                                                           @RequestBody EduCourseObjectiveIndicatorPoint support) {
        EduCourseObjectiveIndicatorPoint current = objectiveIndicatorPointService.getById(id);
        if (current == null) {
            return Result.error("目标映射不存在");
        }
        validateObjectiveSupportUnique(
                support.getCourseObjectiveId() != null ? support.getCourseObjectiveId() : current.getCourseObjectiveId(),
                support.getIndicatorPointId() != null ? support.getIndicatorPointId() : current.getIndicatorPointId(),
                current.getId());
        BeanUtil.copyProperties(support, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        objectiveIndicatorPointService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/objective-supports/{id}")
    public Result<Void> deleteObjectiveSupport(@PathVariable Long id) {
        EduCourseObjectiveIndicatorPoint current = objectiveIndicatorPointService.getById(id);
        if (current == null) {
            return Result.error("目标映射不存在");
        }
        EntityAuditSupport.touchDelete(current);
        objectiveIndicatorPointService.updateById(current);
        return Result.success();
    }

    @GetMapping("/{courseId}/contents")
    public Result<List<EduCourseContent>> contents(@PathVariable Long courseId) {
        return Result.success(contentService.list(new QueryWrapper<EduCourseContent>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_id", courseId)
                .orderByAsc("sort_no")
                .orderByAsc("id")));
    }

    @PostMapping("/{courseId}/contents")
    public Result<EduCourseContent> createContent(@PathVariable Long courseId, @RequestBody EduCourseContent content) {
        content.setCourseId(courseId);
        validateContentCodeUnique(courseId, content.getContentCode(), null);
        if (content.getEnabled() == null) {
            content.setEnabled(1);
        }
        EntityAuditSupport.touchCreate(content);
        contentService.save(content);
        return Result.success(content);
    }

    @PutMapping("/contents/{id}")
    public Result<EduCourseContent> updateContent(@PathVariable Long id, @RequestBody EduCourseContent content) {
        EduCourseContent current = contentService.getById(id);
        if (current == null) {
            return Result.error("教学内容不存在");
        }
        validateContentCodeUnique(current.getCourseId(),
                defaultIfBlank(content.getContentCode(), current.getContentCode()),
                current.getId());
        BeanUtil.copyProperties(content, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "courseId", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        contentService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/contents/{id}")
    @Transactional
    public Result<Void> deleteContent(@PathVariable Long id) {
        EduCourseContent current = contentService.getById(id);
        if (current == null) {
            return Result.error("教学内容不存在");
        }
        EntityAuditSupport.touchDelete(current);
        contentService.updateById(current);
        markDeleted(contentObjectiveRelService.list(new QueryWrapper<EduCourseContentObjectiveRel>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("content_id", id)), contentObjectiveRelService);
        return Result.success();
    }

    @GetMapping("/content-relations")
    public Result<List<EduCourseContentObjectiveRel>> contentRelations(@RequestParam(required = false) Long courseId,
                                                                       @RequestParam(required = false) Long contentId) {
        QueryWrapper<EduCourseContentObjectiveRel> wrapper = activeWrapper();
        if (contentId != null) {
            wrapper.eq("content_id", contentId);
        } else if (courseId != null) {
            List<Long> contentIds = contentService.list(new QueryWrapper<EduCourseContent>()
                            .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                            .eq("course_id", courseId))
                    .stream().map(EduCourseContent::getId).toList();
            if (contentIds.isEmpty()) {
                return Result.success(List.of());
            }
            wrapper.in("content_id", contentIds);
        }
        wrapper.orderByDesc("id");
        return Result.success(contentObjectiveRelService.list(wrapper));
    }

    @PostMapping("/content-relations")
    public Result<EduCourseContentObjectiveRel> createContentRelation(@RequestBody EduCourseContentObjectiveRel relation) {
        validateContentRelationUnique(relation.getContentId(), relation.getObjectiveId(), null);
        EntityAuditSupport.touchCreate(relation);
        contentObjectiveRelService.save(relation);
        return Result.success(relation);
    }

    @PutMapping("/content-relations/{id}")
    public Result<EduCourseContentObjectiveRel> updateContentRelation(@PathVariable Long id,
                                                                      @RequestBody EduCourseContentObjectiveRel relation) {
        EduCourseContentObjectiveRel current = contentObjectiveRelService.getById(id);
        if (current == null) {
            return Result.error("内容关联不存在");
        }
        validateContentRelationUnique(
                relation.getContentId() != null ? relation.getContentId() : current.getContentId(),
                relation.getObjectiveId() != null ? relation.getObjectiveId() : current.getObjectiveId(),
                current.getId());
        BeanUtil.copyProperties(relation, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        contentObjectiveRelService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/content-relations/{id}")
    public Result<Void> deleteContentRelation(@PathVariable Long id) {
        EduCourseContentObjectiveRel current = contentObjectiveRelService.getById(id);
        if (current == null) {
            return Result.error("内容关联不存在");
        }
        EntityAuditSupport.touchDelete(current);
        contentObjectiveRelService.updateById(current);
        return Result.success();
    }

    @GetMapping("/{courseId}/assessment-methods")
    public Result<List<EduCourseAssessmentMethod>> assessmentMethods(@PathVariable Long courseId) {
        return Result.success(assessmentMethodService.list(new QueryWrapper<EduCourseAssessmentMethod>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("course_id", courseId)
                .orderByAsc("id")));
    }

    @PostMapping("/{courseId}/assessment-methods")
    public Result<EduCourseAssessmentMethod> createAssessmentMethod(@PathVariable Long courseId,
                                                                    @RequestBody EduCourseAssessmentMethod method) {
        method.setCourseId(courseId);
        validateAssessmentMethodCodeUnique(courseId, method.getMethodCode(), null);
        if (method.getRatioPercent() == null) {
            return Result.error("考核方式权重不能为空");
        }
        if (method.getEnabled() == null) {
            method.setEnabled(1);
        }
        validateAssessmentWeight(method.getCourseId(), null, method.getRatioPercent(), method.getEnabled());
        EntityAuditSupport.touchCreate(method);
        assessmentMethodService.save(method);
        return Result.success(method);
    }

    @PutMapping("/assessment-methods/{id}")
    public Result<EduCourseAssessmentMethod> updateAssessmentMethod(@PathVariable Long id,
                                                                    @RequestBody EduCourseAssessmentMethod method) {
        EduCourseAssessmentMethod current = assessmentMethodService.getById(id);
        if (current == null) {
            return Result.error("考核方式不存在");
        }
        validateAssessmentMethodCodeUnique(current.getCourseId(),
                defaultIfBlank(method.getMethodCode(), current.getMethodCode()),
                current.getId());
        Integer nextEnabled = method.getEnabled() != null ? method.getEnabled() : current.getEnabled();
        BigDecimal nextRatio = method.getRatioPercent() != null ? method.getRatioPercent() : current.getRatioPercent();
        validateAssessmentWeight(current.getCourseId(), current.getId(), nextRatio, nextEnabled);
        BeanUtil.copyProperties(method, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "courseId", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        assessmentMethodService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/assessment-methods/{id}")
    @Transactional
    public Result<Void> deleteAssessmentMethod(@PathVariable Long id) {
        EduCourseAssessmentMethod current = assessmentMethodService.getById(id);
        if (current == null) {
            return Result.error("考核方式不存在");
        }
        validateAssessmentMethodDeleteAllowed(id);
        EntityAuditSupport.touchDelete(current);
        assessmentMethodService.updateById(current);
        markDeleted(assessmentStandardService.list(new QueryWrapper<EduCourseAssessmentStandard>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .eq("method_id", id)), assessmentStandardService);
        return Result.success();
    }

    @GetMapping("/assessment-standards")
    public Result<List<EduCourseAssessmentStandard>> assessmentStandards(@RequestParam(required = false) Long methodId) {
        QueryWrapper<EduCourseAssessmentStandard> wrapper = activeWrapper();
        if (methodId != null) {
            wrapper.eq("method_id", methodId);
        }
        wrapper.orderByAsc("sort_no").orderByAsc("id");
        return Result.success(assessmentStandardService.list(wrapper));
    }

    @PostMapping("/assessment-standards")
    public Result<EduCourseAssessmentStandard> createAssessmentStandard(@RequestBody EduCourseAssessmentStandard standard) {
        if (standard.getScoreMin() == null) {
            standard.setScoreMin(BigDecimal.ZERO);
        }
        if (standard.getScoreMax() == null) {
            standard.setScoreMax(new BigDecimal("100"));
        }
        validateScoreRange(standard.getScoreMin(), standard.getScoreMax());
        EntityAuditSupport.touchCreate(standard);
        assessmentStandardService.save(standard);
        return Result.success(standard);
    }

    @PutMapping("/assessment-standards/{id}")
    public Result<EduCourseAssessmentStandard> updateAssessmentStandard(@PathVariable Long id,
                                                                        @RequestBody EduCourseAssessmentStandard standard) {
        EduCourseAssessmentStandard current = assessmentStandardService.getById(id);
        if (current == null) {
            return Result.error("考核标准不存在");
        }
        validateScoreRange(
                standard.getScoreMin() != null ? standard.getScoreMin() : current.getScoreMin(),
                standard.getScoreMax() != null ? standard.getScoreMax() : current.getScoreMax());
        BeanUtil.copyProperties(standard, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "methodId", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        assessmentStandardService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/assessment-standards/{id}")
    public Result<Void> deleteAssessmentStandard(@PathVariable Long id) {
        EduCourseAssessmentStandard current = assessmentStandardService.getById(id);
        if (current == null) {
            return Result.error("考核标准不存在");
        }
        EntityAuditSupport.touchDelete(current);
        assessmentStandardService.updateById(current);
        return Result.success();
    }

    private QueryWrapper<EduCourse> buildCourseWrapper(String keyword, Long offeringUnitId, Integer status) {
        QueryWrapper<EduCourse> wrapper = activeWrapper();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("course_code", keyword)
                    .or().like("course_name", keyword)
                    .or().like("remark", keyword));
        }
        if (offeringUnitId != null) {
            wrapper.eq("offering_unit_id", offeringUnitId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        return wrapper;
    }

    private <T> QueryWrapper<T> activeWrapper() {
        return new QueryWrapper<T>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private <T> void markDeleted(List<T> records, Object service) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (T record : records) {
            EntityAuditSupport.touchDelete(record);
        }
        if (service instanceof EduCourseObjectiveService s) {
            s.updateBatchById((List<EduCourseObjective>) records);
        } else if (service instanceof EduCourseObjectiveIndicatorPointService s) {
            s.updateBatchById((List<EduCourseObjectiveIndicatorPoint>) records);
        } else if (service instanceof EduCourseContentService s) {
            s.updateBatchById((List<EduCourseContent>) records);
        } else if (service instanceof EduCourseContentObjectiveRelService s) {
            s.updateBatchById((List<EduCourseContentObjectiveRel>) records);
        } else if (service instanceof EduCourseAssessmentMethodService s) {
            s.updateBatchById((List<EduCourseAssessmentMethod>) records);
        } else if (service instanceof EduCourseAssessmentStandardService s) {
            s.updateBatchById((List<EduCourseAssessmentStandard>) records);
        }
    }

    private void validateAssessmentWeight(Long courseId, Long currentId, BigDecimal ratioPercent, Integer enabled) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (ratioPercent == null) {
            throw new IllegalArgumentException("考核方式权重不能为空");
        }
        BigDecimal activeSum = assessmentMethodService.list(new QueryWrapper<EduCourseAssessmentMethod>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                        .eq("course_id", courseId)
                        .eq("enabled", 1)
                        .ne(currentId != null, "id", currentId))
                .stream()
                .map(EduCourseAssessmentMethod::getRatioPercent)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal prospectiveSum = Integer.valueOf(0).equals(enabled) ? activeSum : activeSum.add(ratioPercent);
        if (prospectiveSum.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("考核方式权重合计不能超过100%");
        }
    }

    private void validateCourseCodeUnique(String courseCode, Long currentId) {
        if (!StringUtils.hasText(courseCode)) {
            throw new IllegalArgumentException("课程代码不能为空");
        }
        long count = courseService.count(this.<EduCourse>activeWrapper()
                .eq("course_code", courseCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("课程代码已存在");
        }
    }

    private void validateObjectiveCodeUnique(Long courseId, String objectiveCode, Long currentId) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (!StringUtils.hasText(objectiveCode)) {
            throw new IllegalArgumentException("课程目标编号不能为空");
        }
        long count = objectiveService.count(this.<EduCourseObjective>activeWrapper()
                .eq("course_id", courseId)
                .eq("objective_code", objectiveCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("课程目标编号已存在");
        }
    }

    private void validateObjectiveDeleteAllowed(Long objectiveId) {
        long scoreBatchCount = batchService.count(this.<CourseScoreBatch>activeWrapper().eq("objective_id", objectiveId));
        if (scoreBatchCount > 0) {
            throw new IllegalArgumentException("课程目标已被成绩批次引用，无法删除");
        }
    }

    private void validateObjectiveSupportUnique(Long objectiveId, Long indicatorPointId, Long currentId) {
        if (objectiveId == null || indicatorPointId == null) {
            throw new IllegalArgumentException("目标映射字段不能为空");
        }
        long count = objectiveIndicatorPointService.count(this.<EduCourseObjectiveIndicatorPoint>activeWrapper()
                .eq("course_objective_id", objectiveId)
                .eq("indicator_point_id", indicatorPointId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("课程目标与指标点映射已存在");
        }
    }

    private void validateContentCodeUnique(Long courseId, String contentCode, Long currentId) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (!StringUtils.hasText(contentCode)) {
            throw new IllegalArgumentException("教学内容编号不能为空");
        }
        long count = contentService.count(this.<EduCourseContent>activeWrapper()
                .eq("course_id", courseId)
                .eq("content_code", contentCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("教学内容编号已存在");
        }
    }

    private void validateContentRelationUnique(Long contentId, Long objectiveId, Long currentId) {
        if (contentId == null || objectiveId == null) {
            throw new IllegalArgumentException("内容关联字段不能为空");
        }
        long count = contentObjectiveRelService.count(this.<EduCourseContentObjectiveRel>activeWrapper()
                .eq("content_id", contentId)
                .eq("objective_id", objectiveId)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("教学内容与目标关联已存在");
        }
    }

    private void validateAssessmentMethodCodeUnique(Long courseId, String methodCode, Long currentId) {
        if (courseId == null) {
            throw new IllegalArgumentException("课程ID不能为空");
        }
        if (!StringUtils.hasText(methodCode)) {
            throw new IllegalArgumentException("考核方式编号不能为空");
        }
        long count = assessmentMethodService.count(this.<EduCourseAssessmentMethod>activeWrapper()
                .eq("course_id", courseId)
                .eq("method_code", methodCode)
                .ne(currentId != null, "id", currentId));
        if (count > 0) {
            throw new IllegalArgumentException("考核方式编号已存在");
        }
    }

    private void validateAssessmentMethodDeleteAllowed(Long methodId) {
        long scoreBatchCount = batchService.count(this.<CourseScoreBatch>activeWrapper().eq("method_id", methodId));
        if (scoreBatchCount > 0) {
            throw new IllegalArgumentException("考核方式已被成绩批次引用，无法删除");
        }
        long evidenceCount = evidenceMaterialService.count(this.<CourseEvidenceMaterial>activeWrapper().eq("method_id", methodId));
        if (evidenceCount > 0) {
            throw new IllegalArgumentException("考核方式已被证据材料引用，无法删除");
        }
    }

    private void validateScoreRange(BigDecimal scoreMin, BigDecimal scoreMax) {
        if (scoreMin != null && scoreMax != null && scoreMin.compareTo(scoreMax) > 0) {
            throw new IllegalArgumentException("考核标准的最小分不能大于最大分");
        }
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private Object nullable(Object value) {
        return value == null ? "" : value;
    }
}
