package com.educationcertificationsystem.notice.service;

import com.educationcertificationsystem.model.entity.NoticeRecipient;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【notice_recipient(通知接收人表)】的数据库操作Service
* @createDate 2026-07-16 14:29:34
*/
public interface NoticeRecipientService extends IService<NoticeRecipient> {
    NoticeRecipient getActiveById(Long id);

    NoticeRecipient findByNoticeIdAndRecipientUserId(Long noticeId, Long recipientUserId);

    Page<NoticeRecipient> pageByCondition(long pageNum,
                                          long pageSize,
                                          Long noticeId,
                                          Long recipientUserId,
                                          Integer readStatus);

    List<NoticeRecipient> listByCondition(Long noticeId,
                                          Long recipientUserId,
                                          Integer readStatus);

    long countByCondition(Long noticeId,
                          Long recipientUserId,
                          Integer readStatus);

    long countUnread(Long recipientUserId);

    int batchCreateRecipients(Long noticeId, List<Long> recipientUserIds, String remark);
}
