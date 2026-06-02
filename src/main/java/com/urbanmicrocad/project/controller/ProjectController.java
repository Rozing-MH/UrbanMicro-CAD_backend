package com.urbanmicrocad.project.controller;

import com.urbanmicrocad.common.response.ApiResponse;
import com.urbanmicrocad.common.response.PageResponse;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.dto.CreateProjectRequest;
import com.urbanmicrocad.project.dto.ProjectDTO;
import com.urbanmicrocad.project.dto.ProjectSnapshotDTO;
import com.urbanmicrocad.project.dto.ProjectSummaryDTO;
import com.urbanmicrocad.project.dto.SaveSnapshotRequest;
import com.urbanmicrocad.project.dto.UpdateProjectRequest;
import com.urbanmicrocad.project.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ApiResponse<PageResponse<ProjectSummaryDTO>> list(
        @AuthenticationPrincipal CurrentUser user,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(projectService.list(user, page, size));
    }

    @PostMapping
    public ApiResponse<ProjectDTO> create(
        @AuthenticationPrincipal CurrentUser user,
        @Valid @RequestBody CreateProjectRequest request
    ) {
        return ApiResponse.ok(projectService.create(user, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProjectDTO> get(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
        return ApiResponse.ok(projectService.get(user, id));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProjectDTO> update(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateProjectRequest request
    ) {
        return ApiResponse.ok(projectService.update(user, id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal CurrentUser user, @PathVariable UUID id) {
        projectService.delete(user, id);
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/snapshot")
    public ApiResponse<ProjectDTO> saveSnapshot(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id,
        @Valid @RequestBody SaveSnapshotRequest request
    ) {
        return ApiResponse.ok(projectService.saveSnapshot(user, id, request));
    }

    @GetMapping("/{id}/snapshots")
    public ApiResponse<PageResponse<ProjectSnapshotDTO>> listSnapshots(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(projectService.listSnapshots(user, id, page, size));
    }

    @GetMapping("/{id}/snapshots/{version}")
    public ApiResponse<ProjectDTO> getSnapshotByVersion(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id,
        @PathVariable int version
    ) {
        return ApiResponse.ok(projectService.getSnapshotByVersion(user, id, version));
    }

    @PostMapping("/{id}/snapshots/{snapshotId}/restore")
    public ApiResponse<ProjectDTO> restoreSnapshot(
        @AuthenticationPrincipal CurrentUser user,
        @PathVariable UUID id,
        @PathVariable UUID snapshotId
    ) {
        return ApiResponse.ok(projectService.restoreSnapshot(user, id, snapshotId));
    }
}
