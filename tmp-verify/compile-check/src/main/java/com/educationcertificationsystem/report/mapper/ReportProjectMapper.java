package com.educationcertificationsystem.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.entity.ReportProject;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ReportProjectMapper extends BaseMapper<ReportProject> {

    long countByCondition(@Param("status") String status,
                          @Param("ownerUserId") Long ownerUserId,
                          @Param("viewerUserId") Long viewerUserId,
                          @Param("keyword") String keyword);

    List<ReportProject> selectPageByCondition(@Param("offset") long offset,
                                              @Param("size") long size,
                                              @Param("status") String status,
                                              @Param("ownerUserId") Long ownerUserId,
                                              @Param("viewerUserId") Long viewerUserId,
                                              @Param("keyword") String keyword);
}
