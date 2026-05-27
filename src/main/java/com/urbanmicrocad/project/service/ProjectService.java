package com.urbanmicrocad.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.common.config.JsonNodeTypeHandler;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.dto.CreateProjectRequest;
import com.urbanmicrocad.project.dto.ProjectDTO;
import com.urbanmicrocad.project.dto.SaveSnapshotRequest;
import com.urbanmicrocad.project.dto.UpdateProjectRequest;
import com.urbanmicrocad.project.entity.Project;
import com.urbanmicrocad.project.entity.ProjectSnapshot;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProjectService {
    private static final int MAX_LIST_SIZE = 100;
    private static final int MAX_SNAPSHOT_JSON_CHARS = 2_000_000;

    private final ProjectMapper projectMapper;
    private final ProjectSnapshotMapper snapshotMapper;
    private final ObjectMapper objectMapper;

    public ProjectService(ProjectMapper projectMapper, ProjectSnapshotMapper snapshotMapper, ObjectMapper objectMapper) {
        this.projectMapper = projectMapper;
        this.snapshotMapper = snapshotMapper;
        this.objectMapper = objectMapper;
    }

    public List<ProjectDTO> list(CurrentUser user) {
        return projectMapper.selectPage(new Page<>(1, MAX_LIST_SIZE), ownerQuery(user.id()))
            .getRecords()
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Transactional
    public ProjectDTO create(CurrentUser user, CreateProjectRequest request) {
        OffsetDateTime now = OffsetDateTime.now();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUserId(user.id());
        project.setName(request.name());
        project.setDescription(defaultString(request.description()));
        project.setTopologyData(JsonNodeTypeHandler.emptyObject());
        project.setRuleData(defaultRuleData());
        project.setVersion(1);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setIsDeleted(false);
        projectMapper.insert(project);
        return toDto(project);
    }

    public ProjectDTO get(CurrentUser user, UUID id) {
        return toDto(requireProject(user, id));
    }

    @Transactional
    public ProjectDTO update(CurrentUser user, UUID id, UpdateProjectRequest request) {
        Project project = requireProject(user, id);
        project.setName(request.name());
        project.setDescription(defaultString(request.description()));
        project.setUpdatedAt(OffsetDateTime.now());
        projectMapper.updateById(project);
        return toDto(project);
    }

    @Transactional
    public void delete(CurrentUser user, UUID id) {
        Project project = requireProject(user, id);
        project.setIsDeleted(true);
        project.setUpdatedAt(OffsetDateTime.now());
        projectMapper.updateById(project);
    }

    @Transactional
    public ProjectDTO saveSnapshot(CurrentUser user, UUID id, SaveSnapshotRequest request) {
        validateSnapshotSize(request);
        Project project = requireProject(user, id);
        OffsetDateTime now = OffsetDateTime.now();
        project.setTopologyData(request.topologyData());
        project.setRuleData(request.ruleData());
        project.setDescription(defaultString(request.description()));
        project.setUpdatedAt(now);
        projectMapper.updateById(project);
        Project updatedProject = requireProject(user, id);

        ProjectSnapshot snapshot = new ProjectSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setProjectId(updatedProject.getId());
        snapshot.setVersion(updatedProject.getVersion());
        snapshot.setSnapshotData(snapshotData(request, now));
        snapshot.setDescription(defaultString(request.description()));
        snapshot.setCreatedAt(now);
        snapshot.setUpdatedAt(now);
        snapshot.setIsDeleted(false);
        snapshotMapper.insert(snapshot);
        return toDto(updatedProject);
    }

    public Project requireProject(CurrentUser user, UUID id) {
        Project project = projectMapper.selectOne(ownerQuery(user.id()).eq(Project::getId, id));
        if (project == null) {
            throw new ApiException(ErrorCode.NOT_FOUND, "工程不存在");
        }
        return project;
    }

    private LambdaQueryWrapper<Project> ownerQuery(Long userId) {
        return new LambdaQueryWrapper<Project>()
            .eq(Project::getUserId, userId)
            .eq(Project::getIsDeleted, false)
            .orderByDesc(Project::getUpdatedAt);
    }

    private ProjectDTO toDto(Project project) {
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

    private JsonNode defaultRuleData() {
        ObjectNode ruleData = objectMapper.createObjectNode();
        ruleData.putArray("ruleSets");
        ObjectNode odConfig = ruleData.putObject("odConfig");
        odConfig.putArray("pairs");
        return ruleData;
    }

    private JsonNode snapshotData(SaveSnapshotRequest request, OffsetDateTime savedAt) {
        ObjectNode snapshot = objectMapper.createObjectNode();
        snapshot.set("topologyData", request.topologyData());
        snapshot.set("ruleData", request.ruleData());
        snapshot.put("description", defaultString(request.description()));
        snapshot.put("savedAt", savedAt.toString());
        return snapshot;
    }

    private void validateSnapshotSize(SaveSnapshotRequest request) {
        int topologyChars = jsonChars(request.topologyData());
        int ruleChars = jsonChars(request.ruleData());
        if (topologyChars + ruleChars > MAX_SNAPSHOT_JSON_CHARS) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "工程快照数据过大");
        }
    }

    private int jsonChars(JsonNode node) {
        return node == null ? 0 : node.toString().length();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
