package com.urbanmicrocad.project.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProjectDTO(
    UUID id,
    String name,
    String description,
    JsonNode topologyData,
    JsonNode ruleData,
    Integer version,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String ownerId,
    String thumbnailUrl
) {
}
