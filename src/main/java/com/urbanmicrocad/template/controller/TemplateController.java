package com.urbanmicrocad.template.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.response.ApiResponse;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.template.dto.SaveCustomTemplateRequest;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.service.TemplateService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<TemplateDTO>> list(
        @RequestParam(required = false) String category,
        @AuthenticationPrincipal CurrentUser user
    ) {
        return ApiResponse.ok(templateService.list(category, user));
    }

    @PostMapping
    public ApiResponse<TemplateDTO> saveCustomTemplate(
        @AuthenticationPrincipal CurrentUser user,
        @Valid @RequestBody SaveCustomTemplateRequest request
    ) {
        Objects.requireNonNull(user, "未认证用户不能保存模板");
        return ApiResponse.ok(templateService.saveCustomTemplate(user, request));
    }

    @GetMapping("/cross-sections")
    public ApiResponse<List<JsonNode>> listCrossSections() {
        return ApiResponse.ok(templateService.listCrossSections());
    }

    @GetMapping("/cross-sections/{id}")
    public ApiResponse<JsonNode> getCrossSection(@PathVariable String id) {
        return ApiResponse.ok(templateService.getCrossSection(id));
    }

    @GetMapping("/assets")
    public ApiResponse<List<TemplateDTO>> listAssets(
        @RequestParam(required = false) String category,
        @AuthenticationPrincipal CurrentUser user
    ) {
        return ApiResponse.ok(templateService.list(category, user));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateDTO> get(
        @PathVariable UUID id,
        @AuthenticationPrincipal CurrentUser user
    ) {
        return ApiResponse.ok(templateService.get(id, user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id
    ) {
        Objects.requireNonNull(user, "未认证用户不能删除模板");
        templateService.deleteCustomTemplate(user, id);
        return ApiResponse.ok();
    }
}
