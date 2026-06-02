package com.urbanmicrocad.project.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 快照 DTO。列表端点不填充 snapshotData（保持响应轻量），
 * 版本加载端点通过独立的 ProjectDTO 返回完整数据。
 */
public record ProjectSnapshotDTO(
    UUID id,
    UUID projectId,
    Integer version,
    String description,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    JsonNode snapshotData
) {
}
