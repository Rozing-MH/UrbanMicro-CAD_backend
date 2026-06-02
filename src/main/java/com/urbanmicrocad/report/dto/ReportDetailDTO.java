package com.urbanmicrocad.report.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * 报表详情 DTO，包含完整指标数据。
 * 对齐设计文档 GET /api/reports/{id} 接口契约。
 */
public record ReportDetailDTO(
    UUID id,
    UUID projectId,
    OffsetDateTime createdAt,
    String networkLOS,
    double averageDelay,
    JsonNode laneMetrics,
    JsonNode intersectionLos,
    JsonNode heatmapConfig,
    JsonNode flightLineData
) {
}
