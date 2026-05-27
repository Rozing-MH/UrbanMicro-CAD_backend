package com.urbanmicrocad.report.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ExportReportRequest(
    @NotNull UUID projectId,
    String format,
    JsonNode metrics,
    JsonNode laneMetrics,
    JsonNode intersectionLos,
    JsonNode heatmapConfig,
    JsonNode flightLineData,
    JsonNode snapshot
) {
}
