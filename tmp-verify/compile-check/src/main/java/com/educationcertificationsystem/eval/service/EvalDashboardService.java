package com.educationcertificationsystem.eval.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.model.dto.eval.EvalDashboardQueryRequest;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardCourseTargetDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardDistributionItemVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardGraduationRequirementDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardOverviewVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardRequirementMatrixRowVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardTrendPointVO;
import java.util.List;

public interface EvalDashboardService {

    EvalDashboardOverviewVO getOverview(EvalDashboardQueryRequest request);

    List<EvalDashboardTrendPointVO> getSemesterTrend(EvalDashboardQueryRequest request);

    List<EvalDashboardDistributionItemVO> getCourseDistribution(EvalDashboardQueryRequest request);

    List<EvalDashboardDistributionItemVO> getRequirementDistribution(EvalDashboardQueryRequest request);

    List<EvalDashboardRequirementMatrixRowVO> getRequirementMatrix(EvalDashboardQueryRequest request);

    Page<EvalDashboardCourseTargetDetailVO> pageCourseTargetDetails(EvalDashboardQueryRequest request);

    List<EvalDashboardCourseTargetDetailVO> exportCourseTargetDetails(EvalDashboardQueryRequest request);

    Page<EvalDashboardGraduationRequirementDetailVO> pageGraduationRequirementDetails(EvalDashboardQueryRequest request);

    List<EvalDashboardGraduationRequirementDetailVO> exportGraduationRequirementDetails(EvalDashboardQueryRequest request);
}
