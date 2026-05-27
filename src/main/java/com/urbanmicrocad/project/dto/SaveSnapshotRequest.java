package com.urbanmicrocad.project.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveSnapshotRequest(
    @NotNull JsonNode topologyData,
    @NotNull JsonNode ruleData,
    @Size(max = 1000) String description
) {
}
