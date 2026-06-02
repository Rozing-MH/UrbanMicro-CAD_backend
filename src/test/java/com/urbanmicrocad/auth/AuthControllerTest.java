package com.urbanmicrocad.auth;

import com.urbanmicrocad.auth.dto.LoginResponse;
import com.urbanmicrocad.auth.controller.AuthController;
import com.urbanmicrocad.auth.mapper.SysUserMapper;
import com.urbanmicrocad.auth.service.AuthService;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class,
        com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration.class
    }
)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController API 测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

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

    @Test
    @DisplayName("POST /api/auth/register — 注册成功")
    void register_success() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"pass123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("POST /api/auth/register — 空用户名校验失败")
    void register_blankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"pass123456\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/register — 密码过短校验失败")
    void register_shortPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"newuser\",\"password\":\"12\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/login — 登录成功")
    void login_success() throws Exception {
        LoginResponse response = new LoginResponse("jwt-token", "1", "demo", "USER");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"demo\",\"password\":\"pass123456\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
            .andExpect(jsonPath("$.data.userId").value("1"))
            .andExpect(jsonPath("$.data.username").value("demo"))
            .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/login — 错误凭据返回401")
    void login_invalidCredentials_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"demo\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login — 空用户名返回400")
    void login_blankUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"\",\"password\":\"pass123456\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("GET /api/auth/me — 已认证用户获取信息")
    void me_authenticated_returnsUserInfo() throws Exception {
        CurrentUser user = new CurrentUser(1L, "demo", "USER");
        LoginResponse response = new LoginResponse("jwt-token", "1", "demo", "USER");
        when(authService.me(any(CurrentUser.class), eq("Bearer jwt-token"))).thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer jwt-token")
                .with(MockAuth.withUser(user)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.username").value("demo"));
    }

    @Test
    @DisplayName("POST /api/auth/logout — 登出成功")
    void logout_success() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .with(MockAuth.withDefaultUser()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200));
    }
}
