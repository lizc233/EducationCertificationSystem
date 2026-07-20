package com.educationcertificationsystem.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.course.service.*;
import com.educationcertificationsystem.model.dto.LookupOption;
import com.educationcertificationsystem.model.entity.*;
import com.educationcertificationsystem.org.service.OrgClassService;
import com.educationcertificationsystem.org.service.OrgCollegeService;
import com.educationcertificationsystem.org.service.OrgGradeService;
import com.educationcertificationsystem.org.service.OrgMajorService;
import com.educationcertificationsystem.program.service.TrGraduationRequirementService;
import com.educationcertificationsystem.program.service.TrProgramTargetService;
import com.educationcertificationsystem.program.service.TrProgramVersionService;
import com.educationcertificationsystem.program.service.TrRequirementIndicatorPointService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final JdbcTemplate jdbcTemplate;

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
        boolean hasMajorIdColumn = hasOrgGradeMajorIdColumn();
        QueryWrapper<OrgGrade> wrapper = new QueryWrapper<OrgGrade>()
                .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted"))
                .orderByDesc("grade_year")
                .orderByAsc("id");
        if (hasMajorIdColumn) {
            wrapper.orderByAsc("major_id");
        }

        List<OrgGrade> grades = orgGradeService.list(wrapper);
        if (!hasMajorIdColumn) {
            return Result.success(grades.stream()
                    .map(item -> new LookupOption(
                            item.getId(),
                            item.getGradeYear() == null ? String.valueOf(item.getId()) : item.getGradeYear() + "级"))
                    .toList());
        }

        Map<Long, String> majorNameMap = orgMajorService.list(new QueryWrapper<OrgMajor>()
                        .and(w -> w.eq("is_deleted", 0).or().isNull("is_deleted")))
                .stream()
                .collect(Collectors.toMap(OrgMajor::getId, item -> item.getMajorName() == null ? item.getMajorCode() : item.getMajorName()));

        return Result.success(grades.stream()
                .map(item -> {
                    String gradeLabel = item.getGradeYear() == null ? String.valueOf(item.getId()) : item.getGradeYear() + "级";
                    String majorName = majorNameMap.get(item.getMajorId());
                    String label = (majorName == null || majorName.isBlank()) ? gradeLabel : gradeLabel + " / " + majorName;
                    return new LookupOption(item.getId(), label, item.getMajorId());
                })
                .toList());
    }

    private boolean hasOrgGradeMajorIdColumn() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'org_grade'
                  AND COLUMN_NAME = 'major_id'
                """,
                Integer.class);
        return count != null && count > 0;
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
