package com.educationcertificationsystem.eval.mapper;

import com.educationcertificationsystem.model.dto.eval.EvalDashboardQueryRequest;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardCourseTargetDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardDistributionItemVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardGraduationRequirementDetailVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardOverviewVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardRequirementMatrixRowVO;
import com.educationcertificationsystem.model.vo.eval.EvalDashboardTrendPointVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface EvalDashboardMapper {

    EvalDashboardOverviewVO selectOverview(@Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardTrendPointVO> selectSemesterTrend(@Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardDistributionItemVO> selectCourseDistribution(@Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardDistributionItemVO> selectRequirementDistribution(@Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardRequirementMatrixRowVO> selectRequirementMatrix(@Param("query") EvalDashboardQueryRequest query);

    long countCourseTargetDetails(@Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardCourseTargetDetailVO> selectCourseTargetDetails(@Param("offset") long offset,
                                                                      @Param("size") long size,
                                                                      @Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardCourseTargetDetailVO> selectAllCourseTargetDetails(@Param("query") EvalDashboardQueryRequest query);

    long countGraduationRequirementDetails(@Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardGraduationRequirementDetailVO> selectGraduationRequirementDetails(@Param("offset") long offset,
                                                                                        @Param("size") long size,
                                                                                        @Param("query") EvalDashboardQueryRequest query);

    List<EvalDashboardGraduationRequirementDetailVO> selectAllGraduationRequirementDetails(
            @Param("query") EvalDashboardQueryRequest query);
}
