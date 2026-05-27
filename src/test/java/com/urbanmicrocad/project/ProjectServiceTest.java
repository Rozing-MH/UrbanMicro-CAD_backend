package com.urbanmicrocad.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.project.dto.SaveSnapshotRequest;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import com.urbanmicrocad.project.service.ProjectService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class ProjectServiceTest {

    @Test
    void rejectsOversizedSnapshotBeforeDatabaseAccess() {
        ObjectMapper objectMapper = new ObjectMapper();
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
}
