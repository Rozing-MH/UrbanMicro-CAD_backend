package com.urbanmicrocad.auth.dto;

public record LoginResponse(
    String token,
    String userId,
    String username,
    String role
) {
}
