package com.urbanmicrocad.template.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.entity.ProjectTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateConverterTest {

    private final TemplateConverter converter = new TemplateConverter();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("topology/rules 键名规范化为 topologyData/ruleData")
    void normalizeLegacyKeys() {
        String json = """
            {
              "topology": { "nodes": [], "segments": [] },
              "rules": { "ruleSets": [] }
            }
            """;
        ProjectTemplate template = templateWithSnapshot(json);

        TemplateDTO dto = converter.toDto(template);

        assertThat(dto.snapshotData().has("topologyData")).isTrue();
        assertThat(dto.snapshotData().has("ruleData")).isTrue();
        assertThat(dto.snapshotData().has("topology")).isFalse();
        assertThat(dto.snapshotData().has("rules")).isFalse();
    }

    @Test
    @DisplayName("已有 topologyData/ruleData 键名不重复转换")
    void noOpWhenCanonicalKeysExist() {
        String json = """
            {
              "topologyData": { "nodes": [], "segments": [] },
              "ruleData": { "ruleSets": [] }
            }
            """;
        ProjectTemplate template = templateWithSnapshot(json);

        TemplateDTO dto = converter.toDto(template);

        assertThat(dto.snapshotData().has("topologyData")).isTrue();
        assertThat(dto.snapshotData().has("ruleData")).isTrue();
        assertThat(dto.snapshotData().has("topology")).isFalse();
        assertThat(dto.snapshotData().has("rules")).isFalse();
    }

    @Test
    @DisplayName("同时存在 topology 和 topologyData 时保留 topologyData")
    void prefersCanonicalKeyWhenBothExist() {
        String json = """
            {
              "topology": { "nodes": ["old"], "segments": [] },
              "topologyData": { "nodes": ["new"], "segments": [] },
              "rules": { "ruleSets": [] },
              "ruleData": { "ruleSets": [] }
            }
            """;
        ProjectTemplate template = templateWithSnapshot(json);

        TemplateDTO dto = converter.toDto(template);

        assertThat(dto.snapshotData().get("topologyData").get("nodes").get(0).asText()).isEqualTo("new");
        assertThat(dto.snapshotData().has("topology")).isTrue();
        assertThat(dto.snapshotData().has("rules")).isTrue();
    }

    @Test
    @DisplayName("snapshotData 为 null 时安全返回 null")
    void nullSnapshotReturnsNull() {
        ProjectTemplate template = new ProjectTemplate();
        template.setId(UUID.randomUUID());
        template.setName("empty");
        template.setCategory("CUSTOM");

        TemplateDTO dto = converter.toDto(template);

        assertThat(dto.snapshotData()).isNull();
    }

    @Test
    @DisplayName("toCustomDto 同样规范化键名")
    void toCustomDtoAlsoNormalizesKeys() {
        String json = """
            {
              "topology": { "nodes": [], "segments": [] },
              "rules": { "ruleSets": [] }
            }
            """;
        ProjectTemplate template = templateWithSnapshot(json);

        TemplateDTO dto = converter.toCustomDto(template, null, "CUSTOM");

        assertThat(dto.snapshotData().has("topologyData")).isTrue();
        assertThat(dto.snapshotData().has("ruleData")).isTrue();
    }

    @Test
    @DisplayName("profile 字段从规范化后的 snapshotData 提取")
    void profileExtractedFromNormalizedSnapshot() {
        String json = """
            {
              "topology": { "nodes": [] },
              "rules": { "ruleSets": [] },
              "profile": { "lanes": [], "totalWidth": 12.0 }
            }
            """;
        ProjectTemplate template = templateWithSnapshot(json);

        TemplateDTO dto = converter.toDto(template);

        assertThat(dto.profile()).isNotNull();
        assertThat(dto.profile().get("totalWidth").asDouble()).isEqualTo(12.0);
    }

    private ProjectTemplate templateWithSnapshot(String json) {
        try {
            ProjectTemplate template = new ProjectTemplate();
            template.setId(UUID.randomUUID());
            template.setName("test-template");
            template.setCategory("BASIC_INTERSECTION");
            template.setSnapshotData(objectMapper.readTree(json));
            return template;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
