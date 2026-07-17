package com.educationcertificationsystem.notice.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.constant.NoticeMqConstants;
import com.educationcertificationsystem.model.entity.NoticePushLog;
import com.educationcertificationsystem.notice.mapper.NoticePushLogMapper;
import com.educationcertificationsystem.notice.service.NoticePushLogService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NoticePushLogServiceImpl extends ServiceImpl<NoticePushLogMapper, NoticePushLog>
        implements NoticePushLogService {

    @Override
    public NoticePushLog getActiveById(Long id) {
        return baseMapper.selectActiveById(id);
    }

    @Override
    public Page<NoticePushLog> pageByCondition(long pageNum, long pageSize, Long noticeId, String sendStatus,
                                               String mqTopic) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        long offset = (current - 1) * size;
        long total = countByCondition(noticeId, sendStatus, mqTopic);
        List<NoticePushLog> records = total == 0
                ? List.of()
                : baseMapper.selectByCondition(offset, size, noticeId, sendStatus, mqTopic);
        Page<NoticePushLog> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public List<NoticePushLog> listByCondition(Long noticeId, String sendStatus, String mqTopic) {
        return baseMapper.selectByCondition(0L, Long.MAX_VALUE, noticeId, sendStatus, mqTopic);
    }

    @Override
    public long countByCondition(Long noticeId, String sendStatus, String mqTopic) {
        return baseMapper.countByCondition(noticeId, sendStatus, mqTopic);
    }

    @Override
    public NoticePushLog createPendingLog(Long noticeId, String mqTopic, String mqKey, String remark) {
        NoticePushLog log = new NoticePushLog();
        log.setNoticeId(noticeId);
        log.setMqTopic(mqTopic);
        log.setMqKey(mqKey);
        log.setRetryCount(0);
        log.setSendStatus(NoticeMqConstants.PUSH_STATUS_WAITING);
        log.setRemark(remark);
        save(log);
        return log;
    }

    @Override
    public NoticePushLog findLatestByNoticeId(Long noticeId) {
        return baseMapper.selectLatestByNoticeId(noticeId);
    }

    @Override
    public NoticePushLog markSent(Long noticeId) {
        NoticePushLog log = findLatestByNoticeId(noticeId);
        if (log == null) {
            return null;
        }
        log.setSendStatus(NoticeMqConstants.PUSH_STATUS_SENT);
        log.setSentAt(LocalDateTime.now());
        updateById(log);
        return log;
    }

    @Override
    public NoticePushLog markConsumed(Long noticeId) {
        NoticePushLog log = findLatestByNoticeId(noticeId);
        if (log == null) {
            return null;
        }
        log.setSendStatus(NoticeMqConstants.PUSH_STATUS_CONSUMED);
        log.setAckedAt(LocalDateTime.now());
        updateById(log);
        return log;
    }

    @Override
    public NoticePushLog markFailed(Long noticeId, String errorMessage) {
        NoticePushLog log = findLatestByNoticeId(noticeId);
        if (log == null) {
            return null;
        }
        log.setSendStatus(NoticeMqConstants.PUSH_STATUS_FAILED);
        log.setErrorMessage(errorMessage);
        updateById(log);
        return log;
    }
}
