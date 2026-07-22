package com.educationcertificationsystem.report.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
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
import com.educationcertificationsystem.model.entity.ReportProject;
import com.educationcertificationsystem.model.entity.ReportTaskAssignment;
import com.educationcertificationsystem.model.vo.report.ReportDraftVO;
import com.educationcertificationsystem.model.vo.report.ReportProgressBoardVO;
import com.educationcertificationsystem.model.vo.report.ReportProjectDetailVO;
import com.educationcertificationsystem.model.vo.report.ReportProjectPageVO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ReportProjectService extends IService<ReportProject> {

    Page<ReportProjectPageVO> pageByCondition(long pageNum,
                                              long pageSize,
                                              String status,
                                              Long ownerUserId,
                                              Long viewerUserId,
                                              String keyword);

    ReportProjectDetailVO getDetail(Long projectId, Long viewerUserId);

    ReportProjectDetailVO createProject(ReportProjectSaveRequest request);

    ReportProjectDetailVO updateProject(Long projectId, ReportProjectSaveRequest request);

    void deleteProject(Long projectId);

    ReportProjectDetailVO saveChapterTree(Long projectId, List<ReportChapterSaveRequest> requests);

    List<ReportTaskAssignment> saveAssignments(Long projectId, List<ReportTaskAssignmentSaveRequest> requests);

    List<ReportDraftVO> listDrafts(Long chapterId);

    ReportDraft saveDraft(Long chapterId, ReportDraftSaveRequest request);

    ReportDraft uploadDraft(Long chapterId, ReportDraftUploadRequest request, MultipartFile file);

    ReportChapter lockChapter(Long chapterId, ReportChapterLockRequest request);

    ReportProgressLog saveProgress(Long projectId, ReportProgressSaveRequest request);

    ReportProgressBoardVO getProgressBoard(Long projectId);

    ReportProjectDetailVO generateInitialDrafts(Long projectId, ReportInitialDraftRequest request);

    String buildMergedReport(Long projectId);
}
