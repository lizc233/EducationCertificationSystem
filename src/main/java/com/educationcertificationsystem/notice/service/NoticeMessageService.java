package com.educationcertificationsystem.notice.service;

import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【notice_message(通知消息表)】的数据库操作Service
* @createDate 2026-07-16 14:29:34
*/
public interface NoticeMessageService extends IService<NoticeMessage> {
    NoticeMessage getActiveById(Long id);

    Page<NoticeMessage> pageByCondition(long pageNum,
                                        long pageSize,
                                        String noticeType,
                                        String publishStatus,
                                        String channelType,
                                        Long senderUserId,
                                        String title);

    List<NoticeMessage> listByCondition(String noticeType,
                                        String publishStatus,
                                        String channelType,
                                        Long senderUserId,
                                        String title);

    long countByCondition(String noticeType,
                          String publishStatus,
                          String channelType,
                          Long senderUserId,
                          String title);

    NoticeMessage publishNotice(Long noticeId,
                                List<Long> recipientUserIds,
                                Long operatorUserId,
                                String remark);

    void updatePublishStatus(Long noticeId, String publishStatus);
}
