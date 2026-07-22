package com.educationcertificationsystem.notice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.constant.NoticeMqConstants;
import com.educationcertificationsystem.model.dto.notice.NoticePublishEvent;
import com.educationcertificationsystem.model.dto.notice.NoticeSendRequest;
import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.educationcertificationsystem.model.entity.NoticeRecipient;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.notice.mapper.NoticeMessageMapper;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.notice.service.NoticePushLogService;
import com.educationcertificationsystem.notice.service.NoticeRecipientService;
import com.educationcertificationsystem.user.service.SysUserService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeMessageServiceImpl extends ServiceImpl<NoticeMessageMapper, NoticeMessage>
        implements NoticeMessageService {

    private final RabbitTemplate rabbitTemplate;
    private final NoticePushLogService noticePushLogService;
    private final NoticeRecipientService noticeRecipientService;
    private final SysUserService sysUserService;

    @Override
    public NoticeMessage getActiveById(Long id) {
        return baseMapper.selectActiveById(id);
    }

    @Override
    public Page<NoticeMessage> pageByCondition(long pageNum, long pageSize, String noticeType, String publishStatus,
                                               String channelType, Long senderUserId, String title) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        long offset = (current - 1) * size;
        long total = countByCondition(noticeType, publishStatus, channelType, senderUserId, title);
        List<NoticeMessage> records = total == 0
                ? List.of()
                : baseMapper.selectByCondition(offset, size, noticeType, publishStatus, channelType, senderUserId, title);
        Page<NoticeMessage> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<NoticeMessage> listByCondition(String noticeType, String publishStatus, String channelType,
                                               Long senderUserId, String title) {
        return baseMapper.selectByCondition(0L, Long.MAX_VALUE, noticeType, publishStatus, channelType, senderUserId, title);
    }

    @Override
    public long countByCondition(String noticeType, String publishStatus, String channelType, Long senderUserId,
                                 String title) {
        return baseMapper.countByCondition(noticeType, publishStatus, channelType, senderUserId, title);
    }

    @Override
    @Transactional
    public NoticeMessage publishNotice(Long noticeId, List<Long> recipientUserIds, Long operatorUserId, String remark) {
        NoticeMessage noticeMessage = getRequiredNotice(noticeId);
        List<Long> uniqueRecipientIds = normalizeRecipientIds(recipientUserIds);
        if (uniqueRecipientIds.isEmpty()) {
            throw new IllegalArgumentException("通知接收人不能为空");
        }
        validateRecipients(uniqueRecipientIds);

        String currentStatus = noticeMessage.getPublishStatus();
        if (NoticeMqConstants.NOTICE_STATUS_PUBLISHING.equals(currentStatus)
                || NoticeMqConstants.NOTICE_STATUS_PUBLISHED.equals(currentStatus)) {
            throw new IllegalStateException("通知已经发布或正在发布");
        }

        return dispatchNotice(noticeMessage, uniqueRecipientIds, operatorUserId, remark, false);
    }

    @Override
    @Transactional
    public NoticeMessage retryPublish(Long noticeId, List<Long> recipientUserIds, Long operatorUserId, String remark) {
        NoticeMessage noticeMessage = getRequiredNotice(noticeId);
        if (NoticeMqConstants.NOTICE_STATUS_PUBLISHING.equals(noticeMessage.getPublishStatus())) {
            throw new IllegalStateException("通知正在发布，无法重试");
        }

        List<Long> resolvedRecipientIds = normalizeRecipientIds(recipientUserIds);
        if (resolvedRecipientIds.isEmpty()) {
            List<NoticeRecipient> recipients = noticeRecipientService.listByCondition(noticeId, null, null);
            for (NoticeRecipient recipient : recipients) {
                if (recipient.getRecipientUserId() != null) {
                    resolvedRecipientIds.add(recipient.getRecipientUserId());
                }
            }
        }
        if (resolvedRecipientIds.isEmpty()) {
            throw new IllegalArgumentException("缺少可重试的接收人，请重新指定");
        }
        validateRecipients(resolvedRecipientIds);
        return dispatchNotice(noticeMessage, resolvedRecipientIds, operatorUserId, remark, true);
    }

    @Override
    @Transactional
    public NoticeMessage sendNotice(NoticeSendRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (request.getSenderUserId() != null) {
            SysUser sender = sysUserService.getById(request.getSenderUserId());
            if (sender == null || (sender.getIsDeleted() != null && sender.getIsDeleted() != 0)) {
                throw new IllegalArgumentException("发送人不存在");
            }
        }

        NoticeMessage entity = new NoticeMessage();
        entity.setNoticeType(request.getNoticeType());
        entity.setTitle(request.getTitle());
        entity.setContent(request.getContent());
        entity.setSenderUserId(request.getSenderUserId());
        entity.setBizType(request.getBizType());
        entity.setBizId(request.getBizId());
        entity.setChannelType(request.getChannelType());
        entity.setPriorityLevel(request.getPriorityLevel() == null ? 0 : request.getPriorityLevel());
        entity.setPublishStatus(NoticeMqConstants.NOTICE_STATUS_DRAFT);
        entity.setSendAt(request.getSendAt());
        entity.setExpireAt(request.getExpireAt());
        entity.setIsDeleted(0);
        entity.setRemark(request.getRemark());
        save(entity);

        return publishNotice(entity.getId(), request.getRecipientUserIds(), request.getOperatorUserId(), request.getRemark());
    }

    @Override
    public void updatePublishStatus(Long noticeId, String publishStatus) {
        NoticeMessage noticeMessage = baseMapper.selectById(noticeId);
        if (noticeMessage == null) {
            return;
        }
        noticeMessage.setPublishStatus(publishStatus);
        updateById(noticeMessage);
    }

    private NoticeMessage getRequiredNotice(Long noticeId) {
        NoticeMessage noticeMessage = getActiveById(noticeId);
        if (noticeMessage == null) {
            throw new IllegalArgumentException("通知消息不存在");
        }
        return noticeMessage;
    }

    private NoticeMessage dispatchNotice(NoticeMessage noticeMessage, List<Long> recipientUserIds, Long operatorUserId,
                                         String remark, boolean retry) {
        Long noticeId = noticeMessage.getId();
        noticeMessage.setPublishStatus(NoticeMqConstants.NOTICE_STATUS_PUBLISHING);
        noticeMessage.setSendAt(LocalDateTime.now());
        updateById(noticeMessage);

        if (retry) {
            noticePushLogService.createRetryLog(noticeId, NoticeMqConstants.NOTICE_EXCHANGE,
                    NoticeMqConstants.NOTICE_ROUTING_KEY, remark);
        } else {
            noticePushLogService.createPendingLog(noticeId, NoticeMqConstants.NOTICE_EXCHANGE,
                    NoticeMqConstants.NOTICE_ROUTING_KEY, remark);
        }

        NoticePublishEvent event = new NoticePublishEvent(
                noticeId,
                recipientUserIds,
                operatorUserId,
                remark,
                LocalDateTime.now());

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    rabbitTemplate.convertAndSend(NoticeMqConstants.NOTICE_EXCHANGE,
                            NoticeMqConstants.NOTICE_ROUTING_KEY, event);
                    log.info("通知 {} 已成功发送到 RabbitMQ", noticeId);
                } catch (Exception ex) {
                    log.error("通知 {} 发送到 RabbitMQ 失败", noticeId, ex);
                    noticePushLogService.markFailed(noticeId, ex.getMessage());
                    updatePublishStatus(noticeId, NoticeMqConstants.NOTICE_STATUS_PUBLISH_FAILED);
                    return;
                }

                try {
                    noticePushLogService.markSent(noticeId);
                } catch (Exception ex) {
                    log.error("通知 {} 已发送，但更新推送日志失败", noticeId, ex);
                }
            }
        });
        return noticeMessage;
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

    private void validateRecipients(List<Long> recipientUserIds) {
        for (Long recipientUserId : recipientUserIds) {
            SysUser user = sysUserService.getById(recipientUserId);
            if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
                throw new IllegalArgumentException("接收人不存在: " + recipientUserId);
            }
        }
    }
}
