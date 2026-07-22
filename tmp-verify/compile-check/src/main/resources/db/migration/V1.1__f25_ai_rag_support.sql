CREATE TABLE IF NOT EXISTS ai_knowledge_document (
                                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                                     source_type VARCHAR(50) NOT NULL,
                                                     source_id BIGINT NOT NULL,
                                                     title VARCHAR(200) NOT NULL,
                                                     summary VARCHAR(1000) DEFAULT NULL,
                                                     biz_scope VARCHAR(100) DEFAULT NULL,
                                                     content_checksum VARCHAR(64) DEFAULT NULL,
                                                     index_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                                     last_indexed_at DATETIME DEFAULT NULL,
                                                     version_no INT NOT NULL DEFAULT 1,
                                                     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                     updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                     is_deleted TINYINT NOT NULL DEFAULT 0,
                                                     remark VARCHAR(500) DEFAULT NULL,
                                                     PRIMARY KEY (id),
                                                     UNIQUE KEY uk_ai_knowledge_document_source (source_type, source_id),
                                                     KEY idx_ai_knowledge_document_scope (biz_scope)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识文档表';

CREATE TABLE IF NOT EXISTS ai_knowledge_chunk (
                                                  id BIGINT NOT NULL AUTO_INCREMENT,
                                                  document_id BIGINT NOT NULL,
                                                  chunk_no INT NOT NULL,
                                                  chunk_text LONGTEXT NOT NULL,
                                                  metadata_json JSON DEFAULT NULL,
                                                  qdrant_point_id VARCHAR(100) DEFAULT NULL,
                                                  embedding_model VARCHAR(100) DEFAULT NULL,
                                                  index_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                                  last_indexed_at DATETIME DEFAULT NULL,
                                                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                  is_deleted TINYINT NOT NULL DEFAULT 0,
                                                  remark VARCHAR(500) DEFAULT NULL,
                                                  PRIMARY KEY (id),
                                                  UNIQUE KEY uk_ai_knowledge_chunk_no (document_id, chunk_no),
                                                  KEY idx_ai_knowledge_chunk_point (qdrant_point_id),
                                                  CONSTRAINT fk_ai_knowledge_chunk_document FOREIGN KEY (document_id) REFERENCES ai_knowledge_document (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI知识分块表';

INSERT INTO ai_prompt_template (
    template_code, template_name, scenario_type, system_prompt, user_prompt,
    input_schema_json, output_schema_json, enabled, remark
) VALUES (
    'REPORT_CHAPTER_EXPAND',
    'Report Chapter Expand',
    'REPORT_CHAPTER_EXPAND',
    'You are an assistant for engineering education accreditation reports. Expand the chapter using only the provided business facts and retrieved knowledge. Do not fabricate data. Keep the tone formal and usable in a self-evaluation report.',
    'Task: expand a report chapter.\nBusiness context:\n{{businessContext}}\n\nRetrieved knowledge:\n{{retrievedContext}}\n\nRequirements:\n1. Produce polished Chinese prose.\n2. Keep facts consistent with context.\n3. Include a short rationale summary at the end prefixed with [RATIONALE].',
    '{"type":"object","properties":{"chapterId":{"type":"integer"},"requesterUserId":{"type":"integer"}}}',
    '{"type":"object","properties":{"content":{"type":"string"},"rationale":{"type":"string"}}}',
    1,
    'F25 default template'
) ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    scenario_type = VALUES(scenario_type),
    system_prompt = VALUES(system_prompt),
    user_prompt = VALUES(user_prompt),
    input_schema_json = VALUES(input_schema_json),
    output_schema_json = VALUES(output_schema_json),
    enabled = VALUES(enabled),
    remark = VALUES(remark);

INSERT INTO ai_prompt_template (
    template_code, template_name, scenario_type, system_prompt, user_prompt,
    input_schema_json, output_schema_json, enabled, remark
) VALUES (
    'REPORT_CHAPTER_POLISH',
    'Report Chapter Polish',
    'REPORT_CHAPTER_POLISH',
    'You are an assistant for engineering education accreditation reports. Polish and reorganize the supplied chapter content while preserving the original facts and intent. Do not invent data.',
    'Task: polish a report chapter.\nBusiness context:\n{{businessContext}}\n\nRetrieved knowledge:\n{{retrievedContext}}\n\nRequirements:\n1. Improve structure, wording and readability.\n2. Preserve factual accuracy.\n3. Output polished Chinese prose only, then append [RATIONALE] with a short explanation.',
    '{"type":"object","properties":{"chapterId":{"type":"integer"},"requesterUserId":{"type":"integer"}}}',
    '{"type":"object","properties":{"content":{"type":"string"},"rationale":{"type":"string"}}}',
    1,
    'F25 default template'
) ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    scenario_type = VALUES(scenario_type),
    system_prompt = VALUES(system_prompt),
    user_prompt = VALUES(user_prompt),
    input_schema_json = VALUES(input_schema_json),
    output_schema_json = VALUES(output_schema_json),
    enabled = VALUES(enabled),
    remark = VALUES(remark);

INSERT INTO ai_prompt_template (
    template_code, template_name, scenario_type, system_prompt, user_prompt,
    input_schema_json, output_schema_json, enabled, remark
) VALUES (
    'IMPROVE_PLAN_SUGGEST',
    'Improve Plan Suggest',
    'IMPROVE_PLAN_SUGGEST',
    'You are an assistant for continuous improvement in engineering education accreditation. Generate actionable improvement plans strictly from the supplied evidence and retrieved historical cases. Do not fabricate metrics.',
    'Task: generate a continuous improvement suggestion.\nBusiness context:\n{{businessContext}}\n\nRetrieved knowledge:\n{{retrievedContext}}\n\nRequirements:\n1. Return valid JSON only.\n2. JSON fields: planName, planSummary, priority, dueDays, actions.\n3. Each action includes actionTitle, actionDesc, dueDaysOffset.\n4. Keep suggestions concrete and implementable.',
    '{"type":"object","properties":{"sourceType":{"type":"string"},"sourceId":{"type":"integer"},"requesterUserId":{"type":"integer"}}}',
    '{"type":"object","properties":{"planName":{"type":"string"},"planSummary":{"type":"string"},"priority":{"type":"integer"},"dueDays":{"type":"integer"},"actions":{"type":"array"}}}',
    1,
    'F25 default template'
) ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    scenario_type = VALUES(scenario_type),
    system_prompt = VALUES(system_prompt),
    user_prompt = VALUES(user_prompt),
    input_schema_json = VALUES(input_schema_json),
    output_schema_json = VALUES(output_schema_json),
    enabled = VALUES(enabled),
    remark = VALUES(remark);
