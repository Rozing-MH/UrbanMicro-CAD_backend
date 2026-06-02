package com.urbanmicrocad.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.project.entity.Project;
import com.urbanmicrocad.project.entity.ProjectSnapshot;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Project Mapper 集成测试 (H2)")
class ProjectIntegrationTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectSnapshotMapper snapshotMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("插入并查询工程")
    void insertAndFind() {
        Project project = createProject(1L, "集成测试工程");

        Project found = projectMapper.selectById(project.getId());
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("集成测试工程");
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("JSONB 字段读写正确")
    void jsonFieldsReadWrite() {
        Project project = createProject(1L, "JSON测试");
        ObjectNode topology = objectMapper.createObjectNode();
        topology.put("nodeCount", 42);
        topology.putArray("nodes").add("node-1").add("node-2");
        project.setTopologyData(topology);
        projectMapper.updateById(project);

        Project found = projectMapper.selectById(project.getId());
        assertThat(found.getTopologyData()).isNotNull();
        assertThat(found.getTopologyData().get("nodeCount").asInt()).isEqualTo(42);
        assertThat(found.getTopologyData().get("nodes").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("软删除过滤 — 已删除工程不出现在查询中")
    void softDelete_notInQuery() {
        Project active = createProject(1L, "活跃工程");
        Project deleted = createProject(1L, "已删除工程");
        deleted.setIsDeleted(true);
        projectMapper.updateById(deleted);

        List<Project> results = projectMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, 1L)
                .eq(Project::getIsDeleted, false)
        );

        assertThat(results).anyMatch(p -> p.getId().equals(active.getId()));
        assertThat(results).noneMatch(p -> p.getId().equals(deleted.getId()));
    }

    @Test
    @DisplayName("用户隔离 — 不同用户看不到对方工程")
    void userIsolation() {
        createProject(1L, "用户1工程");
        createProject(2L, "用户2工程");

        List<Project> user1Projects = projectMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Project>()
                .eq(Project::getUserId, 1L)
                .eq(Project::getIsDeleted, false)
        );

        assertThat(user1Projects).allMatch(p -> p.getUserId().equals(1L));
        assertThat(user1Projects).noneMatch(p -> p.getUserId().equals(2L));
    }

    @Test
    @DisplayName("快照插入与查询")
    void snapshotInsertAndQuery() {
        Project project = createProject(1L, "快照测试工程");

        ProjectSnapshot snapshot = new ProjectSnapshot();
        snapshot.setId(UUID.randomUUID());
        snapshot.setProjectId(project.getId());
        snapshot.setVersion(1);
        ObjectNode snapshotData = objectMapper.createObjectNode();
        snapshotData.put("description", "v1快照");
        snapshot.setSnapshotData(snapshotData);
        snapshot.setDescription("v1快照");
        snapshot.setCreatedAt(OffsetDateTime.now());
        snapshot.setUpdatedAt(OffsetDateTime.now());
        snapshot.setIsDeleted(false);
        snapshotMapper.insert(snapshot);

        ProjectSnapshot found = snapshotMapper.selectById(snapshot.getId());
        assertThat(found).isNotNull();
        assertThat(found.getProjectId()).isEqualTo(project.getId());
        assertThat(found.getSnapshotData()).isNotNull();
        assertThat(found.getSnapshotData().get("description").asText()).isEqualTo("v1快照");
    }

    @Test
    @DisplayName("更新工程名称")
    void updateProject() {
        Project project = createProject(1L, "旧名称");
        project.setName("新名称");
        projectMapper.updateById(project);

        Project found = projectMapper.selectById(project.getId());
        assertThat(found.getName()).isEqualTo("新名称");
    }

    private Project createProject(Long userId, String name) {
        OffsetDateTime now = OffsetDateTime.now();
        Project project = new Project();
        project.setId(UUID.randomUUID());
        project.setUserId(userId);
        project.setName(name);
        project.setDescription("");
        project.setTopologyData(objectMapper.createObjectNode());
        ObjectNode ruleData = objectMapper.createObjectNode();
        ruleData.putArray("ruleSets");
        project.setRuleData(ruleData);
        project.setVersion(1);
        project.setCreatedAt(now);
        project.setUpdatedAt(now);
        project.setIsDeleted(false);
        projectMapper.insert(project);
        return project;
    }
}
