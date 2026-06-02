package com.urbanmicrocad.report;

import com.urbanmicrocad.auth.mapper.SysUserMapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.response.PageResponse;
import com.urbanmicrocad.common.security.CurrentUserService;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.common.security.JwtAuthenticationFilter;
import com.urbanmicrocad.common.security.JwtService;
import com.urbanmicrocad.common.security.RateLimitFilter;
import com.urbanmicrocad.common.security.TokenBlacklist;
import com.urbanmicrocad.common.test.MockAuth;
import com.urbanmicrocad.common.test.TestSecurityConfig;
import com.urbanmicrocad.project.mapper.ProjectMapper;
import com.urbanmicrocad.project.mapper.ProjectSnapshotMapper;
import com.urbanmicrocad.report.controller.ReportController;
import com.urbanmicrocad.report.dto.ExportReportRequest;
import com.urbanmicrocad.report.dto.ReportDetailDTO;
import com.urbanmicrocad.report.dto.ReportSummary;
import com.urbanmicrocad.report.mapper.EvaluationReportMapper;
import com.urbanmicrocad.report.service.ReportService;
import com.urbanmicrocad.template.mapper.TemplateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = ReportController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
    }
)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ReportController API 测试")
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

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

    private final CurrentUser user = new CurrentUser(1L, "demo", "USER");
    private final UUID projectId = UUID.randomUUID();
    private final UUID reportId = UUID.randomUUID();

    private ReportSummary reportSummary() {
        return new ReportSummary(reportId, projectId, OffsetDateTime.now(), "C", 0.0);
    }

    @Test
    @DisplayName("POST /api/reports/generate — 生成报表成功")
    void generate_success() throws Exception {
        when(reportService.generate(any(CurrentUser.class), any(ExportReportRequest.class)))
            .thenReturn(reportSummary());

        mockMvc.perform(post("/api/reports/generate")
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":\"" + projectId + "\",\"format\":\"CSV\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(reportId.toString()));
    }

    @Test
    @DisplayName("GET /api/reports?projectId=... — 报表列表（分页）")
    void list_success() throws Exception {
        PageResponse<ReportSummary> page = new PageResponse<>(List.of(reportSummary()), 1, 1, 20);
        when(reportService.list(any(CurrentUser.class), eq(projectId), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/reports")
                .param("projectId", projectId.toString())
                .with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.records[0].id").value(reportId.toString()));
    }

    @Test
    @DisplayName("GET /api/reports?projectId=... — 工程不存在返回404")
    void list_projectNotFound_returns404() throws Exception {
        when(reportService.list(any(CurrentUser.class), eq(projectId), anyInt(), anyInt()))
            .thenThrow(new ApiException(ErrorCode.NOT_FOUND, "工程不存在"));

        mockMvc.perform(get("/api/reports")
                .param("projectId", projectId.toString())
                .with(MockAuth.withUser(user)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("POST /api/reports/export — CSV导出成功")
    void export_success() throws Exception {
        byte[] csv = "reportId,projectId\n".getBytes(StandardCharsets.UTF_8);
        ResponseEntity<byte[]> response = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.csv\"")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv);
        when(reportService.export(any(CurrentUser.class), any(ExportReportRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/reports/export")
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":\"" + projectId + "\",\"format\":\"CSV\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    @DisplayName("GET /api/reports/{id}/download — 下载报表成功")
    void download_success() throws Exception {
        byte[] csv = "reportId,projectId\n".getBytes(StandardCharsets.UTF_8);
        ResponseEntity<byte[]> response = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.csv\"")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv);
        when(reportService.download(user, reportId)).thenReturn(response);

        mockMvc.perform(get("/api/reports/{id}/download", reportId)
                .with(MockAuth.withUser(user)))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/reports/{id}/download — 报表不存在返回404")
    void download_notFound_returns404() throws Exception {
        when(reportService.download(user, reportId))
            .thenThrow(new ApiException(ErrorCode.NOT_FOUND, "报表不存在"));

        mockMvc.perform(get("/api/reports/{id}/download", reportId)
                .with(MockAuth.withUser(user)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("POST /api/reports/export — PDF导出成功")
    void export_pdf_success() throws Exception {
        byte[] pdf = "%PDF-1.4 test content".getBytes(StandardCharsets.UTF_8);
        ResponseEntity<byte[]> response = ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
        when(reportService.export(any(CurrentUser.class), any(ExportReportRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/reports/export")
                .with(MockAuth.withUser(user))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectId\":\"" + projectId + "\",\"format\":\"PDF\"}"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString("application/pdf")));
    }

    @Test
    @DisplayName("GET /api/reports/{id} — 报表详情")
    void detail_success() throws Exception {
        ReportDetailDTO detail = new ReportDetailDTO(
            reportId, projectId, OffsetDateTime.now(), "C", 0.0, null, null, null, null
        );
        when(reportService.detail(user, reportId)).thenReturn(detail);

        mockMvc.perform(get("/api/reports/{id}", reportId)
                .with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.id").value(reportId.toString()));
    }

    @Test
    @DisplayName("GET /api/reports/{id} — 报表不存在返回404")
    void detail_notFound_returns404() throws Exception {
        when(reportService.detail(user, reportId))
            .thenThrow(new ApiException(ErrorCode.NOT_FOUND, "报表不存在"));

        mockMvc.perform(get("/api/reports/{id}", reportId)
                .with(MockAuth.withUser(user)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("未认证请求 — 无认证主体时控制器正常响应（安全拦截由 SecurityFilterChain 保证）")
    void unauthenticated_noPrincipal_returnsOk() throws Exception {
        mockMvc.perform(get("/api/reports")
                .param("projectId", projectId.toString()))
            .andExpect(status().isOk());
    }
}
