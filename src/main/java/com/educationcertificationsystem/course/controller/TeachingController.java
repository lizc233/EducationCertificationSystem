package com.educationcertificationsystem.course.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.entity.TeachingTask;
import com.educationcertificationsystem.course.service.TeachingTaskService;
import com.educationcertificationsystem.support.EntityAuditSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/teaching")
@RequiredArgsConstructor
public class TeachingController {

    private final TeachingTaskService teachingTaskService;

    @GetMapping("/tasks")
    public Result<Page<TeachingTask>> tasks(@RequestParam(defaultValue = "1") long page,
                                            @RequestParam(defaultValue = "10") long size,
                                            @RequestParam(required = false) Long semesterId,
                                            @RequestParam(required = false) Long courseId,
                                            @RequestParam(required = false) Long classId,
                                            @RequestParam(required = false) Long teacherId,
                                            @RequestParam(required = false) String taskStatus,
                                            @RequestParam(required = false) String keyword) {
        QueryWrapper<TeachingTask> wrapper = activeWrapper();
        if (semesterId != null) {
            wrapper.eq("semester_id", semesterId);
        }
        if (courseId != null) {
            wrapper.eq("course_id", courseId);
        }
        if (classId != null) {
            wrapper.eq("class_id", classId);
        }
        if (teacherId != null) {
            wrapper.eq("teacher_id", teacherId);
        }
        if (taskStatus != null && !taskStatus.isBlank()) {
            wrapper.eq("task_status", taskStatus);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like("task_code", keyword).or().like("schedule_desc", keyword).or().like("remark", keyword));
        }
        wrapper.orderByDesc("id");
        return Result.success(teachingTaskService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/tasks/{id}")
    public Result<TeachingTask> task(@PathVariable Long id) {
        return Result.success(teachingTaskService.getById(id));
    }

    @PostMapping("/tasks")
    public Result<TeachingTask> createTask(@RequestBody TeachingTask task) {
        if (task.getTaskStatus() == null || task.getTaskStatus().isBlank()) {
            task.setTaskStatus("DRAFT");
        }
        if (task.getTaskCode() == null || task.getTaskCode().isBlank()) {
            task.setTaskCode("TT-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8));
        }
        EntityAuditSupport.touchCreate(task);
        teachingTaskService.save(task);
        return Result.success(task);
    }

    @PutMapping("/tasks/{id}")
    public Result<TeachingTask> updateTask(@PathVariable Long id, @RequestBody TeachingTask task) {
        TeachingTask current = teachingTaskService.getById(id);
        if (current == null) {
            return Result.error("授课任务不存在");
        }
        BeanUtil.copyProperties(task, current, CopyOptions.create().setIgnoreNullValue(true)
                .setIgnoreProperties("id", "createdAt", "updatedAt", "isDeleted"));
        EntityAuditSupport.touchUpdate(current);
        teachingTaskService.updateById(current);
        return Result.success(current);
    }

    @DeleteMapping("/tasks/{id}")
    @Transactional
    public Result<Void> deleteTask(@PathVariable Long id) {
        TeachingTask current = teachingTaskService.getById(id);
        if (current == null) {
            return Result.error("授课任务不存在");
        }
        EntityAuditSupport.touchDelete(current);
        teachingTaskService.updateById(current);
        return Result.success();
    }

    @PostMapping("/tasks/{id}/status")
    public Result<TeachingTask> updateStatus(@PathVariable Long id, @RequestParam String status) {
        TeachingTask current = teachingTaskService.getById(id);
        if (current == null) {
            return Result.error("授课任务不存在");
        }
        current.setTaskStatus(status);
        EntityAuditSupport.touchUpdate(current);
        teachingTaskService.updateById(current);
        return Result.success(current);
    }

    private QueryWrapper<TeachingTask> activeWrapper() {
        return new QueryWrapper<TeachingTask>().and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"));
    }
}
