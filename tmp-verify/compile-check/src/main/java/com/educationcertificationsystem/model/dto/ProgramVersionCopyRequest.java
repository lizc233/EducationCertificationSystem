package com.educationcertificationsystem.model.dto;

import lombok.Data;

@Data
public class ProgramVersionCopyRequest {
    private String versionNo;
    private String versionName;
    private Long majorId;
}
