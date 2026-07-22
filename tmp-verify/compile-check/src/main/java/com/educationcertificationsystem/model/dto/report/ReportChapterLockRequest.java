package com.educationcertificationsystem.model.dto.report;

import lombok.Data;

@Data
public class ReportChapterLockRequest {

    private Integer lockedFlag;

    private String remark;
}
