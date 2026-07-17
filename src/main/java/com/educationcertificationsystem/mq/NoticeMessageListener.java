package com.educationcertificationsystem.mq;

import com.educationcertificationsystem.constant.NoticeMqConstants;
import com.educationcertificationsystem.model.dto.notice.NoticePublishEvent;
import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.notice.service.NoticePushLogService;
import com.educationcertificationsystem.notice.service.NoticeRecipientService;
import com.educationcertificationsystem.user.service.SysUserService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/*

      MQ核心实现逻辑




 */

@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeMessageListener {

    private final NoticeMessageService noticeMessageService;
    private final NoticeRecipientService noticeRecipientService;
    private final NoticePushLogService noticePushLogService;
    private final SysUserService sysUserService;

    //触发入口，监听 NOTICE_QUEUE 这个队列，
    //只要 RabbitMQ 中这个队列有消息进来，这个方法就会被自动调用
    //消息内容会被 Spring 自动反序列化成 NoticePublishEvent 对象
    @RabbitListener(queues = NoticeMqConstants.NOTICE_QUEUE)
    public void onNoticePublish(NoticePublishEvent event) {
        if (event == null || event.getNoticeId() == null) {
            log.error("收到无效的通知发布事件: {}", event);
            return;
        }

        try {
            NoticeMessage noticeMessage = noticeMessageService.getActiveById(event.getNoticeId());
            if (noticeMessage == null) {
                throw new IllegalArgumentException("通知消息不存在或已删除");
            }

            List<Long> recipientUserIds = normalizeRecipientIds(event.getRecipientUserIds());
            if (recipientUserIds.isEmpty()) {
                throw new IllegalArgumentException("通知接收人不能为空");
            }

            validateUsers(recipientUserIds);
            noticeRecipientService.batchCreateRecipients(event.getNoticeId(), recipientUserIds, event.getRemark());
            noticePushLogService.markConsumed(event.getNoticeId());
            noticeMessageService.updatePublishStatus(event.getNoticeId(), NoticeMqConstants.NOTICE_STATUS_PUBLISHED);
            log.info("通知 {} 消费成功，接收人数={}", event.getNoticeId(), recipientUserIds.size());
        } catch (Exception ex) {
            log.error("通知发布事件消费失败，noticeId={}", event.getNoticeId(), ex);
            noticePushLogService.markFailed(event.getNoticeId(), ex.getMessage());
            noticeMessageService.updatePublishStatus(event.getNoticeId(), NoticeMqConstants.NOTICE_STATUS_PUBLISH_FAILED);
        }
    }

    private List<Long> normalizeRecipientIds(List<Long> recipientUserIds) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        if (recipientUserIds != null) {
            for (Long recipientUserId : recipientUserIds) {
                if (recipientUserId != null) {
                    uniqueIds.add(recipientUserId);
                }
            }
        }
        return new ArrayList<>(uniqueIds);
    }

    private void validateUsers(List<Long> recipientUserIds) {
        for (Long recipientUserId : recipientUserIds) {
            SysUser user = sysUserService.getById(recipientUserId);
            if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
                throw new IllegalArgumentException("接收人用户不存在: " + recipientUserId);
            }
        }
    }
}
