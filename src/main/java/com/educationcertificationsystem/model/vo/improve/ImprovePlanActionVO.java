package com.educationcertificationsystem.model.vo.improve;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Data;

@Data
public class ImprovePlanActionVO {

    private Long id;

    private Long planId;

    private String actionCode;

    private String actionTitle;

    private String actionDesc;

    private Long responsibleUserId;

    private String responsibleUserName;

    private LocalDate startDate;

    private LocalDate dueDate;

    private BigDecimal progressPercent;

    private String status;

    private Integer sortNo;

    private Integer overdueFlag;

    private Integer recordCount;

    private String remark;

    private List<ImprovePlanRecordVO> records;
}
