package com.educationcertificationsystem.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.educationcertificationsystem.model.entity.NoticeRecipient;
import com.educationcertificationsystem.model.vo.notice.NoticeInboxItem;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
* @author Lizc233
* @description 针对表【notice_recipient(通知接收人表)】的数据库操作Mapper
* @createDate 2026-07-16 14:29:34
* @Entity com.educationcertificationsystem.model.entity.NoticeRecipient
*/
public interface NoticeRecipientMapper extends BaseMapper<NoticeRecipient> {
    NoticeRecipient selectActiveById(@Param("id") Long id);

    NoticeRecipient selectByNoticeIdAndRecipientUserId(@Param("noticeId") Long noticeId,
                                                       @Param("recipientUserId") Long recipientUserId);

    long countByCondition(@Param("noticeId") Long noticeId,
                          @Param("recipientUserId") Long recipientUserId,
                          @Param("readStatus") Integer readStatus);

    long countUnread(@Param("recipientUserId") Long recipientUserId);

    long countInbox(@Param("recipientUserId") Long recipientUserId,
                    @Param("readStatus") Integer readStatus,
                    @Param("noticeType") String noticeType,
                    @Param("title") String title);

    List<NoticeRecipient> selectByCondition(@Param("offset") long offset,
                                            @Param("size") long size,
                                            @Param("noticeId") Long noticeId,
                                            @Param("recipientUserId") Long recipientUserId,
                                            @Param("readStatus") Integer readStatus);

    List<NoticeInboxItem> selectInbox(@Param("offset") long offset,
                                      @Param("size") long size,
                                      @Param("recipientUserId") Long recipientUserId,
                                      @Param("readStatus") Integer readStatus,
                                      @Param("noticeType") String noticeType,
                                      @Param("title") String title);

    int markAllRead(@Param("recipientUserId") Long recipientUserId,
                    @Param("readAt") LocalDateTime readAt);

    int batchInsertIgnore(@Param("list") List<NoticeRecipient> recipients);

}




