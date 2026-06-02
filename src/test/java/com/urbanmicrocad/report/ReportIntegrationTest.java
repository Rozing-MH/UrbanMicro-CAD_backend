package com.urbanmicrocad.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.urbanmicrocad.project.entity.Project;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.report.entity.EvaluationReport;
import com.urbanmicrocad.report.mapper.EvaluationReportMapper;
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
@DisplayName("Report Mapper 集成测试 (H2)")
class ReportIntegrationTest {

    @Autowired
    private EvaluationReportMapper reportMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("插入并查询报表")
    void insertAndFind() {
        Project project = createProject(1L, "报表测试工程");
        EvaluationReport report = createReport(project.getId(), 1L);

        EvaluationReport found = reportMapper.selectById(report.getId());
        assertThat(found).isNotNull();
        assertThat(found.getProjectId()).isEqualTo(project.getId());
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getIsDeleted()).isFalse();
    }

    @Test
    @DisplayName("报表 JSON 字段读写正确")
    void jsonFieldsReadWrite() {
        Project project = createProject(1L, "JSON报表测试");
        EvaluationReport report = createReport(project.getId(), 1L);

        ObjectNode metrics = objectMapper.createObjectNode();
        metrics.put("avgSpeed", 35.5);
        metrics.put("volume", 1200);
        report.setLaneMetrics(metrics);
        reportMapper.updateById(report);

        EvaluationReport found = reportMapper.selectById(report.getId());
        assertThat(found.getLaneMetrics()).isNotNull();
        assertThat(found.getLaneMetrics().get("avgSpeed").asDouble()).isCloseTo(35.5, org.assertj.core.data.Offset.offset(0.01));
        assertThat(found.getLaneMetrics().get("volume").asInt()).isEqualTo(1200);
    }

    @Test
    @DisplayName("按工程和用户查询报表列表")
    void listByProjectAndUser() {
        Project project = createProject(1L, "列表测试工程");
        createReport(project.getId(), 1L);
        createReport(project.getId(), 1L);

        Project otherProject = createProject(1L, "其他工程");
        createReport(otherProject.getId(), 1L);

        List<EvaluationReport> results = reportMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationReport>()
                .eq(EvaluationReport::getProjectId, project.getId())
                .eq(EvaluationReport::getUserId, 1L)
                .eq(EvaluationReport::getIsDeleted, false)
        );

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getProjectId().equals(project.getId()));
    }

    @Test
    @DisplayName("软删除过滤 — 已删除报表不出现在查询中")
    void softDelete_notInQuery() {
        Project project = createProject(1L, "软删除测试");
        EvaluationReport active = createReport(project.getId(), 1L);
        EvaluationReport deleted = createReport(project.getId(), 1L);
        deleted.setIsDeleted(true);
        reportMapper.updateById(deleted);

        List<EvaluationReport> results = reportMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationReport>()
                .eq(EvaluationReport::getProjectId, project.getId())
                .eq(EvaluationReport::getUserId, 1L)
                .eq(EvaluationReport::getIsDeleted, false)
        );

        assertThat(results).anyMatch(r -> r.getId().equals(active.getId()));
        assertThat(results).noneMatch(r -> r.getId().equals(deleted.getId()));
    }

    @Test
    @DisplayName("用户隔离 — 不同用户看不到对方报表")
    void userIsolation() {
        Project project = createProject(1L, "隔离测试");
        createReport(project.getId(), 1L);
        createReport(project.getId(), 2L);

        List<EvaluationReport> user1Reports = reportMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EvaluationReport>()
                .eq(EvaluationReport::getProjectId, project.getId())
                .eq(EvaluationReport::getUserId, 1L)
                .eq(EvaluationReport::getIsDeleted, false)
        );

        assertThat(user1Reports).allMatch(r -> r.getUserId().equals(1L));
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

    private EvaluationReport createReport(UUID projectId, Long userId) {
        OffsetDateTime now = OffsetDateTime.now();
        EvaluationReport report = new EvaluationReport();
        report.setId(UUID.randomUUID());
        report.setProjectId(projectId);
        report.setUserId(userId);
        report.setLaneMetrics(objectMapper.createObjectNode());
        report.setIntersectionLos(objectMapper.createObjectNode());
        report.setHeatmapConfig(objectMapper.createObjectNode());
        report.setFlightLineData(objectMapper.createObjectNode());
        report.setGeneratedAt(now);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);
        report.setIsDeleted(false);
        reportMapper.insert(report);
        return report;
    }
}
