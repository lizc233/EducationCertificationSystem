package com.educationcertificationsystem.model.dto.survey;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyPublishEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long taskId;

    private Long questionnaireId;

    private String actionType;

    private Long operatorUserId;

    private String remark;

    private LocalDateTime dispatchTime;
}
