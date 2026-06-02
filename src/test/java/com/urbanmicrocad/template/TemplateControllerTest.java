package com.urbanmicrocad.template;

import com.urbanmicrocad.auth.mapper.SysUserMapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUserService;
import com.urbanmicrocad.common.security.JwtAuthenticationFilter;
import com.urbanmicrocad.common.security.JwtService;
import com.urbanmicrocad.common.security.RateLimitFilter;
import com.urbanmicrocad.common.security.TokenBlacklist;
import com.urbanmicrocad.common.test.MockAuth;
import com.urbanmicrocad.common.test.TestSecurityConfig;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import com.urbanmicrocad.report.mapper.EvaluationReportMapper;
import com.urbanmicrocad.template.controller.TemplateController;
import com.urbanmicrocad.template.dto.TemplateDTO;
import com.urbanmicrocad.template.mapper.TemplateMapper;
import com.urbanmicrocad.template.service.TemplateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = TemplateController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
    }
)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TemplateController API 测试")
class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TemplateService templateService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private TokenBlacklist tokenBlacklist;

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

    private final UUID templateId = UUID.randomUUID();

    private TemplateDTO templateDTO() {
        return new TemplateDTO(
            templateId, "基础十字路口", "BASIC_INTERSECTION",
            null, "", null
        );
    }

    @Test
    @DisplayName("GET /api/templates — 模板列表")
    void list_returnsTemplates() throws Exception {
        when(templateService.list(isNull(), any())).thenReturn(List.of(templateDTO()));

        mockMvc.perform(get("/api/templates").with(MockAuth.withDefaultUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data[0].name").value("基础十字路口"));
    }

    @Test
    @DisplayName("GET /api/templates?category=BASIC_INTERSECTION — 按分类筛选")
    void list_withCategory_returnsFiltered() throws Exception {
        when(templateService.list(eq("BASIC_INTERSECTION"), any())).thenReturn(List.of(templateDTO()));

        mockMvc.perform(get("/api/templates")
                .param("category", "BASIC_INTERSECTION")
                .with(MockAuth.withDefaultUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].category").value("BASIC_INTERSECTION"));
    }

    @Test
    @DisplayName("GET /api/templates/{id} — 模板详情成功")
    void get_success() throws Exception {
        when(templateService.get(eq(templateId), any())).thenReturn(templateDTO());

        mockMvc.perform(get("/api/templates/{id}", templateId).with(MockAuth.withDefaultUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(templateId.toString()));
    }

    @Test
    @DisplayName("GET /api/templates/{id} — 模板不存在返回404")
    void get_notFound_returns404() throws Exception {
        when(templateService.get(eq(templateId), any()))
            .thenThrow(new ApiException(ErrorCode.NOT_FOUND, "模板不存在"));

        mockMvc.perform(get("/api/templates/{id}", templateId).with(MockAuth.withDefaultUser()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("GET /api/templates/cross-sections — 断面模板列表")
    void listCrossSections_returnsProfiles() throws Exception {
        when(templateService.listCrossSections()).thenReturn(List.of());

        mockMvc.perform(get("/api/templates/cross-sections").with(MockAuth.withDefaultUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("未认证请求 — 无认证主体时控制器正常响应（安全拦截由 SecurityFilterChain 保证）")
    void unauthenticated_noPrincipal_returnsOk() throws Exception {
        mockMvc.perform(get("/api/templates"))
            .andExpect(status().isOk());
    }
}
