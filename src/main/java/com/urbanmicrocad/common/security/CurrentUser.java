package com.urbanmicrocad.common.security;

/**
 * Authenticated user principal extracted from JWT.
 *
 * @param id        user ID
 * @param username  username
 * @param role      user role
 * @param jti       JWT unique identifier (for token revocation)
 * @param expiresAt token expiration epoch millis
 */
public record CurrentUser(Long id, String username, String role, String jti, Long expiresAt) {

    /** Convenience constructor without jti/expiry for service-layer usage. */
    public CurrentUser(Long id, String username, String role) {
        this(id, username, role, null, null);
    }
}
