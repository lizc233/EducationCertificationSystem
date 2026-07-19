package com.educationcertificationsystem.controller;

import com.educationcertificationsystem.auth.RequireRoles;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.common.ApiResponse;
import com.educationcertificationsystem.common.PageResult;
import com.educationcertificationsystem.service.NoticeCenterService;
import com.educationcertificationsystem.vo.notice.NoticeInboxItemVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notice/recipients")
@RequireRoles({RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT})
public class NoticeController {

    private final NoticeCenterService noticeCenterService;

    public NoticeController(NoticeCenterService noticeCenterService) {
        this.noticeCenterService = noticeCenterService;
    }

    @GetMapping("/inbox")
    public ApiResponse<PageResult<NoticeInboxItemVO>> inbox(
        @RequestParam Long recipientUserId,
        @RequestParam(defaultValue = "1") Long pageNum,
        @RequestParam(defaultValue = "12") Long pageSize,
        @RequestParam(required = false) Integer readStatus,
        @RequestParam(defaultValue = "") String noticeType,
        @RequestParam(defaultValue = "") String title
    ) {
        return ApiResponse.success(noticeCenterService.inbox(
            recipientUserId,
            pageNum,
            pageSize,
            readStatus,
            noticeType,
            title
        ));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> unreadCount(@RequestParam Long recipientUserId) {
        return ApiResponse.success(noticeCenterService.unreadCount(recipientUserId));
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        noticeCenterService.markRead(id);
        return ApiResponse.success();
    }

    @PutMapping("/read-all")
    public ApiResponse<Void> markAllRead(@RequestParam Long recipientUserId) {
        noticeCenterService.markAllRead(recipientUserId);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        noticeCenterService.deleteRecipient(id);
        return ApiResponse.success();
    }
}
