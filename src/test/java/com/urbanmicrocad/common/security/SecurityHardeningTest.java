package com.urbanmicrocad.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DisplayName("Security hardening integration tests")
class SecurityHardeningTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("login rate limit returns 429 after exceeding threshold")
    void loginRateLimitReturns429() throws Exception {
        String body = """
            {"username":"ratelimituser","password":"Password123"}
            """;
        // Use a unique IP to isolate from other tests
        String uniqueIp = "10.0.0.100";

        // Exhaust the rate limit (5 requests per 10 seconds in test config)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .header("X-Forwarded-For", uniqueIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
        }

        // The 6th request should be rate-limited
        mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", uniqueIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(429));
    }

    @Test
    @DisplayName("register rate limit returns 429 after exceeding threshold")
    void registerRateLimitReturns429() throws Exception {
        String body = """
            {"username":"ratelimitreg","password":"Password123"}
            """;
        // Use a unique IP to isolate from other tests
        String uniqueIp = "10.0.0.101";

        // Exhaust the rate limit (3 requests per 10 seconds in test config)
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/register")
                    .header("X-Forwarded-For", uniqueIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body));
        }

        // The 4th request should be rate-limited
        mockMvc.perform(post("/api/auth/register")
                .header("X-Forwarded-For", uniqueIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(429));
    }

    @Test
    @DisplayName("logout blacklists the current token")
    void logoutBlacklistsToken() throws Exception {
        // Use a unique IP to avoid rate limit interference
        String uniqueIp = "10.0.0.102";
        // Register and login
        String registerBody = """
            {"username":"logouttest","password":"Password123"}
            """;
        mockMvc.perform(post("/api/auth/register")
                .header("X-Forwarded-For", uniqueIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                .header("X-Forwarded-For", uniqueIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
            .andExpect(status().isOk())
            .andReturn();

        // Extract token from login response
        String responseJson = loginResult.getResponse().getContentAsString();
        String token = extractTokenFromJson(responseJson);
        assertThat(token).isNotBlank();

        // Verify /me works before logout
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // Logout
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());

        // Verify /me fails after logout (token is blacklisted)
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("swagger endpoints accessible in default profile")
    void swaggerAccessibleInDefaultProfile() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(result -> {
                // Should not be 403 (denied) — may be 302 redirect or 200
                assertThat(result.getResponse().getStatus()).isNotEqualTo(403);
            });
    }

    private String extractTokenFromJson(String json) {
        // Simple extraction: find "token":"..." pattern
        int start = json.indexOf("\"token\":\"") + 9;
        int end = json.indexOf("\"", start);
        return start > 8 && end > start ? json.substring(start, end) : "";
    }
}
