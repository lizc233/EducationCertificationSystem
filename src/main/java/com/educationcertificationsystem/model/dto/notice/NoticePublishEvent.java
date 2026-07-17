package com.educationcertificationsystem.model.dto.notice;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticePublishEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long noticeId;

    private List<Long> recipientUserIds;

    private Long operatorUserId;

    private String remark;

    private LocalDateTime publishTime;
}
