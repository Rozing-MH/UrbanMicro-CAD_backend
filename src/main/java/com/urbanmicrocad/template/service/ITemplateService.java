package com.urbanmicrocad.template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.template.dto.SaveCustomTemplateRequest;
import com.urbanmicrocad.template.dto.TemplateDTO;

import java.util.List;
import java.util.UUID;

/**
 * 模板服务接口。
 * 对齐设计文档 3.2 后端包图 — Service 接口化。
 */
public interface ITemplateService {

    /**
     * 模板列表，支持 category 过滤。
     * 包含系统模板 + 当前用户自定义模板。
     */
    List<TemplateDTO> list(String category, CurrentUser user);

    /**
     * 模板详情。
     *
     * @throws com.urbanmicrocad.common.exception.ApiException 模板不存在时抛 NOT_FOUND
     */
    TemplateDTO get(UUID id, CurrentUser user);

    /**
     * 保存用户自定义断面模板。category 强制 CUSTOM。
     *
     * @throws com.urbanmicrocad.common.exception.ApiException profile 无效或同名模板已存在
     */
    TemplateDTO saveCustomTemplate(CurrentUser user, SaveCustomTemplateRequest request);

    /**
     * 软删除用户自定义模板。仅模板所有者可删除，系统模板不可删除。
     *
     * @throws com.urbanmicrocad.common.exception.ApiException 模板不存在 / 系统模板不可删 / 非所有者
     */
    void deleteCustomTemplate(CurrentUser user, UUID id);

    /**
     * ROAD_SECTION 断面模板列表（后端内置数据）。
     */
    List<TemplateDTO> listCrossSectionTemplates();

    /**
     * 断面 profile 列表（前端 fallback 接口）。
     */
    List<JsonNode> listCrossSections();

    /**
     * 按 ID 获取断面 profile。
     *
     * @throws com.urbanmicrocad.common.exception.ApiException 断面模板不存在
     */
    JsonNode getCrossSection(String id);
}
