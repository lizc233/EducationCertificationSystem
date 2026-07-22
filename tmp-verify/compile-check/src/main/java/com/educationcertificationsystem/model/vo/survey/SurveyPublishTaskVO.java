package com.educationcertificationsystem.model.vo.survey;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SurveyPublishTaskVO {

    private Long id;

    private Long questionnaireId;

    private String publishBatchNo;

    private String publishStatus;

    private String mqStatus;

    private Integer retryCount;

    private LocalDateTime publishedAt;

    private LocalDateTime revokedAt;

    private String errorMessage;

    private LocalDateTime createdAt;

    private String remark;
}
