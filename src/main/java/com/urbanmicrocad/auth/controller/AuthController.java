package com.urbanmicrocad.auth.controller;

import com.urbanmicrocad.auth.dto.LoginRequest;
import com.urbanmicrocad.auth.dto.LoginResponse;
import com.urbanmicrocad.auth.dto.RegisterRequest;
import com.urbanmicrocad.auth.service.IAuthService;
import com.urbanmicrocad.common.response.ApiResponse;
import com.urbanmicrocad.common.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me(@AuthenticationPrincipal CurrentUser user, HttpServletRequest request) {
        return ApiResponse.ok(authService.me(user, request.getHeader("Authorization")));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CurrentUser user) {
        authService.logout(user);
        return ApiResponse.ok();
    }
}
