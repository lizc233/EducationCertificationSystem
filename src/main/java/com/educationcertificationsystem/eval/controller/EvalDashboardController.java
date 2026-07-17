package com.educationcertificationsystem.eval.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.eval.service.EvalDashboardService;
import com.educationcertificationsystem.model.dto.eval.EvalDashboardQueryRequest;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardCourseTargetDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardDistributionItemVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardGraduationRequirementDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardOverviewVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardRequirementMatrixRowVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardTrendPointVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Attainment Dashboard")
@RequestMapping("/api/eval/dashboard")
public class EvalDashboardController {

    private final EvalDashboardService evalDashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Dashboard overview")
    public Result<EvalDashboardOverviewVO> overview(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.getOverview(request));
    }

    @GetMapping("/semester-trend")
    @Operation(summary = "Semester trend")
    public Result<List<EvalDashboardTrendPointVO>> semesterTrend(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.getSemesterTrend(request));
    }

    @GetMapping("/course-distribution")
    @Operation(summary = "Course distribution")
    public Result<List<EvalDashboardDistributionItemVO>> courseDistribution(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.getCourseDistribution(request));
    }

    @GetMapping("/requirement-distribution")
    @Operation(summary = "Requirement distribution")
    public Result<List<EvalDashboardDistributionItemVO>> requirementDistribution(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.getRequirementDistribution(request));
    }

    @GetMapping("/requirement-matrix")
    @Operation(summary = "Requirement matrix")
    public Result<List<EvalDashboardRequirementMatrixRowVO>> requirementMatrix(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.getRequirementMatrix(request));
    }

    @GetMapping("/course-target-details")
    @Operation(summary = "Course target detail table")
    public Result<Page<EvalDashboardCourseTargetDetailVO>> courseTargetDetails(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.pageCourseTargetDetails(request));
    }

    @GetMapping("/graduation-requirement-details")
    @Operation(summary = "Graduation requirement detail table")
    public Result<Page<EvalDashboardGraduationRequirementDetailVO>> graduationRequirementDetails(
            EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.pageGraduationRequirementDetails(request));
    }

    @GetMapping("/export/course-target-details")
    @Operation(summary = "Export course target details")
    public Result<List<EvalDashboardCourseTargetDetailVO>> exportCourseTargetDetails(EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.exportCourseTargetDetails(request));
    }

    @GetMapping("/export/graduation-requirement-details")
    @Operation(summary = "Export graduation requirement details")
    public Result<List<EvalDashboardGraduationRequirementDetailVO>> exportGraduationRequirementDetails(
            EvalDashboardQueryRequest request) {
        return Result.success(evalDashboardService.exportGraduationRequirementDetails(request));
    }
}
