package com.educationcertificationsystem.model.dto.improve;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ImprovePlanActionProgressRequest {

    private BigDecimal progressPercent;

    private String status;

    private String remark;
}
