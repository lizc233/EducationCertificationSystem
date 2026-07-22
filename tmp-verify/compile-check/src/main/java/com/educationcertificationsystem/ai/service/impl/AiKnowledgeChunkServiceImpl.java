package com.educationcertificationsystem.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.ai.mapper.AiKnowledgeChunkMapper;
import com.educationcertificationsystem.ai.service.AiKnowledgeChunkService;
import com.educationcertificationsystem.model.entity.AiKnowledgeChunk;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeChunkServiceImpl extends ServiceImpl<AiKnowledgeChunkMapper, AiKnowledgeChunk>
        implements AiKnowledgeChunkService {
}
