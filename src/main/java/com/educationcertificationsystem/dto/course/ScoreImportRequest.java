package com.educationcertificationsystem.dto.course;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

/**
 * F15 成绩批量导入请求：指定批次，携带若干学生成绩条目。
 */
@Data
public class ScoreImportRequest {

    private Long batchId;

    private List<Item> items;

    @Data
    public static class Item {
        private Long studentId;
        private BigDecimal rawScore;
        private String sourceType;
        private Long sourceRefId;
        private String remark;
    }
}
