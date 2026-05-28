package com.urbanmicrocad.template.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.urbanmicrocad.common.response.ApiResponse;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.service.TemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
public class TemplateController {
    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @GetMapping
    public ApiResponse<List<TemplateDTO>> list(@RequestParam(required = false) String category) {
        return ApiResponse.ok(templateService.list(category));
    }

    @GetMapping("/cross-sections")
    public ApiResponse<List<JsonNode>> listCrossSections() {
        return ApiResponse.ok(templateService.listCrossSections());
    }

    @GetMapping("/cross-sections/{id}")
    public ApiResponse<JsonNode> getCrossSection(@PathVariable String id) {
        return ApiResponse.ok(templateService.getCrossSection(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateDTO> get(@PathVariable UUID id) {
        return ApiResponse.ok(templateService.get(id));
    }
}
