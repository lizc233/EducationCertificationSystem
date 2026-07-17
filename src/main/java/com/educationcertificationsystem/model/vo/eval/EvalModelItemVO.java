package com.educationcertificationsystem.model.vo.eval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EvalModelItemVO {

    private Long id;

    private Long modelId;

    private String itemCode;

    private String itemName;

    private String itemType;

    private BigDecimal weightPercent;

    private BigDecimal thresholdValue;

    private String calcRule;

    private Integer sortNo;

    private Integer enabled;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
