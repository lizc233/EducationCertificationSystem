package com.educationcertificationsystem.notice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.educationcertificationsystem.common.Result;
import com.educationcertificationsystem.model.entity.NoticeMessage;
import com.educationcertificationsystem.model.entity.NoticeRecipient;
import com.educationcertificationsystem.model.entity.SysUser;
import com.educationcertificationsystem.model.vo.notice.NoticeInboxItem;
import com.educationcertificationsystem.notice.service.NoticeMessageService;
import com.educationcertificationsystem.notice.service.NoticeRecipientService;
import com.educationcertificationsystem.user.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.annotation.Transactional;
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
@Tag(name = "通知接收人管理")
@Profile("generated-notice")
@RequestMapping("/notice/recipients")
public class NoticeRecipientController {

    private final NoticeRecipientService noticeRecipientService;
    private final NoticeMessageService noticeMessageService;
    private final SysUserService sysUserService;

    @PostMapping
    @Operation(summary = "新增通知接收人")
    @Transactional
    public Result<NoticeRecipient> create(@RequestBody NoticeRecipient request) {
        String error = validateForCreate(request);
        if (error != null) {
            return Result.error(error);
        }

        NoticeRecipient existing = noticeRecipientService.getOne(new LambdaQueryWrapper<NoticeRecipient>()
                .eq(NoticeRecipient::getNoticeId, request.getNoticeId())
                .eq(NoticeRecipient::getRecipientUserId, request.getRecipientUserId())
                .last("LIMIT 1"), false);
        if (existing != null && isActive(existing)) {
            return Result.error("该通知接收记录已存在");
        }

        NoticeRecipient entity = existing != null ? existing : new NoticeRecipient();
        entity.setNoticeId(request.getNoticeId());
        entity.setRecipientUserId(request.getRecipientUserId());
        entity.setReadStatus(request.getReadStatus() == null ? 0 : request.getReadStatus());
        entity.setReadAt(request.getReadAt());
        entity.setDeletedFlag(0);
        entity.setIsDeleted(0);
        entity.setRemark(request.getRemark());

        if (entity.getId() == null) {
            noticeRecipientService.save(entity);
        } else {
            noticeRecipientService.updateById(entity);
        }
        return Result.success(entity);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询通知接收记录")
    public Result<NoticeRecipient> getById(@PathVariable Long id) {
        NoticeRecipient entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知接收记录不存在");
        }
        return Result.success(entity);
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询通知接收人列表")
    public Result<Page<NoticeRecipient>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long noticeId,
            @RequestParam(required = false) Long recipientUserId,
            @RequestParam(required = false) Integer readStatus) {
        long current = Math.max(pageNum, 1);
        long size = Math.max(pageSize, 1);
        LambdaQueryWrapper<NoticeRecipient> wrapper = baseWrapper()
                .eq(noticeId != null, NoticeRecipient::getNoticeId, noticeId)
                .eq(recipientUserId != null, NoticeRecipient::getRecipientUserId, recipientUserId)
                .eq(readStatus != null, NoticeRecipient::getReadStatus, readStatus)
                .orderByDesc(NoticeRecipient::getId);
        long total = noticeRecipientService.count(wrapper);
        LambdaQueryWrapper<NoticeRecipient> pageWrapper = baseWrapper()
                .eq(noticeId != null, NoticeRecipient::getNoticeId, noticeId)
                .eq(recipientUserId != null, NoticeRecipient::getRecipientUserId, recipientUserId)
                .eq(readStatus != null, NoticeRecipient::getReadStatus, readStatus)
                .orderByDesc(NoticeRecipient::getId);
        pageWrapper.last("LIMIT " + (current - 1) * size + "," + size);
        Page<NoticeRecipient> page = new Page<>(current, size);
        page.setTotal(total);
        page.setRecords(noticeRecipientService.list(pageWrapper));
        return Result.success(page);
    }

    @GetMapping("/inbox")
    @Operation(summary = "查询通知收件箱")
    public Result<Page<NoticeInboxItem>> inbox(
            @RequestParam Long recipientUserId,
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Integer readStatus,
            @RequestParam(required = false) String noticeType,
            @RequestParam(required = false) String title) {
        return Result.success(noticeRecipientService.pageInbox(
                pageNum, pageSize, recipientUserId, readStatus, noticeType, title));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新通知接收记录")
    @Transactional
    public Result<NoticeRecipient> update(@PathVariable Long id, @RequestBody NoticeRecipient request) {
        NoticeRecipient entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知接收记录不存在");
        }
        String error = validateForCreate(request);
        if (error != null) {
            return Result.error(error);
        }

        NoticeRecipient duplicate = noticeRecipientService.getOne(new LambdaQueryWrapper<NoticeRecipient>()
                .eq(NoticeRecipient::getNoticeId, request.getNoticeId())
                .eq(NoticeRecipient::getRecipientUserId, request.getRecipientUserId())
                .ne(NoticeRecipient::getId, id)
                .eq(NoticeRecipient::getIsDeleted, 0)
                .last("LIMIT 1"), false);
        if (duplicate != null) {
            return Result.error("该通知接收人组合已存在");
        }

        entity.setNoticeId(request.getNoticeId());
        entity.setRecipientUserId(request.getRecipientUserId());
        if (request.getReadStatus() != null) {
            entity.setReadStatus(request.getReadStatus());
        }
        if (request.getReadAt() != null) {
            entity.setReadAt(request.getReadAt());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }

        noticeRecipientService.updateById(entity);
        return Result.success(entity);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记通知已读")
    @Transactional
    public Result<NoticeRecipient> markAsRead(@PathVariable Long id) {
        NoticeRecipient entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知接收记录不存在");
        }
        entity.setReadStatus(1);
        entity.setReadAt(LocalDateTime.now());
        noticeRecipientService.updateById(entity);
        return Result.success(entity);
    }

    @PutMapping("/read-all")
    @Operation(summary = "标记全部通知已读")
    @Transactional
    public Result<Integer> markAllRead(@RequestParam Long recipientUserId) {
        return Result.success(noticeRecipientService.markAllRead(recipientUserId));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "查询未读数量")
    public Result<Long> unreadCount(@RequestParam Long recipientUserId) {
        return Result.success(noticeRecipientService.countUnread(recipientUserId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除通知接收记录")
    @Transactional
    public Result<String> delete(@PathVariable Long id) {
        NoticeRecipient entity = getActiveById(id);
        if (entity == null) {
            return Result.error("通知接收记录不存在");
        }
        entity.setDeletedFlag(1);
        entity.setIsDeleted(1);
        noticeRecipientService.updateById(entity);
        return Result.success("删除成功");
    }

    private LambdaQueryWrapper<NoticeRecipient> baseWrapper() {
        return new LambdaQueryWrapper<NoticeRecipient>()
                .eq(NoticeRecipient::getIsDeleted, 0)
                .eq(NoticeRecipient::getDeletedFlag, 0);
    }

    private NoticeRecipient getActiveById(Long id) {
        NoticeRecipient entity = noticeRecipientService.getById(id);
        return isActive(entity) ? entity : null;
    }

    private boolean isActive(NoticeRecipient entity) {
        return entity != null
                && (entity.getIsDeleted() == null || entity.getIsDeleted() == 0)
                && (entity.getDeletedFlag() == null || entity.getDeletedFlag() == 0);
    }

    private String validateForCreate(NoticeRecipient request) {
        if (request == null) {
            return "请求体不能为空";
        }
        if (request.getNoticeId() == null) {
            return "通知消息ID不能为空";
        }
        if (request.getRecipientUserId() == null) {
            return "接收人用户ID不能为空";
        }
        NoticeMessage noticeMessage = noticeMessageService.getById(request.getNoticeId());
        if (noticeMessage == null || (noticeMessage.getIsDeleted() != null && noticeMessage.getIsDeleted() != 0)) {
            return "通知消息不存在";
        }
        SysUser user = sysUserService.getById(request.getRecipientUserId());
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() != 0)) {
            return "接收人用户不存在";
        }
        return null;
    }
}
