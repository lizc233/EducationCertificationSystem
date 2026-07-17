package com.educationcertificationsystem.notice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.model.entity.NoticeRecipient;
import com.educationcertificationsystem.notice.mapper.NoticeRecipientMapper;
import com.educationcertificationsystem.notice.service.NoticeRecipientService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NoticeRecipientServiceImpl extends ServiceImpl<NoticeRecipientMapper, NoticeRecipient>
        implements NoticeRecipientService {

    @Override
    public NoticeRecipient getActiveById(Long id) {
        return baseMapper.selectActiveById(id);
    }

    @Override
    public NoticeRecipient findByNoticeIdAndRecipientUserId(Long noticeId, Long recipientUserId) {
        return baseMapper.selectByNoticeIdAndRecipientUserId(noticeId, recipientUserId);
    }

    @Override
    public Page<NoticeRecipient> pageByCondition(long pageNum, long pageSize, Long noticeId, Long recipientUserId,
                                                 Integer readStatus) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        long offset = (current - 1) * size;
        long total = countByCondition(noticeId, recipientUserId, readStatus);
        List<NoticeRecipient> records = total == 0
                ? List.of()
                : baseMapper.selectByCondition(offset, size, noticeId, recipientUserId, readStatus);
        Page<NoticeRecipient> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<NoticeRecipient> listByCondition(Long noticeId, Long recipientUserId, Integer readStatus) {
        return baseMapper.selectByCondition(0L, Long.MAX_VALUE, noticeId, recipientUserId, readStatus);
    }

    @Override
    public long countByCondition(Long noticeId, Long recipientUserId, Integer readStatus) {
        return baseMapper.countByCondition(noticeId, recipientUserId, readStatus);
    }

    @Override
    public long countUnread(Long recipientUserId) {
        return baseMapper.countUnread(recipientUserId);
    }

    @Override
    public int batchCreateRecipients(Long noticeId, List<Long> recipientUserIds, String remark) {
        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>();
        if (recipientUserIds != null) {
            for (Long recipientUserId : recipientUserIds) {
                if (recipientUserId != null) {
                    uniqueIds.add(recipientUserId);
                }
            }
        }
        if (uniqueIds.isEmpty()) {
            return 0;
        }

        List<NoticeRecipient> recipients = new ArrayList<>(uniqueIds.size());
        for (Long recipientUserId : uniqueIds) {
            NoticeRecipient recipient = new NoticeRecipient();
            recipient.setNoticeId(noticeId);
            recipient.setRecipientUserId(recipientUserId);
            recipient.setReadStatus(0);
            recipient.setDeletedFlag(0);
            recipient.setIsDeleted(0);
            recipient.setRemark(remark);
            recipients.add(recipient);
        }
        return baseMapper.batchInsertIgnore(recipients);
    }
}
