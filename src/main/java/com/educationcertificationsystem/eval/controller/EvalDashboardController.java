package com.educationcertificationsystem.eval.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
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
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
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

    @GetMapping("/download/course-target-details")
    @Operation(summary = "Download course target details")
    public void downloadCourseTargetDetails(EvalDashboardQueryRequest request, HttpServletResponse response) throws IOException {
        writeExcel(
                evalDashboardService.exportCourseTargetDetails(request),
                buildCourseTargetHeaderAliases(),
                "course_target_details",
                "course-target-details",
                response);
    }

    @GetMapping("/download/graduation-requirement-details")
    @Operation(summary = "Download graduation requirement details")
    public void downloadGraduationRequirementDetails(EvalDashboardQueryRequest request, HttpServletResponse response)
            throws IOException {
        writeExcel(
                evalDashboardService.exportGraduationRequirementDetails(request),
                buildGraduationRequirementHeaderAliases(),
                "graduation_requirement_details",
                "graduation-requirement-details",
                response);
    }

    private void writeExcel(
            List<?> rows,
            LinkedHashMap<String, String> headerAliases,
            String sheetName,
            String fileNamePrefix,
            HttpServletResponse response) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = fileNamePrefix + "_" + timestamp + ".xlsx";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(ExcelUtil.XLSX_CONTENT_TYPE);
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        ExcelWriter writer = ExcelUtil.getWriter(true);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            writer.renameSheet(sheetName);
            writer.setHeaderAlias(headerAliases);
            writer.setOnlyAlias(true);
            if (rows == null || rows.isEmpty()) {
                writer.writeHeadRow(headerAliases.values());
            } else {
                writer.write(rows, true);
            }
            writer.autoSizeColumnAll();
            writer.flush(outputStream, true);
        } finally {
            writer.close();
        }
    }

    private LinkedHashMap<String, String> buildCourseTargetHeaderAliases() {
        LinkedHashMap<String, String> aliases = new LinkedHashMap<>();
        aliases.put("resultId", "Result ID");
        aliases.put("majorName", "Major");
        aliases.put("programVersionName", "Program Version");
        aliases.put("semesterName", "Semester");
        aliases.put("className", "Class");
        aliases.put("courseName", "Course");
        aliases.put("taskCode", "Task Code");
        aliases.put("objectiveCode", "Objective Code");
        aliases.put("objectiveName", "Objective Name");
        aliases.put("modelName", "Model");
        aliases.put("attainmentRate", "Attainment Rate");
        aliases.put("attainmentValue", "Attainment Value");
        aliases.put("targetValue", "Target Value");
        aliases.put("resultLevel", "Result Level");
        aliases.put("lockedFlag", "Locked Flag");
        aliases.put("calcTime", "Calc Time");
        aliases.put("remark", "Remark");
        return aliases;
    }

    private LinkedHashMap<String, String> buildGraduationRequirementHeaderAliases() {
        LinkedHashMap<String, String> aliases = new LinkedHashMap<>();
        aliases.put("resultId", "Result ID");
        aliases.put("majorName", "Major");
        aliases.put("programVersionName", "Program Version");
        aliases.put("requirementCode", "Requirement Code");
        aliases.put("requirementName", "Requirement Name");
        aliases.put("modelName", "Model");
        aliases.put("attainmentRate", "Attainment Rate");
        aliases.put("attainmentValue", "Attainment Value");
        aliases.put("thresholdValue", "Threshold Value");
        aliases.put("warningFlag", "Warning Flag");
        aliases.put("lockFlag", "Lock Flag");
        aliases.put("calcTime", "Calc Time");
        aliases.put("remark", "Remark");
        return aliases;
    }
}
