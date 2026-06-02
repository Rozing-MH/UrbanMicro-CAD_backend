package com.urbanmicrocad.common.test;

import com.urbanmicrocad.common.security.CurrentUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

/**
 * MockMvc test helper that sets up SecurityContext with a {@link CurrentUser} principal,
 * matching the authentication model used by JwtAuthenticationFilter.
 */
public final class MockAuth {

    private MockAuth() {}

    public static RequestPostProcessor withUser(CurrentUser user) {
        var authentication = new UsernamePasswordAuthenticationToken(
            user,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
        );
        return request -> {
            // Set SecurityContext directly for @AuthenticationPrincipal resolution
            // (needed when security filters are disabled via addFilters=false)
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            // Also apply via Spring Security test support for filter-based resolution
            SecurityMockMvcRequestPostProcessors.authentication(authentication).postProcessRequest(request);
            return request;
        };
    }

    public static RequestPostProcessor withDefaultUser() {
        return withUser(new CurrentUser(1L, "demo", "USER"));
    }
}
