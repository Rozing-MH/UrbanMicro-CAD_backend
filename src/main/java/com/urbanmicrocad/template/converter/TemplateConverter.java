package com.urbanmicrocad.template.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.entity.ProjectTemplate;
import org.springframework.stereotype.Component;

/**
 * 模板 Entity ↔ DTO 转换器。
 * 对齐设计文档 3.2 后端包图 — Converter 转换层。
 */
@Component
public class TemplateConverter {

    public TemplateDTO toDto(ProjectTemplate template) {
        JsonNode profile = null;
        if (template.getSnapshotData() != null && template.getSnapshotData().has("profile")) {
            profile = template.getSnapshotData().get("profile");
        }
        return new TemplateDTO(
            template.getId(),
            template.getName(),
            template.getCategory(),
            template.getSnapshotData(),
            template.getThumbnailUrl(),
            profile
        );
    }

    public TemplateDTO toCustomDto(ProjectTemplate template, JsonNode profile, String category) {
        return new TemplateDTO(
            template.getId(),
            template.getName(),
            category,
            template.getSnapshotData(),
            template.getThumbnailUrl(),
            profile
        );
    }
}
