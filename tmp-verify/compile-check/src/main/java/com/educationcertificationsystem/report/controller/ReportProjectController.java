package com.educationcertificationsystem.report.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.dto.report.ReportChapterLockRequest;
import com.educationcertificationsystem.model.dto.report.ReportChapterSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportDraftSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportDraftUploadRequest;
import com.educationcertificationsystem.model.dto.report.ReportInitialDraftRequest;
import com.educationcertificationsystem.model.dto.report.ReportProgressSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportProjectSaveRequest;
import com.educationcertificationsystem.model.dto.report.ReportTaskAssignmentSaveRequest;
import com.educationcertificationsystem.model.entity.ReportChapter;
import com.educationcertificationsystem.model.entity.ReportDraft;
import com.educationcertificationsystem.model.entity.ReportProgressLog;
import com.educationcertificationsystem.model.entity.ReportTaskAssignment;
import com.educationcertificationsystem.model.vo.report.ReportDraftVO;
import com.educationcertificationsystem.model.vo.report.ReportProgressBoardVO;
import com.educationcertificationsystem.model.vo.report.ReportProjectDetailVO;
import com.educationcertificationsystem.model.vo.report.ReportProjectPageVO;
import com.educationcertificationsystem.report.service.ReportProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Report Project")
@RequestMapping("/api/reports/projects")
public class ReportProjectController {

    private final ReportProjectService reportProjectService;

    @GetMapping
    @Operation(summary = "Page report projects")
    public Result<Page<ReportProjectPageVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                                  @RequestParam(defaultValue = "10") long pageSize,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(required = false) Long ownerUserId,
                                                  @RequestParam(required = false) Long viewerUserId,
                                                  @RequestParam(required = false) String keyword) {
        return Result.success(reportProjectService.pageByCondition(
                pageNum, pageSize, status, ownerUserId, viewerUserId, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Report project detail")
    public Result<ReportProjectDetailVO> detail(@PathVariable Long id,
                                                @RequestParam(required = false) Long viewerUserId) {
        try {
            return Result.success(reportProjectService.getDetail(id, viewerUserId));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Get report project detail failed, id={}", id, ex);
            return Result.error("Get report project detail failed");
        }
    }

    @PostMapping
    @Transactional
    @Operation(summary = "Create report project")
    public Result<ReportProjectDetailVO> create(@RequestBody ReportProjectSaveRequest request) {
        try {
            return Result.success(reportProjectService.createProject(request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Create report project failed", ex);
            return Result.error("Create report project failed");
        }
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Update report project")
    public Result<ReportProjectDetailVO> update(@PathVariable Long id, @RequestBody ReportProjectSaveRequest request) {
        try {
            return Result.success(reportProjectService.updateProject(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Update report project failed, id={}", id, ex);
            return Result.error("Update report project failed");
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    @Operation(summary = "Delete report project")
    public Result<String> delete(@PathVariable Long id) {
        try {
            reportProjectService.deleteProject(id);
            return Result.success("Deleted successfully");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Delete report project failed, id={}", id, ex);
            return Result.error("Delete report project failed");
        }
    }

    @PutMapping("/{id}/chapters")
    @Transactional
    @Operation(summary = "Save report chapter tree")
    public Result<ReportProjectDetailVO> saveChapters(@PathVariable Long id,
                                                      @RequestBody List<ReportChapterSaveRequest> requests) {
        try {
            return Result.success(reportProjectService.saveChapterTree(id, requests));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Save report chapter tree failed, id={}", id, ex);
            return Result.error("Save report chapter tree failed");
        }
    }

    @PutMapping("/{id}/assignments")
    @Transactional
    @Operation(summary = "Save report chapter assignments")
    public Result<List<ReportTaskAssignment>> saveAssignments(@PathVariable Long id,
                                                              @RequestBody List<ReportTaskAssignmentSaveRequest> requests) {
        try {
            return Result.success(reportProjectService.saveAssignments(id, requests));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Save report assignments failed, id={}", id, ex);
            return Result.error("Save report assignments failed");
        }
    }

    @GetMapping("/chapters/{chapterId}/drafts")
    @Operation(summary = "List chapter drafts")
    public Result<List<ReportDraftVO>> listDrafts(@PathVariable Long chapterId) {
        try {
            return Result.success(reportProjectService.listDrafts(chapterId));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("List report drafts failed, chapterId={}", chapterId, ex);
            return Result.error("List report drafts failed");
        }
    }

    @PostMapping("/chapters/{chapterId}/drafts")
    @Transactional
    @Operation(summary = "Save chapter draft")
    public Result<ReportDraft> saveDraft(@PathVariable Long chapterId, @RequestBody ReportDraftSaveRequest request) {
        try {
            return Result.success(reportProjectService.saveDraft(chapterId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Save report draft failed, chapterId={}", chapterId, ex);
            return Result.error("Save report draft failed");
        }
    }

    @PostMapping("/chapters/{chapterId}/drafts/upload")
    @Transactional
    @Operation(summary = "Upload chapter draft file")
    public Result<ReportDraft> uploadDraft(@PathVariable Long chapterId,
                                           @ModelAttribute ReportDraftUploadRequest request,
                                           @RequestParam("file") MultipartFile file) {
        try {
            return Result.success(reportProjectService.uploadDraft(chapterId, request, file));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Upload report draft failed, chapterId={}", chapterId, ex);
            return Result.error("Upload report draft failed");
        }
    }

    @PostMapping("/chapters/{chapterId}/lock")
    @Transactional
    @Operation(summary = "Lock or unlock report chapter")
    public Result<ReportChapter> lockChapter(@PathVariable Long chapterId,
                                             @RequestBody(required = false) ReportChapterLockRequest request) {
        try {
            return Result.success(reportProjectService.lockChapter(chapterId, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Lock report chapter failed, chapterId={}", chapterId, ex);
            return Result.error("Lock report chapter failed");
        }
    }

    @PostMapping("/{id}/progress-logs")
    @Transactional
    @Operation(summary = "Save report progress log")
    public Result<ReportProgressLog> saveProgress(@PathVariable Long id,
                                                  @RequestBody ReportProgressSaveRequest request) {
        try {
            return Result.success(reportProjectService.saveProgress(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Save report progress failed, id={}", id, ex);
            return Result.error("Save report progress failed");
        }
    }

    @GetMapping("/{id}/progress-board")
    @Operation(summary = "Report project progress board")
    public Result<ReportProgressBoardVO> progressBoard(@PathVariable Long id) {
        try {
            return Result.success(reportProjectService.getProgressBoard(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Get report progress board failed, id={}", id, ex);
            return Result.error("Get report progress board failed");
        }
    }

    @PostMapping("/{id}/generate-drafts")
    @Transactional
    @Operation(summary = "Generate chapter initial drafts")
    public Result<ReportProjectDetailVO> generateDrafts(@PathVariable Long id,
                                                        @RequestBody ReportInitialDraftRequest request) {
        try {
            return Result.success(reportProjectService.generateInitialDrafts(id, request));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Generate report drafts failed, id={}", id, ex);
            return Result.error("Generate report drafts failed");
        }
    }

    @GetMapping("/{id}/export/preview")
    @Operation(summary = "Preview merged report")
    public Result<String> previewMergedReport(@PathVariable Long id) {
        try {
            return Result.success(reportProjectService.buildMergedReport(id));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("Preview merged report failed, id={}", id, ex);
            return Result.error("Preview merged report failed");
        }
    }

    @GetMapping("/{id}/download/merged")
    @Operation(summary = "Download merged report markdown")
    public void downloadMergedReport(@PathVariable Long id, HttpServletResponse response) throws IOException {
        String content = reportProjectService.buildMergedReport(id);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = "report_project_" + id + "_" + timestamp + ".md";
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/markdown; charset=UTF-8");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename*=UTF-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        response.getWriter().write(content);
        response.getWriter().flush();
    }
}
