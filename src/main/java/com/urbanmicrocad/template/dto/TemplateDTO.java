package com.urbanmicrocad.template.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

public record TemplateDTO(
    UUID id,
    String name,
    String category,
    JsonNode snapshotData,
    String thumbnailUrl,
    JsonNode profile
) {
}
