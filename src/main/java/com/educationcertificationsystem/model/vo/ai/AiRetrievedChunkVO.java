package com.educationcertificationsystem.model.vo.ai;

import lombok.Data;

@Data
public class AiRetrievedChunkVO {

    private Long chunkId;

    private String sourceType;

    private Long sourceId;

    private String title;

    private String snippet;

    private Double score;
}
