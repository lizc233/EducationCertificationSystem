package com.educationcertificationsystem.vo.notice;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NoticeInboxItemVO {

    private Long recipientId;

    private Long noticeId;

    private String noticeType;

    private String title;

    private String content;

    private String bizType;

    private Long bizId;

    private String channelType;

    private Integer readStatus;

    private LocalDateTime readAt;

    private LocalDateTime sendAt;

    private LocalDateTime expireAt;

    private LocalDateTime recipientCreatedAt;
}
