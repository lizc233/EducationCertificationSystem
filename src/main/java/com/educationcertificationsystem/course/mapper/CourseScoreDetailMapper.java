package com.educationcertificationsystem.course.mapper;

import com.educationcertificationsystem.model.entity.CourseScoreDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.vo.course.ScoreDetailView;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author Lizc233
* @description 针对表【course_score_detail(成绩明细表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.CourseScoreDetail
*/
public interface CourseScoreDetailMapper extends BaseMapper<CourseScoreDetail> {

    /**
     * 按批次查询成绩明细（联表学生学号、姓名、班级），供 F15 列表展示。
     */
    List<ScoreDetailView> selectDetailViews(@Param("batchId") Long batchId);
}




