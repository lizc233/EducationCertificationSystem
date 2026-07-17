package com.educationcertificationsystem.course.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.service.CourseEvidenceMaterialService;
import com.educationcertificationsystem.course.service.CourseResourceService;
import com.educationcertificationsystem.file.service.FileStorageService;
import com.educationcertificationsystem.model.entity.CourseEvidenceMaterial;
import com.educationcertificationsystem.model.entity.CourseResource;
import com.educationcertificationsystem.model.entity.SysFile;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ResourceController {

    private static final String BIZ_TYPE_COURSE_RESOURCE = "COURSE_RESOURCE";
    private static final String BIZ_TYPE_EVIDENCE_MATERIAL = "EVIDENCE_MATERIAL";

    private final CourseResourceService courseResourceService;
    private final CourseEvidenceMaterialService evidenceMaterialService;
    private final FileStorageService fileStorageService;

    @GetMapping("/api/course-resources")
    public Result<Page<CourseResource>> resources(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) Long courseId,
                                                  @RequestParam(required = false) Long taskId,
                                                  @RequestParam(required = false) String resourceType,
                                                  @RequestParam(required = false) String keyword) {
        QueryWrapper<CourseResource> wrapper = buildResourceWrapper(courseId, taskId, resourceType, keyword);
        wrapper.orderByDesc("id");
        return Result.success(courseResourceService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/api/course-resources/export")
    public ResponseEntity<ByteArrayResource> exportResources(@RequestParam(required = false) Long courseId,
                                                             @RequestParam(required = false) Long taskId,
                                                             @RequestParam(required = false) String resourceType,
                                                             @RequestParam(required = false) String keyword) {
        List<CourseResource> records = courseResourceService.list(buildResourceWrapper(courseId, taskId, resourceType, keyword).orderByDesc("id"));
        List<List<?>> rows = new ArrayList<>();
        for (CourseResource record : records) {
            rows.add(List.of(
                    record.getId(),
                    record.getCourseId(),
                    nullable(record.getTaskId()),
                    nullable(record.getResourceType()),
                    nullable(record.getResourceName()),
                    nullable(record.getFileId()),
                    nullable(record.getVisibleScopeType()),
                    nullable(record.getVisibleScopeId()),
                    nullable(record.getPublishStatus()),
                    nullable(record.getRemark())
            ));
        }
        return CsvExportSupport.csv("course-resources.csv",
                List.of("id", "courseId", "taskId", "resourceType", "resourceName", "fileId",
                        "visibleScopeType", "visibleScopeId", "publishStatus", "remark"),
                rows);
    }

    @PostMapping("/api/course-resources")
    @Transactional
    public Result<CourseResource> createResource(@RequestBody CourseResource resource) {
        SysFile file = requireFile(resource.getFileId(), "课程资源必须先上传文件");
        if (!StringUtils.hasText(resource.getResourceName())) {
            resource.setResourceName(file.getOriginalName());
        }
        if (resource.getPublishStatus() == null) {
            resource.setPublishStatus(0);
        }
        EntityAuditSupport.touchCreate(resource);
        courseResourceService.save(resource);
        fileStorageService.bindFile(file.getId(), BIZ_TYPE_COURSE_RESOURCE, resource.getId());
        return Result.success(resource);
    }

    @PutMapping("/api/course-resources/{id}")
    @Transactional
    public Result<CourseResource> updateResource(@PathVariable Long id, @RequestBody CourseResource resource) {
        CourseResource current = courseResourceService.getById(id);
        if (current == null) {
            return Result.error("课程资源不存在");
        }
        Long oldFileId = current.getFileId();
        BeanUtil.copyProperties(resource, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        SysFile file = requireFile(current.getFileId(), "课程资源必须绑定文件");
        if (!StringUtils.hasText(current.getResourceName())) {
            current.setResourceName(file.getOriginalName());
        }
        EntityAuditSupport.touchUpdate(current);
        courseResourceService.updateById(current);
        fileStorageService.bindFile(file.getId(), BIZ_TYPE_COURSE_RESOURCE, current.getId());
        if (oldFileId != null && !Objects.equals(oldFileId, current.getFileId())) {
            fileStorageService.deleteFile(oldFileId);
        }
        return Result.success(current);
    }

    @DeleteMapping("/api/course-resources/{id}")
    @Transactional
    public Result<Void> deleteResource(@PathVariable Long id) {
        CourseResource current = courseResourceService.getById(id);
        if (current == null) {
            return Result.error("课程资源不存在");
        }
        Long fileId = current.getFileId();
        EntityAuditSupport.touchDelete(current);
        courseResourceService.updateById(current);
        if (fileId != null) {
            fileStorageService.deleteFile(fileId);
        }
        return Result.success();
    }

    @PostMapping("/api/course-resources/{id}/publish")
    @Transactional
    public Result<CourseResource> publishResource(@PathVariable Long id, @RequestParam Integer publishStatus) {
        CourseResource current = courseResourceService.getById(id);
        if (current == null) {
            return Result.error("课程资源不存在");
        }
        current.setPublishStatus(publishStatus);
        EntityAuditSupport.touchUpdate(current);
        courseResourceService.updateById(current);
        return Result.success(current);
    }

    @GetMapping("/api/evidence-materials")
    public Result<Page<CourseEvidenceMaterial>> materials(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "10") long size,
                                                          @RequestParam(required = false) Long taskId,
                                                          @RequestParam(required = false) Long methodId,
                                                          @RequestParam(required = false) String reviewStatus,
                                                          @RequestParam(required = false) String keyword) {
        QueryWrapper<CourseEvidenceMaterial> wrapper = buildMaterialWrapper(taskId, methodId, reviewStatus, keyword);
        wrapper.orderByDesc("id");
        return Result.success(evidenceMaterialService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/api/evidence-materials/export")
    public ResponseEntity<ByteArrayResource> exportMaterials(@RequestParam(required = false) Long taskId,
                                                             @RequestParam(required = false) Long methodId,
                                                             @RequestParam(required = false) String reviewStatus,
                                                             @RequestParam(required = false) String keyword) {
        List<CourseEvidenceMaterial> records = evidenceMaterialService.list(buildMaterialWrapper(taskId, methodId, reviewStatus, keyword).orderByDesc("id"));
        List<List<?>> rows = new ArrayList<>();
        for (CourseEvidenceMaterial record : records) {
            rows.add(List.of(
                    record.getId(),
                    record.getTaskId(),
                    record.getMethodId(),
                    nullable(record.getMaterialType()),
                    nullable(record.getFileId()),
                    nullable(record.getSourceStudentId()),
                    nullable(record.getReviewStatus()),
                    nullable(record.getReviewUserId()),
                    nullable(record.getReviewComment()),
                    nullable(record.getRemark())
            ));
        }
        return CsvExportSupport.csv("evidence-materials.csv",
                List.of("id", "taskId", "methodId", "materialType", "fileId", "sourceStudentId",
                        "reviewStatus", "reviewUserId", "reviewComment", "remark"),
                rows);
    }

    @PostMapping("/api/evidence-materials")
    @Transactional
    public Result<CourseEvidenceMaterial> createMaterial(@RequestBody CourseEvidenceMaterial material) {
        if (!StringUtils.hasText(material.getReviewStatus())) {
            material.setReviewStatus("PENDING");
        }
        SysFile file = requireFile(material.getFileId(), "证据材料必须先上传文件");
        EntityAuditSupport.touchCreate(material);
        evidenceMaterialService.save(material);
        fileStorageService.bindFile(file.getId(), BIZ_TYPE_EVIDENCE_MATERIAL, material.getId());
        return Result.success(material);
    }

    @PostMapping("/api/evidence-materials/batch")
    @Transactional
    public Result<List<CourseEvidenceMaterial>> createMaterialBatch(@RequestBody List<CourseEvidenceMaterial> materials) {
        for (CourseEvidenceMaterial material : materials) {
            if (!StringUtils.hasText(material.getReviewStatus())) {
                material.setReviewStatus("PENDING");
            }
            SysFile file = requireFile(material.getFileId(), "证据材料必须先上传文件");
            EntityAuditSupport.touchCreate(material);
            evidenceMaterialService.save(material);
            fileStorageService.bindFile(file.getId(), BIZ_TYPE_EVIDENCE_MATERIAL, material.getId());
        }
        return Result.success(materials);
    }

    @PutMapping("/api/evidence-materials/{id}")
    @Transactional
    public Result<CourseEvidenceMaterial> updateMaterial(@PathVariable Long id, @RequestBody CourseEvidenceMaterial material) {
        CourseEvidenceMaterial current = evidenceMaterialService.getById(id);
        if (current == null) {
            return Result.error("考核证据材料不存在");
        }
        Long oldFileId = current.getFileId();
        BeanUtil.copyProperties(material, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        SysFile file = requireFile(current.getFileId(), "证据材料必须绑定文件");
        EntityAuditSupport.touchUpdate(current);
        evidenceMaterialService.updateById(current);
        fileStorageService.bindFile(file.getId(), BIZ_TYPE_EVIDENCE_MATERIAL, current.getId());
        if (oldFileId != null && !Objects.equals(oldFileId, current.getFileId())) {
            fileStorageService.deleteFile(oldFileId);
        }
        return Result.success(current);
    }

    @DeleteMapping("/api/evidence-materials/{id}")
    @Transactional
    public Result<Void> deleteMaterial(@PathVariable Long id) {
        CourseEvidenceMaterial current = evidenceMaterialService.getById(id);
        if (current == null) {
            return Result.error("考核证据材料不存在");
        }
        Long fileId = current.getFileId();
        EntityAuditSupport.touchDelete(current);
        evidenceMaterialService.updateById(current);
        if (fileId != null) {
            fileStorageService.deleteFile(fileId);
        }
        return Result.success();
    }

    @PostMapping("/api/evidence-materials/{id}/review")
    @Transactional
    public Result<CourseEvidenceMaterial> reviewMaterial(@PathVariable Long id,
                                                         @RequestParam String status,
                                                         @RequestParam(required = false) String comment) {
        CourseEvidenceMaterial current = evidenceMaterialService.getById(id);
        if (current == null) {
            return Result.error("考核证据材料不存在");
        }
        current.setReviewStatus(status);
        current.setReviewComment(comment);
        EntityAuditSupport.touchUpdate(current);
        evidenceMaterialService.updateById(current);
        return Result.success(current);
    }

    private QueryWrapper<CourseResource> buildResourceWrapper(Long courseId, Long taskId, String resourceType, String keyword) {
        QueryWrapper<CourseResource> wrapper = activeWrapper();
        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        if (taskId != null) {
            wrapper.eq("task_id", taskId);
        }
        if (StringUtils.hasText(resourceType)) {
            wrapper.eq("resource_type", resourceType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("resource_name", keyword).or().like("resource_desc", keyword));
        }
        return wrapper;
    }

    private QueryWrapper<CourseEvidenceMaterial> buildMaterialWrapper(Long taskId, Long methodId, String reviewStatus, String keyword) {
        QueryWrapper<CourseEvidenceMaterial> wrapper = activeWrapper();
        if (taskId != null) {
            wrapper.eq("task_id", taskId);
        }
        if (methodId != null) {
            wrapper.eq("method_id", methodId);
        }
        if (StringUtils.hasText(reviewStatus)) {
            wrapper.eq("review_status", reviewStatus);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like("material_type", keyword).or().like("remark", keyword));
        }
        return wrapper;
    }

    private <T> QueryWrapper<T> activeWrapper() {
        return new QueryWrapper<T>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }

    private SysFile requireFile(Long fileId, String message) {
        if (fileId == null) {
            throw new IllegalArgumentException(message);
        }
        return fileStorageService.requireFile(fileId);
    }

    private Object nullable(Object value) {
        return value == null ? "" : value;
    }
}
