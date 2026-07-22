package com.educationcertificationsystem.model.vo.notice;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NoticeInboxItem {

    private Long recipientId;

    private Long noticeId;

    private Long recipientUserId;

    private Integer readStatus;

    private LocalDateTime readAt;

    private LocalDateTime recipientCreatedAt;

    private String noticeType;

    private String title;

    private String content;

    private Long senderUserId;

    private String bizType;

    private Long bizId;

    private String channelType;

    private Integer priorityLevel;

    private String publishStatus;

    private LocalDateTime sendAt;

    private LocalDateTime expireAt;

    private String remark;
}
