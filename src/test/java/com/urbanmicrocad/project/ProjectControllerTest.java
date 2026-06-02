package com.urbanmicrocad.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanmicrocad.auth.mapper.SysUserMapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUserService;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.common.security.JwtAuthenticationFilter;
import com.urbanmicrocad.common.security.JwtService;
import com.urbanmicrocad.common.test.MockAuth;
import com.urbanmicrocad.common.test.TestSecurityConfig;
import com.urbanmicrocad.project.controller.ProjectController;
import com.urbanmicrocad.project.dto.CreateProjectRequest;
import com.urbanmicrocad.project.dto.ProjectDTO;
import com.urbanmicrocad.project.dto.ProjectSnapshotDTO;
import com.urbanmicrocad.project.dto.SaveSnapshotRequest;
import com.urbanmicrocad.project.dto.UpdateProjectRequest;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import com.urbanmicrocad.project.service.ProjectService;
import com.urbanmicrocad.report.mapper.EvaluationReportMapper;
import com.urbanmicrocad.template.mapper.TemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ProjectController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
    }
)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProjectController API 测试")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Mock mappers to prevent @MapperScan from requiring SqlSessionFactory
    @MockBean
    private SysUserMapper sysUserMapper;
    @MockBean
    private ProjectMapper projectMapper;
    @MockBean
    private ProjectSnapshotMapper projectSnapshotMapper;
    @MockBean
    private TemplateMapper templateMapper;
    @MockBean
    private EvaluationReportMapper evaluationReportMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CurrentUser user = new CurrentUser(1L, "demo", "USER");
    private final UUID projectId = UUID.randomUUID();

    private ProjectDTO projectDTO() {
        return new ProjectDTO(
            projectId, "Test Project", "desc",
            objectMapper.createObjectNode(),
            objectMapper.createObjectNode(),
            1, OffsetDateTime.now(), OffsetDateTime.now(),
            "1", null
        );
    }

    @Test
    @DisplayName("GET /api/projects — 工程列表")
    void list_returnsProjects() throws Exception {
        when(projectService.list(user)).thenReturn(List.of(projectDTO()));

        mockMvc.perform(get("/api/projects").with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].name").value("Test Project"));
    }

    @Test
    @DisplayName("POST /api/projects — 创建工程成功")
    void create_success() throws Exception {
        when(projectService.create(any(CurrentUser.class), any(CreateProjectRequest.class)))
            .thenReturn(projectDTO());

        mockMvc.perform(post("/api/projects")
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Project\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.name").value("Test Project"));
    }

    @Test
    @DisplayName("POST /api/projects — 空工程名校验失败")
    void create_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/projects")
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("GET /api/projects/{id} — 获取工程详情")
    void get_success() throws Exception {
        when(projectService.get(user, projectId)).thenReturn(projectDTO());

        mockMvc.perform(get("/api/projects/{id}", projectId).with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(projectId.toString()));
    }

    @Test
    @DisplayName("GET /api/projects/{id} — 工程不存在返回404")
    void get_notFound_returns404() throws Exception {
        when(projectService.get(user, projectId))
            .thenThrow(new ApiException(ErrorCode.NOT_FOUND, "工程不存在"));

        mockMvc.perform(get("/api/projects/{id}", projectId).with(MockAuth.withUser(user)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("PUT /api/projects/{id} — 更新工程成功")
    void update_success() throws Exception {
        when(projectService.update(any(CurrentUser.class), eq(projectId), any(UpdateProjectRequest.class)))
            .thenReturn(projectDTO());

        mockMvc.perform(put("/api/projects/{id}", projectId)
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Updated\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} — 删除工程成功")
    void delete_success() throws Exception {
        mockMvc.perform(delete("/api/projects/{id}", projectId).with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/projects/{id}/snapshot — 保存快照成功")
    void saveSnapshot_success() throws Exception {
        when(projectService.saveSnapshot(any(CurrentUser.class), eq(projectId), any(SaveSnapshotRequest.class)))
            .thenReturn(projectDTO());

        mockMvc.perform(put("/api/projects/{id}/snapshot", projectId)
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"topologyData\":{},\"ruleData\":{}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/projects/{id}/snapshot — topologyData为null校验失败")
    void saveSnapshot_nullTopology_returns400() throws Exception {
        mockMvc.perform(put("/api/projects/{id}/snapshot", projectId)
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ruleData\":{}}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("GET /api/projects/{id}/snapshots — 快照列表")
    void listSnapshots_success() throws Exception {
        ProjectSnapshotDTO snapshotDTO = new ProjectSnapshotDTO(
            UUID.randomUUID(), projectId, 1, "v1",
            OffsetDateTime.now(), OffsetDateTime.now()
        );
        when(projectService.listSnapshots(user, projectId)).thenReturn(List.of(snapshotDTO));

        mockMvc.perform(get("/api/projects/{id}/snapshots", projectId).with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].version").value(1));
    }

    @Test
    @DisplayName("POST /api/projects/{id}/snapshots/{snapshotId}/restore — 回滚成功")
    void restoreSnapshot_success() throws Exception {
        UUID snapshotId = UUID.randomUUID();
        when(projectService.restoreSnapshot(user, projectId, snapshotId)).thenReturn(projectDTO());

        mockMvc.perform(post("/api/projects/{id}/snapshots/{snapshotId}/restore", projectId, snapshotId)
                .with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证请求 — 无认证主体时控制器正常响应（安全拦截由 SecurityFilterChain 保证）")
    void unauthenticated_noPrincipal_returnsOk() throws Exception {
        mockMvc.perform(get("/api/projects"))
            .andExpect(status().isOk());
    }
}
