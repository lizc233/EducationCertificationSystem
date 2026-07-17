package com.educationcertificationsystem.notice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.constant.NoticeMqConstants;
import com.educationcertificationsystem.model.dto.notice.NoticePublishRequest;
import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.user.service.SysUserService;
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
@Tag(name = "通知消息管理")
@RequestMapping("/notice/messages")
public class NoticeMessageController {

    private final NoticeMessageService noticeMessageService;
    private final SysUserService sysUserService;

    @PostMapping
    @Operation(summary = "新增通知消息")
    @Transactional
    public Result<NoticeMessage> create(@RequestBody NoticeMessage request) {
        String error = validateForCreate(request);
        if (error != null) {
            return Result.error(error);
        }
        if (request.getSenderUserId() != null && !userExists(request.getSenderUserId())) {
            return Result.error("发送人不存在");
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
        entity.setPublishStatus(StringUtils.hasText(request.getPublishStatus())
                ? request.getPublishStatus()
                : NoticeMqConstants.NOTICE_STATUS_DRAFT);
        entity.setSendAt(request.getSendAt());
        entity.setExpireAt(request.getExpireAt());
        entity.setIsDeleted(0);
        entity.setRemark(request.getRemark());

        noticeMessageService.save(entity);
        return Result.success(entity);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布通知消息")
    public Result<NoticeMessage> publish(@PathVariable Long id, @RequestBody NoticePublishRequest request) {
        try {
            if (request == null) {
                return Result.error("请求体不能为空");
            }
            NoticeMessage entity = noticeMessageService.publishNotice(
                    id,
                    request.getRecipientUserIds(),
                    request.getOperatorUserId(),
                    request.getRemark());
            return Result.success(entity);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return Result.error(ex.getMessage());
        } catch (Exception ex) {
            log.error("发布通知失败, id={}", id, ex);
            return Result.error("发布失败");
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询通知消息")
    public Result<NoticeMessage> getById(@PathVariable Long id) {
        NoticeMessage entity = noticeMessageService.getActiveById(id);
        if (entity == null) {
            return Result.error("通知消息不存在");
        }
        return Result.success(entity);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询通知消息列表")
    public Result<Page<NoticeMessage>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String noticeType,
            @RequestParam(required = false) String publishStatus,
            @RequestParam(required = false) String channelType,
            @RequestParam(required = false) Long senderUserId,
            @RequestParam(required = false) String title) {
        Page<NoticeMessage> page = noticeMessageService.pageByCondition(
                pageNum, pageSize, noticeType, publishStatus, channelType, senderUserId, title);
        return Result.success(page);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通知消息")
    @Transactional
    public Result<NoticeMessage> update(@PathVariable Long id, @RequestBody NoticeMessage request) {
        NoticeMessage entity = noticeMessageService.getActiveById(id);
        if (entity == null) {
            return Result.error("通知消息不存在");
        }
        if (request == null) {
            return Result.error("请求体不能为空");
        }
        if (request.getSenderUserId() != null && !userExists(request.getSenderUserId())) {
            return Result.error("发送人不存在");
        }

        if (StringUtils.hasText(request.getNoticeType())) {
            entity.setNoticeType(request.getNoticeType());
        }
        if (StringUtils.hasText(request.getTitle())) {
            entity.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            entity.setContent(request.getContent());
        }
        if (request.getSenderUserId() != null) {
            entity.setSenderUserId(request.getSenderUserId());
        }
        if (StringUtils.hasText(request.getBizType())) {
            entity.setBizType(request.getBizType());
        }
        if (request.getBizId() != null) {
            entity.setBizId(request.getBizId());
        }
        if (StringUtils.hasText(request.getChannelType())) {
            entity.setChannelType(request.getChannelType());
        }
        if (request.getPriorityLevel() != null) {
            entity.setPriorityLevel(request.getPriorityLevel());
        }
        if (StringUtils.hasText(request.getPublishStatus())) {
            entity.setPublishStatus(request.getPublishStatus());
        }
        if (request.getSendAt() != null) {
            entity.setSendAt(request.getSendAt());
        }
        if (request.getExpireAt() != null) {
            entity.setExpireAt(request.getExpireAt());
        }
        if (StringUtils.hasText(request.getRemark())) {
            entity.setRemark(request.getRemark());
        }

        noticeMessageService.updateById(entity);
        return Result.success(entity);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知消息")
    @Transactional
    public Result<String> delete(@PathVariable Long id) {
        NoticeMessage entity = noticeMessageService.getActiveById(id);
        if (entity == null) {
            return Result.error("通知消息不存在");
        }
        entity.setIsDeleted(1);
        noticeMessageService.updateById(entity);
        return Result.success("删除成功");
    }

    private boolean userExists(Long userId) {
        SysUser user = sysUserService.getById(userId);
        return user != null && (user.getIsDeleted() == null || user.getIsDeleted() == 0);
    }

    private String validateForCreate(NoticeMessage request) {
        if (request == null) {
            return "请求体不能为空";
        }
        if (!StringUtils.hasText(request.getNoticeType())) {
            return "通知类型不能为空";
        }
        if (!StringUtils.hasText(request.getTitle())) {
            return "通知标题不能为空";
        }
        if (!StringUtils.hasText(request.getContent())) {
            return "通知内容不能为空";
        }
        if (!StringUtils.hasText(request.getChannelType())) {
            return "发送渠道不能为空";
        }
        return null;
    }
}
