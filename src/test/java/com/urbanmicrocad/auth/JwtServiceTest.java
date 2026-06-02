package com.urbanmicrocad.auth;

import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.common.security.JwtProperties;
import com.urbanmicrocad.common.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    @Test
    void generatesAndParsesCurrentUser() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-test-secret-test-secret-test-secret");
        properties.setExpiresMinutes(60);
        JwtService service = new JwtService(properties);

        String token = service.generateToken(new CurrentUser(1L, "demo", "USER"));
        CurrentUser parsed = service.parseToken(token);

        assertThat(parsed.id()).isEqualTo(1L);
        assertThat(parsed.username()).isEqualTo("demo");
        assertThat(parsed.role()).isEqualTo("USER");
        assertThat(parsed.jti()).isNotNull();
        assertThat(parsed.expiresAt()).isNotNull();
    }
}
