package com.urbanmicrocad.auth.service;

import com.urbanmicrocad.auth.dto.LoginRequest;
import com.urbanmicrocad.auth.dto.LoginResponse;
import com.urbanmicrocad.auth.dto.RegisterRequest;
import com.urbanmicrocad.common.security.CurrentUser;

/**
 * 认证服务接口。
 * 对齐设计文档 3.2 后端包图 — Service 接口化。
 */
public interface IAuthService {

    /**
     * 用户注册。
     *
     * @param request 注册请求（用户名 + 密码）
     * @throws com.urbanmicrocad.common.exception.ApiException 用户名已存在时抛 CONFLICT
     */
    void register(RegisterRequest request);

    /**
     * 用户登录。
     *
     * @param request 登录请求（用户名 + 密码）
     * @return JWT token + 用户概要
     * @throws org.springframework.security.authentication.BadCredentialsException 凭据无效
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取当前用户信息（刷新 token 后恢复状态）。
     *
     * @param user        当前认证用户
     * @param bearerToken 原始 Bearer Token
     * @return JWT token + 用户概要
     */
    LoginResponse me(CurrentUser user, String bearerToken);

    /**
     * 登出：将当前 token 加入黑名单。
     *
     * @param user 当前认证用户（含 jti 和 expiresAt）
     */
    void logout(CurrentUser user);
}
