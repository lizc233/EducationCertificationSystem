package com.educationcertificationsystem.model.dto.eval;

import java.util.List;
import lombok.Data;

@Data
public class EvalGraduationWarningNotifyRequest {

    private List<Long> resultIds;

    private Long senderUserId;

    private Long operatorUserId;

    private String remark;
}
