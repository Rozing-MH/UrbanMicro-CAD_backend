package com.urbanmicrocad.template.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 保存用户自定义断面模板请求。
 * 不含 category 字段 — Service 层强制设为 CUSTOM，防止权限提升。
 */
public record SaveCustomTemplateRequest(
    @NotBlank String name,
    @NotNull JsonNode profile
) {
}
