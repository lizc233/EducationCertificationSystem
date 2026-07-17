package com.educationcertificationsystem.model.dto.notice;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class NoticeSendRequest {

    private String noticeType;

    private String title;

    private String content;

    private Long senderUserId;

    private String bizType;

    private Long bizId;

    private String channelType;

    private Integer priorityLevel;

    private LocalDateTime sendAt;

    private LocalDateTime expireAt;

    private String remark;

    private List<Long> recipientUserIds;

    private Long operatorUserId;
}
