package com.educationcertificationsystem.notice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.educationcertificationsystem.model.entity.NoticePushLog;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.notice.service.NoticePushLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "通知推送日志管理")
@RequestMapping("/notice/push-logs")
public class NoticePushLogController {

    private final NoticePushLogService noticePushLogService;
    private final NoticeMessageService noticeMessageService;

    @PostMapping
    @Operation(summary = "新增通知推送日志")
    @Transactional
    public Result<NoticePushLog> create(@RequestBody NoticePushLog request) {
        String error = validateForCreate(request);
        if (error != null) {
            return Result.error(error);
        }

        NoticePushLog entity = new NoticePushLog();
        entity.setNoticeId(request.getNoticeId());
        entity.setMqTopic(request.getMqTopic());
        entity.setMqKey(request.getMqKey());
        entity.setRetryCount(request.getRetryCount() == null ? 0 : request.getRetryCount());
        entity.setSendStatus(request.getSendStatus());
        entity.setErrorMessage(request.getErrorMessage());
        entity.setSentAt(request.getSentAt());
        entity.setAckedAt(request.getAckedAt());
        entity.setIsDeleted(0);
        entity.setRemark(request.getRemark());

        noticePushLogService.save(entity);
        return Result.success(entity);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询推送日志")
    public Result<NoticePushLog> getById(@PathVariable Long id) {
        NoticePushLog entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知推送日志不存在");
        }
        return Result.success(entity);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询推送日志列表")
    public Result<Page<NoticePushLog>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long noticeId,
            @RequestParam(required = false) String sendStatus,
            @RequestParam(required = false) String mqTopic) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        LambdaQueryWrapper<NoticePushLog> wrapper = baseWrapper()
                .eq(noticeId != null, NoticePushLog::getNoticeId, noticeId)
                .eq(StringUtils.hasText(sendStatus), NoticePushLog::getSendStatus, sendStatus)
                .eq(StringUtils.hasText(mqTopic), NoticePushLog::getMqTopic, mqTopic)
                .orderByDesc(NoticePushLog::getId);
        long total = noticePushLogService.count(wrapper);
        LambdaQueryWrapper<NoticePushLog> pageWrapper = baseWrapper()
                .eq(noticeId != null, NoticePushLog::getNoticeId, noticeId)
                .eq(StringUtils.hasText(sendStatus), NoticePushLog::getSendStatus, sendStatus)
                .eq(StringUtils.hasText(mqTopic), NoticePushLog::getMqTopic, mqTopic)
                .orderByDesc(NoticePushLog::getId);
        pageWrapper.last("LIMIT " + (current - 1) * size + "," + size);
        Page<NoticePushLog> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(noticePushLogService.list(pageWrapper));
        return Result.success(page);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新推送日志")
    @Transactional
    public Result<NoticePushLog> update(@PathVariable Long id, @RequestBody NoticePushLog request) {
        NoticePushLog entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知推送日志不存在");
        }
        String error = validateForCreate(request);
        if (error != null) {
            return Result.error(error);
        }

        entity.setNoticeId(request.getNoticeId());
        if (StringUtils.hasText(request.getMqTopic())) {
            entity.setMqTopic(request.getMqTopic());
        }
        if (StringUtils.hasText(request.getMqKey())) {
            entity.setMqKey(request.getMqKey());
        }
        if (request.getRetryCount() != null) {
            entity.setRetryCount(request.getRetryCount());
        }
        if (StringUtils.hasText(request.getSendStatus())) {
            entity.setSendStatus(request.getSendStatus());
        }
        if (StringUtils.hasText(request.getErrorMessage())) {
            entity.setErrorMessage(request.getErrorMessage());
        }
        if (request.getSentAt() != null) {
            entity.setSentAt(request.getSentAt());
        }
        if (request.getAckedAt() != null) {
            entity.setAckedAt(request.getAckedAt());
        }
        if (StringUtils.hasText(request.getRemark())) {
            entity.setRemark(request.getRemark());
        }

        noticePushLogService.updateById(entity);
        return Result.success(entity);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除推送日志")
    @Transactional
    public Result<String> delete(@PathVariable Long id) {
        NoticePushLog entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知推送日志不存在");
        }
        entity.setIsDeleted(1);
        noticePushLogService.updateById(entity);
        return Result.success("删除成功");
    }

    private LambdaQueryWrapper<NoticePushLog> baseWrapper() {
        return new LambdaQueryWrapper<NoticePushLog>().eq(NoticePushLog::getIsDeleted, 0);
    }

    private NoticePushLog getActiveById(Long id) {
        NoticePushLog entity = noticePushLogService.getById(id);
        if (entity == null || (entity.getIsDeleted() != null && entity.getIsDeleted() != 0)) {
            return null;
        }
        return entity;
    }

    private String validateForCreate(NoticePushLog request) {
        if (request == null) {
            return "请求体不能为空";
        }
        if (request.getNoticeId() == null) {
            return "通知消息ID不能为空";
        }
        if (!StringUtils.hasText(request.getSendStatus())) {
            return "发送状态不能为空";
        }
        NoticeMessage noticeMessage = noticeMessageService.getById(request.getNoticeId());
        if (noticeMessage == null || (noticeMessage.getIsDeleted() != null && noticeMessage.getIsDeleted() != 0)) {
            return "通知消息不存在";
        }
        return null;
    }
}
