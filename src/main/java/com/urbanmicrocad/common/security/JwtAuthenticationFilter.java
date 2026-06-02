package com.urbanmicrocad.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;
    private final TokenBlacklist tokenBlacklist;

    public JwtAuthenticationFilter(JwtService jwtService, CurrentUserService currentUserService,
                                   TokenBlacklist tokenBlacklist) {
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            CurrentUser tokenUser = jwtService.parseToken(header.substring(7));
            // Check if token has been revoked
            if (tokenUser.jti() != null && tokenBlacklist.isBlacklisted(tokenUser.jti())) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            CurrentUser user = currentUserService.loadActiveUser(tokenUser.id());
            if (user != null) {
                // Preserve jti and expiresAt from token for logout revocation
                CurrentUser authenticatedUser = new CurrentUser(
                    user.id(), user.username(), user.role(),
                    tokenUser.jti(), tokenUser.expiresAt()
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (RuntimeException ignored) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}
