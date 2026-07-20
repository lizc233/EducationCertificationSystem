package com.educationcertificationsystem.controller;

import com.educationcertificationsystem.auth.CurrentUserContext;
import com.educationcertificationsystem.auth.RequireRoles;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.dto.selection.CourseSelectionTaskSaveRequest;
import com.educationcertificationsystem.service.CourseSelectionService;
import com.educationcertificationsystem.vo.selection.CourseSelectionAdminTaskVO;
import com.educationcertificationsystem.vo.selection.CourseSelectionRecordVO;
import com.educationcertificationsystem.vo.selection.CourseSelectionStudentTaskVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/course-selection")
public class CourseSelectionController {

    private final CourseSelectionService courseSelectionService;

    public CourseSelectionController(CourseSelectionService courseSelectionService) {
        this.courseSelectionService = courseSelectionService;
    }

    @GetMapping("/admin/tasks")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<List<CourseSelectionAdminTaskVO>> adminTasks(
        @RequestParam(defaultValue = "") String term,
        @RequestParam(defaultValue = "") String status,
        @RequestParam(defaultValue = "") String keyword
    ) {
        return ApiResponse.success(courseSelectionService.listAdminTasks(term, status, keyword));
    }

    @PostMapping("/admin/tasks")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<CourseSelectionAdminTaskVO> createTask(@Valid @RequestBody CourseSelectionTaskSaveRequest request) {
        return ApiResponse.success(courseSelectionService.createTask(request));
    }

    @PutMapping("/admin/tasks/{id}")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<CourseSelectionAdminTaskVO> updateTask(@PathVariable Long id, @Valid @RequestBody CourseSelectionTaskSaveRequest request) {
        return ApiResponse.success(courseSelectionService.updateTask(id, request));
    }

    @PostMapping("/admin/tasks/{id}/close")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<Void> closeTask(@PathVariable Long id) {
        courseSelectionService.closeTask(id);
        return ApiResponse.success();
    }

    @DeleteMapping("/admin/tasks/{id}")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        courseSelectionService.deleteTask(id);
        return ApiResponse.success();
    }

    @GetMapping("/admin/tasks/{id}/students")
    @RequireRoles({RoleConstants.SUPER_ADMIN})
    public ApiResponse<List<CourseSelectionRecordVO>> taskStudents(@PathVariable Long id) {
        return ApiResponse.success(courseSelectionService.listTaskStudents(id));
    }

    @GetMapping("/student/tasks")
    @RequireRoles({RoleConstants.STUDENT})
    public ApiResponse<List<CourseSelectionStudentTaskVO>> studentTasks(
        @RequestParam(defaultValue = "") String term,
        @RequestParam(defaultValue = "") String keyword
    ) {
        Long userId = CurrentUserContext.require().getUserId();
        return ApiResponse.success(courseSelectionService.listStudentTasks(userId, term, keyword));
    }

    @PostMapping("/student/tasks/{id}/select")
    @RequireRoles({RoleConstants.STUDENT})
    public ApiResponse<Void> selectTask(@PathVariable Long id) {
        courseSelectionService.selectTask(id, CurrentUserContext.require().getUserId());
        return ApiResponse.success();
    }

    @DeleteMapping("/student/tasks/{id}/select")
    @RequireRoles({RoleConstants.STUDENT})
    public ApiResponse<Void> dropTask(@PathVariable Long id) {
        courseSelectionService.dropTask(id, CurrentUserContext.require().getUserId());
        return ApiResponse.success();
    }
}
