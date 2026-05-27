package com.urbanmicrocad.report.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportSummary(
    UUID id,
    UUID projectId,
    OffsetDateTime createdAt,
    String networkLOS,
    double averageDelay
) {
}
