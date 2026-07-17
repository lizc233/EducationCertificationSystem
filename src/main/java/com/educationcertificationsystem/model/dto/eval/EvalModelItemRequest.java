package com.educationcertificationsystem.model.dto.eval;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class EvalModelItemRequest {

    private String itemCode;

    private String itemName;

    private String itemType;

    private BigDecimal weightPercent;

    private BigDecimal thresholdValue;

    private String calcRule;

    private Integer sortNo;

    private Integer enabled;

    private String remark;
}
