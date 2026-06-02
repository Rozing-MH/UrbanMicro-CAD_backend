package com.urbanmicrocad.project.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 项目列表轻量 DTO，不含 topologyData/ruleData 大字段。
 * 用于 GET /api/projects 列表响应，Dashboard 页面只需元数据。
 */
public record ProjectSummaryDTO(
    UUID id,
    String name,
    String description,
    Integer version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String ownerId,
    String thumbnailUrl
) {
}
