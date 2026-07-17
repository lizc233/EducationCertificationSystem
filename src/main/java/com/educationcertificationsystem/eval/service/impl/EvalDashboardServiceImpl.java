package com.educationcertificationsystem.eval.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.eval.mapper.EvalDashboardMapper;
import com.educationcertificationsystem.eval.service.EvalDashboardService;
import com.educationcertificationsystem.model.dto.eval.EvalDashboardQueryRequest;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardCourseTargetDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardDistributionItemVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardGraduationRequirementDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardOverviewVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardRequirementMatrixRowVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardTrendPointVO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvalDashboardServiceImpl implements EvalDashboardService {

    private final EvalDashboardMapper evalDashboardMapper;

    @Override
    public EvalDashboardOverviewVO getOverview(EvalDashboardQueryRequest request) {
        EvalDashboardOverviewVO overview = evalDashboardMapper.selectOverview(normalizeRequest(request));
        if (overview == null) {
            overview = new EvalDashboardOverviewVO();
            overview.setCourseTargetResultCount(0L);
            overview.setGraduationRequirementResultCount(0L);
            overview.setWarningCount(0L);
            overview.setAvgCourseTargetAttainmentRate(BigDecimal.ZERO);
            overview.setAvgGraduationRequirementAttainmentRate(BigDecimal.ZERO);
            overview.setWarningRate(BigDecimal.ZERO);
            return overview;
        }
        long graduationCount = overview.getGraduationRequirementResultCount() == null
                ? 0L
                : overview.getGraduationRequirementResultCount();
        long warningCount = overview.getWarningCount() == null ? 0L : overview.getWarningCount();
        overview.setCourseTargetResultCount(overview.getCourseTargetResultCount() == null ? 0L : overview.getCourseTargetResultCount());
        overview.setGraduationRequirementResultCount(graduationCount);
        overview.setWarningCount(warningCount);
        overview.setAvgCourseTargetAttainmentRate(defaultDecimal(overview.getAvgCourseTargetAttainmentRate()));
        overview.setAvgGraduationRequirementAttainmentRate(defaultDecimal(overview.getAvgGraduationRequirementAttainmentRate()));
        overview.setWarningRate(graduationCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(warningCount)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(graduationCount), 2, RoundingMode.HALF_UP));
        return overview;
    }

    @Override
    public List<EvalDashboardTrendPointVO> getSemesterTrend(EvalDashboardQueryRequest request) {
        return evalDashboardMapper.selectSemesterTrend(normalizeRequest(request));
    }

    @Override
    public List<EvalDashboardDistributionItemVO> getCourseDistribution(EvalDashboardQueryRequest request) {
        return evalDashboardMapper.selectCourseDistribution(normalizeRequest(request));
    }

    @Override
    public List<EvalDashboardDistributionItemVO> getRequirementDistribution(EvalDashboardQueryRequest request) {
        return evalDashboardMapper.selectRequirementDistribution(normalizeRequest(request));
    }

    @Override
    public List<EvalDashboardRequirementMatrixRowVO> getRequirementMatrix(EvalDashboardQueryRequest request) {
        return evalDashboardMapper.selectRequirementMatrix(normalizeRequest(request));
    }

    @Override
    public Page<EvalDashboardCourseTargetDetailVO> pageCourseTargetDetails(EvalDashboardQueryRequest request) {
        EvalDashboardQueryRequest normalized = normalizeRequest(request);
        long pageNum = normalized.getPageNum() == null ? 1L : Math.max(normalized.getPageNum(), 1L);
        long pageSize = normalized.getPageSize() == null ? 10L : Math.max(normalized.getPageSize(), 1L);
        long offset = (pageNum - 1) * pageSize;
        long total = evalDashboardMapper.countCourseTargetDetails(normalized);
        List<EvalDashboardCourseTargetDetailVO> records = total == 0
                ? List.of()
                : evalDashboardMapper.selectCourseTargetDetails(offset, pageSize, normalized);
        Page<EvalDashboardCourseTargetDetailVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<EvalDashboardCourseTargetDetailVO> exportCourseTargetDetails(EvalDashboardQueryRequest request) {
        return evalDashboardMapper.selectAllCourseTargetDetails(normalizeRequest(request));
    }

    @Override
    public Page<EvalDashboardGraduationRequirementDetailVO> pageGraduationRequirementDetails(EvalDashboardQueryRequest request) {
        EvalDashboardQueryRequest normalized = normalizeRequest(request);
        long pageNum = normalized.getPageNum() == null ? 1L : Math.max(normalized.getPageNum(), 1L);
        long pageSize = normalized.getPageSize() == null ? 10L : Math.max(normalized.getPageSize(), 1L);
        long offset = (pageNum - 1) * pageSize;
        long total = evalDashboardMapper.countGraduationRequirementDetails(normalized);
        List<EvalDashboardGraduationRequirementDetailVO> records = total == 0
                ? List.of()
                : evalDashboardMapper.selectGraduationRequirementDetails(offset, pageSize, normalized);
        Page<EvalDashboardGraduationRequirementDetailVO> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<EvalDashboardGraduationRequirementDetailVO> exportGraduationRequirementDetails(EvalDashboardQueryRequest request) {
        return evalDashboardMapper.selectAllGraduationRequirementDetails(normalizeRequest(request));
    }

    private EvalDashboardQueryRequest normalizeRequest(EvalDashboardQueryRequest request) {
        return request == null ? new EvalDashboardQueryRequest() : request;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}
