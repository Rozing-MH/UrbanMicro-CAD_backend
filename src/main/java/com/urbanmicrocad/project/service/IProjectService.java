package com.urbanmicrocad.project.service;

import com.urbanmicrocad.common.response.PageResponse;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.dto.CreateProjectRequest;
import com.urbanmicrocad.project.dto.ProjectDTO;
import com.urbanmicrocad.project.dto.ProjectSnapshotDTO;
import com.urbanmicrocad.project.dto.ProjectSummaryDTO;
import com.urbanmicrocad.project.dto.SaveSnapshotRequest;
import com.urbanmicrocad.project.dto.UpdateProjectRequest;
import com.urbanmicrocad.project.entity.Project;

import java.util.UUID;

/**
 * 工程管理服务接口。
 * 对齐设计文档 3.2 后端包图 — Service 接口化。
 */
public interface IProjectService {

    PageResponse<ProjectSummaryDTO> list(CurrentUser user, int page, int size);

    ProjectDTO create(CurrentUser user, CreateProjectRequest request);

    ProjectDTO get(CurrentUser user, UUID id);

    ProjectDTO update(CurrentUser user, UUID id, UpdateProjectRequest request);

    void delete(CurrentUser user, UUID id);

    ProjectDTO saveSnapshot(CurrentUser user, UUID id, SaveSnapshotRequest request);

    PageResponse<ProjectSnapshotDTO> listSnapshots(CurrentUser user, UUID projectId, int page, int size);

    ProjectDTO restoreSnapshot(CurrentUser user, UUID projectId, UUID snapshotId);

    ProjectDTO getSnapshotByVersion(CurrentUser user, UUID projectId, int version);

    /**
     * 查询指定工程（归属校验 + 软删除过滤）。
     * 供其他 Service 跨模块校验工程归属。
     *
     * @throws com.urbanmicrocad.common.exception.ApiException 工程不存在时抛 NOT_FOUND
     */
    Project requireProject(CurrentUser user, UUID id);
}
