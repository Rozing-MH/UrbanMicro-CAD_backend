package com.urbanmicrocad.project;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.dto.SaveSnapshotRequest;
import com.urbanmicrocad.project.entity.Project;
import com.urbanmicrocad.project.entity.ProjectSnapshot;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import com.urbanmicrocad.project.service.ProjectService;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ProjectServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void rejectsOversizedSnapshotBeforeDatabaseAccess() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        SaveSnapshotRequest request = new SaveSnapshotRequest(
            objectMapper.createObjectNode().put("payload", "x".repeat(2_000_001)),
            objectMapper.createObjectNode(),
            null
        );

        assertThatThrownBy(() -> service.saveSnapshot(new CurrentUser(1L, "demo", "USER"), UUID.randomUUID(), request))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("工程快照数据过大");
        verifyNoInteractions(projectMapper, snapshotMapper);
    }

    @Test
    void listsSnapshotsForOwnedProject() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        Project project = project(user.id());
        ProjectSnapshot snapshot = snapshot(project.getId(), 2, snapshotData("road-v2"));
        when(projectMapper.selectOne(any(Wrapper.class))).thenReturn(project);
        Page<ProjectSnapshot> snapshotPage = new Page<>();
        snapshotPage.setRecords(List.of(snapshot));
        when(snapshotMapper.selectPage(any(Page.class), any(Wrapper.class))).thenReturn(snapshotPage);

        var snapshots = service.listSnapshots(user, project.getId(), 1, 20);

        assertThat(snapshots.records()).hasSize(1);
        assertThat(snapshots.records().get(0).id()).isEqualTo(snapshot.getId());
        assertThat(snapshots.records().get(0).version()).isEqualTo(2);
        assertThat(snapshots.records().get(0).description()).isEqualTo("snapshot 2");
    }

    @Test
    void restoresOwnedSnapshotIntoProject() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        Project project = project(user.id());
        UUID snapshotId = UUID.randomUUID();
        ProjectSnapshot snapshot = snapshot(project.getId(), 3, snapshotData("restored-road"));
        snapshot.setId(snapshotId);
        // requireProjectForUpdate 返回 project
        when(projectMapper.selectOneForUpdate(project.getId(), user.id())).thenReturn(project);
        // requireProject 重新读取（获取触发器递增后的 version）
        when(projectMapper.selectOne(any(Wrapper.class))).thenReturn(project);
        when(snapshotMapper.selectOne(any(Wrapper.class))).thenReturn(snapshot);
        when(projectMapper.updateById(any(Project.class))).thenReturn(1);

        service.restoreSnapshot(user, project.getId(), snapshotId);

        verify(projectMapper).updateById(argThat((Project updated) ->
            updated.getId().equals(project.getId())
                && updated.getTopologyData().get("name").asText().equals("restored-road")
                && updated.getRuleData().has("ruleSets")
        ));
    }

    @Test
    void rejectsSnapshotRestoreWhenSnapshotDoesNotBelongToProject() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        Project project = project(user.id());
        when(projectMapper.selectOneForUpdate(project.getId(), user.id())).thenReturn(project);
        when(snapshotMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.restoreSnapshot(user, project.getId(), UUID.randomUUID()))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("快照不存在");
    }

    @Test
    void rejectsSnapshotRestoreWhenSnapshotDataIsIncomplete() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        Project project = project(user.id());
        ProjectSnapshot snapshot = snapshot(project.getId(), 4, objectMapper.createObjectNode());
        when(projectMapper.selectOneForUpdate(project.getId(), user.id())).thenReturn(project);
        when(snapshotMapper.selectOne(any(Wrapper.class))).thenReturn(snapshot);

        assertThatThrownBy(() -> service.restoreSnapshot(user, project.getId(), snapshot.getId()))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("快照数据不完整");
    }

    @Test
    void loadsSnapshotByVersionReadOnly() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        Project project = project(user.id());
        ObjectNode snapshotData = snapshotData("version-load-test");
        ProjectSnapshot snapshot = snapshot(project.getId(), 5, snapshotData);
        when(projectMapper.selectOne(any(Wrapper.class))).thenReturn(project);
        when(snapshotMapper.selectOne(any(Wrapper.class))).thenReturn(snapshot);

        var result = service.getSnapshotByVersion(user, project.getId(), 5);

        assertThat(result.topologyData()).isNotNull();
        assertThat(result.topologyData().get("name").asText()).isEqualTo("version-load-test");
        assertThat(result.ruleData()).isNotNull();
        assertThat(result.version()).isEqualTo(5);
    }

    @Test
    void rejectsSnapshotByVersionWhenVersionNotFound() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        Project project = project(user.id());
        when(projectMapper.selectOne(any(Wrapper.class))).thenReturn(project);
        when(snapshotMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        assertThatThrownBy(() -> service.getSnapshotByVersion(user, project.getId(), 99))
            .isInstanceOf(ApiException.class)
            .hasMessageContaining("快照版本不存在");
    }

    @Test
    void saveSnapshot_throwsNotFoundWhenProjectNotOwned() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        UUID projectId = UUID.randomUUID();
        // requireProjectForUpdate 返回 null → 工程不存在
        when(projectMapper.selectOneForUpdate(projectId, user.id())).thenReturn(null);

        SaveSnapshotRequest request = new SaveSnapshotRequest(
            objectMapper.createObjectNode(), objectMapper.createObjectNode(), "test");

        assertThatThrownBy(() -> service.saveSnapshot(user, projectId, request))
            .isInstanceOf(ApiException.class)
            .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND))
            .hasMessageContaining("工程不存在");
    }

    @Test
    void restoreSnapshot_throwsNotFoundWhenProjectNotOwned() {
        ProjectMapper projectMapper = mock(ProjectMapper.class);
        ProjectSnapshotMapper snapshotMapper = mock(ProjectSnapshotMapper.class);
        ProjectService service = new ProjectService(projectMapper, snapshotMapper, objectMapper);
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        UUID projectId = UUID.randomUUID();
        // requireProjectForUpdate 返回 null → 工程不存在
        when(projectMapper.selectOneForUpdate(projectId, user.id())).thenReturn(null);

        assertThatThrownBy(() -> service.restoreSnapshot(user, projectId, UUID.randomUUID()))
            .isInstanceOf(ApiException.class)
            .satisfies(ex -> assertThat(((ApiException) ex).getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND))
            .hasMessageContaining("工程不存在");
    }

    private Project project(Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUserId(userId);
        project.setName("demo project");
        project.setDescription("");
        project.setTopologyData(objectMapper.createObjectNode());
        ObjectNode ruleData = objectMapper.createObjectNode();
        ruleData.putArray("ruleSets");
        project.setRuleData(ruleData);
        project.setVersion(1);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setIsDeleted(false);
        return project;
    }

    private ProjectSnapshot snapshot(UUID projectId, int version, ObjectNode snapshotData) {
        OffsetDateTime now = OffsetDateTime.now();
        ProjectSnapshot snapshot = new ProjectSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setProjectId(projectId);
        snapshot.setVersion(version);
        snapshot.setSnapshotData(snapshotData);
        snapshot.setDescription("snapshot " + version);
        snapshot.setCreatedAt(now);
        snapshot.setUpdatedAt(now);
        snapshot.setIsDeleted(false);
        return snapshot;
    }

    private ObjectNode snapshotData(String topologyName) {
        ObjectNode data = objectMapper.createObjectNode();
        data.set("topologyData", objectMapper.createObjectNode().put("name", topologyName));
        ObjectNode ruleData = objectMapper.createObjectNode();
        ruleData.putArray("ruleSets");
        data.set("ruleData", ruleData);
        data.put("description", "restore target");
        data.put("savedAt", OffsetDateTime.now().toString());
        return data;
    }
}
