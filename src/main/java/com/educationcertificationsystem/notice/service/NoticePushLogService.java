package com.educationcertificationsystem.notice.service;

import com.educationcertificationsystem.model.entity.NoticePushLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;

/**
* @author Lizc233
* @description 针对表【notice_push_log(通知推送日志表)】的数据库操作Service
* @createDate 2026-07-16 14:29:33
*/
public interface NoticePushLogService extends IService<NoticePushLog> {
    NoticePushLog getActiveById(Long id);

    Page<NoticePushLog> pageByCondition(long pageNum,
                                        long pageSize,
                                        Long noticeId,
                                        String sendStatus,
                                        String mqTopic);

    List<NoticePushLog> listByCondition(Long noticeId,
                                        String sendStatus,
                                        String mqTopic);

    long countByCondition(Long noticeId,
                          String sendStatus,
                          String mqTopic);

    NoticePushLog createPendingLog(Long noticeId, String mqTopic, String mqKey, String remark);

    NoticePushLog createRetryLog(Long noticeId, String mqTopic, String mqKey, String remark);

    NoticePushLog findLatestByNoticeId(Long noticeId);

    NoticePushLog markSent(Long noticeId);

    NoticePushLog markConsumed(Long noticeId);

    NoticePushLog markFailed(Long noticeId, String errorMessage);
}
