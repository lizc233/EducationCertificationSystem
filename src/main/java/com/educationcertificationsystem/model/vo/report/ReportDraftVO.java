package com.educationcertificationsystem.model.vo.report;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReportDraftVO {

    private Long id;

    private Long chapterId;

    private Integer versionNo;

    private String draftContent;

    private Long editedBy;

    private String editedByName;

    private LocalDateTime editedAt;

    private Integer lockFlag;

    private String remark;
}
