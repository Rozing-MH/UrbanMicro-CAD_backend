package com.urbanmicrocad.project.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;
import com.urbanmicrocad.project.dto.ProjectDTO;
import com.urbanmicrocad.project.dto.ProjectSnapshotDTO;
import com.urbanmicrocad.project.dto.ProjectSummaryDTO;
import com.urbanmicrocad.project.entity.Project;
import com.urbanmicrocad.project.entity.ProjectSnapshot;
import org.springframework.stereotype.Component;

/**
 * 工程 Entity ↔ DTO 转换器。
 * 对齐设计文档 3.2 后端包图 — Converter 转换层。
 */
@Component
public class ProjectConverter {

    public ProjectDTO toDto(Project project) {
        return new ProjectDTO(
            project.getId(),
            project.getName(),
            defaultString(project.getDescription()),
            project.getTopologyData() == null ? JsonNodeTypeHandler.emptyObject() : project.getTopologyData(),
            project.getRuleData() == null ? defaultRuleData() : project.getRuleData(),
            project.getVersion(),
            project.getCreatedAt(),
            project.getUpdatedAt(),
            project.getUserId() == null ? null : project.getUserId().toString(),
            null
        );
    }

    public ProjectSummaryDTO toSummaryDto(Project project) {
        return new ProjectSummaryDTO(
            project.getId(),
            project.getName(),
            defaultString(project.getDescription()),
            project.getVersion(),
            project.getCreatedAt(),
            project.getUpdatedAt(),
            project.getUserId() == null ? null : project.getUserId().toString(),
            null
        );
    }

    public ProjectSnapshotDTO toSnapshotDto(ProjectSnapshot snapshot, boolean includeData) {
        return new ProjectSnapshotDTO(
            snapshot.getId(),
            snapshot.getProjectId(),
            snapshot.getVersion(),
            defaultString(snapshot.getDescription()),
            snapshot.getCreatedAt(),
            snapshot.getUpdatedAt(),
            includeData ? snapshot.getSnapshotData() : null
        );
    }

    private static String defaultString(String value) {
        return value == null ? "" : value;
    }

    private static JsonNode defaultRuleData() {
        return JsonNodeTypeHandler.emptyObject();
    }
}
