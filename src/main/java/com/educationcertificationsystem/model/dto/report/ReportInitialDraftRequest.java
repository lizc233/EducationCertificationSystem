package com.educationcertificationsystem.model.dto.report;

import java.util.List;
import lombok.Data;

@Data
public class ReportInitialDraftRequest {

    private Long editedBy;

    private Integer overwriteEmptyOnly;

    private List<Long> chapterIds;

    private String remark;
}
