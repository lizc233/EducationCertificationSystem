package com.educationcertificationsystem.model.vo.improve;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ImprovePlanRecordVO {

    private Long id;

    private Long actionId;

    private String recordType;

    private String recordContent;

    private LocalDateTime recordTime;

    private Long recorderUserId;

    private String recorderUserName;

    private Long attachmentFileId;

    private String attachmentFileName;

    private String remark;
}
