package com.urbanmicrocad.project.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectSnapshotDTO(
    UUID id,
    UUID projectId,
    Integer version,
    String description,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
