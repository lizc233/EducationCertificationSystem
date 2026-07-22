package com.educationcertificationsystem.improve.mapper;

import com.educationcertificationsystem.model.vo.improve.ImprovePlanPageVO;
import com.educationcertificationsystem.model.entity.ImprovePlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【improve_plan(改进计划表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:34
* @Entity com.educationcertificationsystem.model.entity.ImprovePlan
*/
public interface ImprovePlanMapper extends BaseMapper<ImprovePlan> {

    long countByCondition(@Param("status") String status,
                          @Param("sourceType") String sourceType,
                          @Param("targetType") String targetType,
                          @Param("ownerUserId") Long ownerUserId,
                          @Param("responsibleUserId") Long responsibleUserId,
                          @Param("priority") Integer priority,
                          @Param("overdueOnly") Integer overdueOnly,
                          @Param("keyword") String keyword);

    List<ImprovePlanPageVO> selectPageByCondition(@Param("offset") long offset,
                                                  @Param("size") long size,
                                                  @Param("status") String status,
                                                  @Param("sourceType") String sourceType,
                                                  @Param("targetType") String targetType,
                                                  @Param("ownerUserId") Long ownerUserId,
                                                  @Param("responsibleUserId") Long responsibleUserId,
                                                  @Param("priority") Integer priority,
                                                  @Param("overdueOnly") Integer overdueOnly,
                                                  @Param("keyword") String keyword);
}




