package com.educationcertificationsystem.notice.mapper;

import com.educationcertificationsystem.model.entity.NoticePushLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【notice_push_log(通知推送日志表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:33
* @Entity com.educationcertificationsystem.model.entity.NoticePushLog
*/
public interface NoticePushLogMapper extends BaseMapper<NoticePushLog> {
    NoticePushLog selectActiveById(@Param("id") Long id);

    long countByCondition(@Param("noticeId") Long noticeId,
                          @Param("sendStatus") String sendStatus,
                          @Param("mqTopic") String mqTopic);

    List<NoticePushLog> selectByCondition(@Param("offset") long offset,
                                          @Param("size") long size,
                                          @Param("noticeId") Long noticeId,
                                          @Param("sendStatus") String sendStatus,
                                          @Param("mqTopic") String mqTopic);

    NoticePushLog selectLatestByNoticeId(@Param("noticeId") Long noticeId);

}




