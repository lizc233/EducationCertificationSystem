package com.educationcertificationsystem.model.dto.notice;

import java.util.List;
import lombok.Data;

@Data
public class NoticePublishRequest {

    private List<Long> recipientUserIds;

    private Long operatorUserId;

    private String remark;
}
