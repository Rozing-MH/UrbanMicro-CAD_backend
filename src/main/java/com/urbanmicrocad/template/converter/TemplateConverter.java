package com.urbanmicrocad.template.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.entity.ProjectTemplate;
import org.springframework.stereotype.Component;

/**
 * 模板 Entity ↔ DTO 转换器。
 * 对齐设计文档 3.2 后端包图 — Converter 转换层。
 */
@Component
public class TemplateConverter {

    /**
     * 规范化 snapshotData 键名，对齐前端 ProjectPayload 契约。
     * 数据库种子数据使用 "topology"/"rules"，前端期望 "topologyData"/"ruleData"。
     */
    private JsonNode normalizeSnapshotKeys(JsonNode snapshotData) {
        if (snapshotData == null || !snapshotData.isObject()) {
            return snapshotData;
        }
        boolean needsTopologyRename = snapshotData.has("topology") && !snapshotData.has("topologyData");
        boolean needsRulesRename = snapshotData.has("rules") && !snapshotData.has("ruleData");

        if (!needsTopologyRename && !needsRulesRename) {
            return snapshotData;
        }

        ObjectNode normalized = snapshotData.deepCopy();
        if (needsTopologyRename) {
            normalized.set("topologyData", normalized.remove("topology"));
        }
        if (needsRulesRename) {
            normalized.set("ruleData", normalized.remove("rules"));
        }
        return normalized;
    }

    public TemplateDTO toDto(ProjectTemplate template) {
        JsonNode normalized = normalizeSnapshotKeys(template.getSnapshotData());
        JsonNode profile = null;
        if (normalized != null && normalized.has("profile")) {
            profile = normalized.get("profile");
        }
        return new TemplateDTO(
            template.getId(),
            template.getName(),
            template.getCategory(),
            normalized,
            template.getThumbnailUrl(),
            profile
        );
    }

    public TemplateDTO toCustomDto(ProjectTemplate template, JsonNode profile, String category) {
        return new TemplateDTO(
            template.getId(),
            template.getName(),
            category,
            normalizeSnapshotKeys(template.getSnapshotData()),
            template.getThumbnailUrl(),
            profile
        );
    }
}
