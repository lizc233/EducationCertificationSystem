package com.educationcertificationsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.auth.CurrentUserContext;
import com.educationcertificationsystem.auth.CurrentUserInfo;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.common.BusinessException;
import com.educationcertificationsystem.common.PageResult;
import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.educationcertificationsystem.model.entity.NoticeRecipient;
import com.educationcertificationsystem.notice.mapper.NoticeMessageMapper;
import com.educationcertificationsystem.notice.mapper.NoticeRecipientMapper;
import com.educationcertificationsystem.vo.notice.NoticeInboxItemVO;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NoticeCenterService {

    private final JdbcTemplate jdbcTemplate;
    private final NoticeMessageMapper noticeMessageMapper;
    private final NoticeRecipientMapper noticeRecipientMapper;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public NoticeCenterService(
        JdbcTemplate jdbcTemplate,
        NoticeMessageMapper noticeMessageMapper,
        NoticeRecipientMapper noticeRecipientMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.noticeMessageMapper = noticeMessageMapper;
        this.noticeRecipientMapper = noticeRecipientMapper;
    }

    public PageResult<NoticeInboxItemVO> inbox(
        Long recipientUserId,
        Long pageNum,
        Long pageSize,
        Integer readStatus,
        String noticeType,
        String title
    ) {
        assertAccess(recipientUserId);
        ensureReady();

        List<NoticeRecipient> recipients = noticeRecipientMapper.selectList(new LambdaQueryWrapper<NoticeRecipient>()
            .eq(NoticeRecipient::getRecipientUserId, recipientUserId)
            .eq(NoticeRecipient::getIsDeleted, 0)
            .eq(NoticeRecipient::getDeletedFlag, 0)
            .orderByDesc(NoticeRecipient::getCreatedAt));
        if (readStatus != null) {
            recipients = recipients.stream()
                .filter(item -> readStatus.equals(item.getReadStatus()))
                .toList();
        }
        if (recipients.isEmpty()) {
            return new PageResult<>(0L, pageNum, pageSize, List.of());
        }

        Map<Long, NoticeMessage> noticeMap = noticeMessageMapper.selectBatchIds(
                recipients.stream().map(NoticeRecipient::getNoticeId).distinct().toList())
            .stream()
            .filter(item -> item.getIsDeleted() == 0)
            .filter(item -> !"REVOKED".equalsIgnoreCase(item.getPublishStatus()))
            .collect(Collectors.toMap(NoticeMessage::getId, Function.identity(), (left, right) -> left));

        List<NoticeInboxItemVO> rows = recipients.stream()
            .map(recipient -> toInboxItem(recipient, noticeMap.get(recipient.getNoticeId())))
            .filter(Objects::nonNull)
            .filter(item -> matchInboxItem(item, noticeType, title))
            .sorted(Comparator.comparing(NoticeInboxItemVO::getSendAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(NoticeInboxItemVO::getRecipientCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();

        int safePageNum = Math.max(1, Math.toIntExact(pageNum));
        int safePageSize = Math.max(1, Math.toIntExact(pageSize));
        int fromIndex = Math.max(0, (safePageNum - 1) * safePageSize);
        int toIndex = Math.min(rows.size(), fromIndex + safePageSize);
        List<NoticeInboxItemVO> records = fromIndex >= rows.size() ? List.of() : rows.subList(fromIndex, toIndex);
        return new PageResult<>((long) rows.size(), (long) safePageNum, (long) safePageSize, records);
    }

    public long unreadCount(Long recipientUserId) {
        assertAccess(recipientUserId);
        ensureReady();
        return noticeRecipientMapper.selectCount(new LambdaQueryWrapper<NoticeRecipient>()
            .eq(NoticeRecipient::getRecipientUserId, recipientUserId)
            .eq(NoticeRecipient::getReadStatus, 0)
            .eq(NoticeRecipient::getDeletedFlag, 0)
            .eq(NoticeRecipient::getIsDeleted, 0));
    }

    public void markRead(Long recipientId) {
        ensureReady();
        NoticeRecipient recipient = getRecipient(recipientId);
        assertAccess(recipient.getRecipientUserId());
        if (recipient.getReadStatus() == 1) {
            return;
        }
        recipient.setReadStatus(1);
        recipient.setReadAt(LocalDateTime.now());
        recipient.setUpdatedAt(LocalDateTime.now());
        noticeRecipientMapper.updateById(recipient);
    }

    public void markAllRead(Long recipientUserId) {
        assertAccess(recipientUserId);
        ensureReady();
        List<NoticeRecipient> recipients = noticeRecipientMapper.selectList(new LambdaQueryWrapper<NoticeRecipient>()
            .eq(NoticeRecipient::getRecipientUserId, recipientUserId)
            .eq(NoticeRecipient::getReadStatus, 0)
            .eq(NoticeRecipient::getDeletedFlag, 0)
            .eq(NoticeRecipient::getIsDeleted, 0));
        if (recipients.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        recipients.forEach(item -> {
            item.setReadStatus(1);
            item.setReadAt(now);
            item.setUpdatedAt(now);
        });
        recipients.forEach(noticeRecipientMapper::updateById);
    }

    public void deleteRecipient(Long recipientId) {
        ensureReady();
        NoticeRecipient recipient = getRecipient(recipientId);
        assertAccess(recipient.getRecipientUserId());
        recipient.setDeletedFlag(1);
        recipient.setUpdatedAt(LocalDateTime.now());
        noticeRecipientMapper.updateById(recipient);
    }

    private void ensureReady() {
        if (initialized.get()) {
            return;
        }
        synchronized (initialized) {
            if (initialized.get()) {
                return;
            }
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notice_message (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    notice_type VARCHAR(50) NOT NULL,
                    title VARCHAR(200) NOT NULL,
                    content TEXT NOT NULL,
                    sender_user_id BIGINT,
                    biz_type VARCHAR(50),
                    biz_id BIGINT,
                    channel_type VARCHAR(20) NOT NULL,
                    priority_level INT NOT NULL DEFAULT 0,
                    publish_status VARCHAR(20) NOT NULL,
                    send_at TIMESTAMP,
                    expire_at TIMESTAMP,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    remark VARCHAR(500),
                    CONSTRAINT fk_notice_message_sender FOREIGN KEY (sender_user_id) REFERENCES sys_user(id)
                )
                """);
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notice_recipient (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    notice_id BIGINT NOT NULL,
                    recipient_user_id BIGINT NOT NULL,
                    read_status TINYINT NOT NULL DEFAULT 0,
                    read_at TIMESTAMP,
                    deleted_flag TINYINT NOT NULL DEFAULT 0,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    is_deleted TINYINT NOT NULL DEFAULT 0,
                    remark VARCHAR(500),
                    CONSTRAINT uk_notice_recipient UNIQUE (notice_id, recipient_user_id),
                    CONSTRAINT fk_notice_recipient_notice FOREIGN KEY (notice_id) REFERENCES notice_message(id),
                    CONSTRAINT fk_notice_recipient_user FOREIGN KEY (recipient_user_id) REFERENCES sys_user(id)
                )
                """);
            initialized.set(true);
        }
    }

    private NoticeInboxItemVO toInboxItem(NoticeRecipient recipient, NoticeMessage message) {
        if (message == null) {
            return null;
        }
        NoticeInboxItemVO item = new NoticeInboxItemVO();
        item.setRecipientId(recipient.getId());
        item.setNoticeId(message.getId());
        item.setNoticeType(message.getNoticeType());
        item.setTitle(message.getTitle());
        item.setContent(message.getContent());
        item.setBizType(message.getBizType());
        item.setBizId(message.getBizId());
        item.setChannelType(message.getChannelType());
        item.setReadStatus(recipient.getReadStatus());
        item.setReadAt(recipient.getReadAt());
        item.setSendAt(message.getSendAt());
        item.setExpireAt(message.getExpireAt());
        item.setRecipientCreatedAt(recipient.getCreatedAt());
        return item;
    }

    private boolean matchInboxItem(NoticeInboxItemVO item, String noticeType, String title) {
        if (StringUtils.hasText(noticeType) && !noticeType.equals(item.getNoticeType())) {
            return false;
        }
        if (StringUtils.hasText(title)) {
            String keyword = title.trim().toLowerCase(Locale.ROOT);
            return contains(item.getTitle(), keyword) || contains(item.getContent(), keyword);
        }
        return true;
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private NoticeRecipient getRecipient(Long recipientId) {
        NoticeRecipient recipient = noticeRecipientMapper.selectById(recipientId);
        if (recipient == null || recipient.getIsDeleted() == 1 || recipient.getDeletedFlag() == 1) {
            throw new BusinessException("Notice recipient not found");
        }
        return recipient;
    }

    private void assertAccess(Long recipientUserId) {
        CurrentUserInfo currentUser = CurrentUserContext.require();
        if (Objects.equals(currentUser.getUserId(), recipientUserId)) {
            return;
        }
        if (currentUser.getRoleCodes() != null && currentUser.getRoleCodes().contains(RoleConstants.SUPER_ADMIN)) {
            return;
        }
        throw new BusinessException(403, "Access denied");
    }
}
