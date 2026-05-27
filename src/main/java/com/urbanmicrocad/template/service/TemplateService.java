package com.urbanmicrocad.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.entity.ProjectTemplate;
import com.urbanmicrocad.template.mapper.TemplateMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TemplateService {
    private static final Set<String> VALID_CATEGORIES = Set.of(
        "BASIC_INTERSECTION",
        "CLOVERLEAF",
        "TURBINE",
        "DIAMOND",
        "ROUNDABOUT",
        "CUSTOM"
    );

    private final TemplateMapper templateMapper;

    public TemplateService(TemplateMapper templateMapper) {
        this.templateMapper = templateMapper;
    }

    public List<TemplateDTO> list(String category) {
        if (category != null && !category.isBlank() && !VALID_CATEGORIES.contains(category)) {
            return List.of();
        }
        LambdaQueryWrapper<ProjectTemplate> query = activeTemplateQuery();
        if (category != null && !category.isBlank()) {
            query.eq(ProjectTemplate::getCategory, category);
        }
        return templateMapper.selectList(query).stream()
            .map(this::toDto)
            .toList();
    }

    public TemplateDTO get(UUID id) {
        ProjectTemplate template = templateMapper.selectOne(activeTemplateQuery().eq(ProjectTemplate::getId, id));
        if (template == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "模板不存在");
        }
        return toDto(template);
    }

    private LambdaQueryWrapper<ProjectTemplate> activeTemplateQuery() {
        return new LambdaQueryWrapper<ProjectTemplate>()
            .eq(ProjectTemplate::getIsDeleted, false)
            .orderByDesc(ProjectTemplate::getUpdatedAt);
    }

    private TemplateDTO toDto(ProjectTemplate template) {
        return new TemplateDTO(
            template.getId(),
            template.getName(),
            template.getCategory(),
            template.getSnapshotData(),
            template.getThumbnailUrl(),
            null
        );
    }
}
