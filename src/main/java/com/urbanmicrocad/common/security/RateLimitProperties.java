package com.urbanmicrocad.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Rate-limiting configuration properties.
 * <p>
 * Binds to {@code app.rate-limit.*} in application configuration.
 */
@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private int loginMaxRequests = 10;
    private int loginWindowSeconds = 60;
    private int registerMaxRequests = 5;
    private int registerWindowSeconds = 60;

    public int getLoginMaxRequests() { return loginMaxRequests; }
    public void setLoginMaxRequests(int loginMaxRequests) { this.loginMaxRequests = loginMaxRequests; }

    public int getLoginWindowSeconds() { return loginWindowSeconds; }
    public void setLoginWindowSeconds(int loginWindowSeconds) { this.loginWindowSeconds = loginWindowSeconds; }

    public int getRegisterMaxRequests() { return registerMaxRequests; }
    public void setRegisterMaxRequests(int registerMaxRequests) { this.registerMaxRequests = registerMaxRequests; }

    public int getRegisterWindowSeconds() { return registerWindowSeconds; }
    public void setRegisterWindowSeconds(int registerWindowSeconds) { this.registerWindowSeconds = registerWindowSeconds; }
}
