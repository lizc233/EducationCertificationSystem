package com.educationcertificationsystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.dto.LookupOption;
import com.educationcertificationsystem.entity.*;
import com.educationcertificationsystem.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lookups")
@RequiredArgsConstructor
public class LookupController {

    private final OrgCollegeService orgCollegeService;
    private final OrgMajorService orgMajorService;
    private final OrgGradeService orgGradeService;
    private final OrgClassService orgClassService;
    private final EduCourseService eduCourseService;
    private final EduTeacherService eduTeacherService;
    private final EduSemesterService eduSemesterService;
    private final EduStudentService eduStudentService;
    private final TrProgramVersionService trProgramVersionService;
    private final TrProgramTargetService trProgramTargetService;
    private final TrGraduationRequirementService trGraduationRequirementService;
    private final TrRequirementIndicatorPointService trRequirementIndicatorPointService;
    private final EduCourseObjectiveService eduCourseObjectiveService;
    private final EduCourseAssessmentMethodService eduCourseAssessmentMethodService;

    @GetMapping("/colleges")
    public Result<List<LookupOption>> colleges() {
        return Result.success(orgCollegeService.list(new QueryWrapper<OrgCollege>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("sort_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getCollegeName() == null ? item.getCollegeCode() : item.getCollegeName()))
                .toList());
    }

    @GetMapping("/majors")
    public Result<List<LookupOption>> majors() {
        return Result.success(orgMajorService.list(new QueryWrapper<OrgMajor>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("sort_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getMajorName() == null ? item.getMajorCode() : item.getMajorName()))
                .toList());
    }

    @GetMapping("/grades")
    public Result<List<LookupOption>> grades() {
        return Result.success(orgGradeService.list(new QueryWrapper<OrgGrade>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByDesc("grade_year")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getGradeYear() == null ? String.valueOf(item.getId()) : String.valueOf(item.getGradeYear())))
                .toList());
    }

    @GetMapping("/classes")
    public Result<List<LookupOption>> classes() {
        return Result.success(orgClassService.list(new QueryWrapper<OrgClass>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("class_code")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getClassName() == null ? item.getClassCode() : item.getClassName()))
                .toList());
    }

    @GetMapping("/courses")
    public Result<List<LookupOption>> courses() {
        return Result.success(eduCourseService.list(new QueryWrapper<EduCourse>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("course_code")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getCourseName() == null ? item.getCourseCode() : item.getCourseName()))
                .toList());
    }

    @GetMapping("/teachers")
    public Result<List<LookupOption>> teachers() {
        return Result.success(eduTeacherService.list(new QueryWrapper<EduTeacher>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("teacher_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getTeacherNo()))
                .toList());
    }

    @GetMapping("/semesters")
    public Result<List<LookupOption>> semesters() {
        return Result.success(eduSemesterService.list(new QueryWrapper<EduSemester>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByDesc("semester_code")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getSemesterName() == null ? item.getSemesterCode() : item.getSemesterName()))
                .toList());
    }

    @GetMapping("/students")
    public Result<List<LookupOption>> students() {
        return Result.success(eduStudentService.list(new QueryWrapper<EduStudent>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("student_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getStudentNo()))
                .toList());
    }

    @GetMapping("/program-versions")
    public Result<List<LookupOption>> programVersions() {
        return Result.success(trProgramVersionService.list(new QueryWrapper<TrProgramVersion>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByDesc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getVersionName() == null ? item.getVersionNo() : item.getVersionName()))
                .toList());
    }

    @GetMapping("/targets")
    public Result<List<LookupOption>> targets() {
        return Result.success(trProgramTargetService.list(new QueryWrapper<TrProgramTarget>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("sort_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getTargetName() == null ? item.getTargetCode() : item.getTargetName()))
                .toList());
    }

    @GetMapping("/graduation-requirements")
    public Result<List<LookupOption>> graduationRequirements() {
        return Result.success(trGraduationRequirementService.list(new QueryWrapper<TrGraduationRequirement>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("sort_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getRequirementName() == null ? item.getRequirementCode() : item.getRequirementName()))
                .toList());
    }

    @GetMapping("/indicator-points")
    public Result<List<LookupOption>> indicatorPoints() {
        return Result.success(trRequirementIndicatorPointService.list(new QueryWrapper<TrRequirementIndicatorPoint>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("sort_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getIndicatorName() == null ? item.getIndicatorCode() : item.getIndicatorName()))
                .toList());
    }

    @GetMapping("/course-objectives")
    public Result<List<LookupOption>> courseObjectives() {
        return Result.success(eduCourseObjectiveService.list(new QueryWrapper<EduCourseObjective>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("sort_no")
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getObjectiveName() == null ? item.getObjectiveCode() : item.getObjectiveName()))
                .toList());
    }

    @GetMapping("/assessment-methods")
    public Result<List<LookupOption>> assessmentMethods() {
        return Result.success(eduCourseAssessmentMethodService.list(new QueryWrapper<EduCourseAssessmentMethod>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByAsc("id"))
                .stream()
                .map(item -> new LookupOption(item.getId(), item.getMethodName() == null ? item.getMethodCode() : item.getMethodName()))
                .toList());
    }
}
