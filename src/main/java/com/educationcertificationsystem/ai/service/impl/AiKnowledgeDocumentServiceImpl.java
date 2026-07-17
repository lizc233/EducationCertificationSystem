package com.educationcertificationsystem.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.educationcertificationsystem.ai.mapper.AiKnowledgeDocumentMapper;
import com.educationcertificationsystem.ai.service.AiKnowledgeDocumentService;
import com.educationcertificationsystem.model.entity.AiKnowledgeDocument;
import org.springframework.stereotype.Service;

@Service
public class AiKnowledgeDocumentServiceImpl extends ServiceImpl<AiKnowledgeDocumentMapper, AiKnowledgeDocument>
        implements AiKnowledgeDocumentService {
}
