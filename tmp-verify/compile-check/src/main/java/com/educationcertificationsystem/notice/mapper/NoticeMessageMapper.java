package com.educationcertificationsystem.notice.mapper;

import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【notice_message(通知消息表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:34
* @Entity com.educationcertificationsystem.model.entity.NoticeMessage
*/
public interface NoticeMessageMapper extends BaseMapper<NoticeMessage> {
    NoticeMessage selectActiveById(@Param("id") Long id);

    long countByCondition(@Param("noticeType") String noticeType,
                          @Param("publishStatus") String publishStatus,
                          @Param("channelType") String channelType,
                          @Param("senderUserId") Long senderUserId,
                          @Param("title") String title);

    List<NoticeMessage> selectByCondition(@Param("offset") long offset,
                                          @Param("size") long size,
                                          @Param("noticeType") String noticeType,
                                          @Param("publishStatus") String publishStatus,
                                          @Param("channelType") String channelType,
                                          @Param("senderUserId") Long senderUserId,
                                          @Param("title") String title);

}




