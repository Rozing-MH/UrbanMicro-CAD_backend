package com.urbanmicrocad.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.urbanmicrocad.auth.dto.LoginRequest;
import com.urbanmicrocad.auth.dto.LoginResponse;
import com.urbanmicrocad.auth.dto.RegisterRequest;
import com.urbanmicrocad.auth.entity.SysUser;
import com.urbanmicrocad.auth.mapper.SysUserMapper;
import com.urbanmicrocad.common.exception.ApiException;
import com.urbanmicrocad.common.exception.ErrorCode;
import com.urbanmicrocad.common.security.CurrentUser;
import com.urbanmicrocad.common.security.JwtService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(SysUserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public void register(RegisterRequest request) {
        SysUser existing = userMapper.selectOne(activeUserQuery(request.username()));
        if (existing != null) {
            throw new ApiException(ErrorCode.CONFLICT, "用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setIsDeleted(false);
        try {
            userMapper.insert(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ApiException(ErrorCode.CONFLICT, "用户名已存在");
        }
    }

    public LoginResponse login(LoginRequest request) {
        SysUser user = userMapper.selectOne(activeUserQuery(request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("invalid credentials");
        }
        CurrentUser currentUser = new CurrentUser(user.getId(), user.getUsername(), user.getRole());
        return toLoginResponse(currentUser, jwtService.generateToken(currentUser));
    }

    public LoginResponse me(CurrentUser user, String bearerToken) {
        String token = bearerToken != null && bearerToken.startsWith("Bearer ") ? bearerToken.substring(7) : "";
        return toLoginResponse(user, token);
    }

    private LoginResponse toLoginResponse(CurrentUser user, String token) {
        return new LoginResponse(token, user.id().toString(), user.username(), user.role());
    }

    private LambdaQueryWrapper<SysUser> activeUserQuery(String username) {
        return new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, username)
            .eq(SysUser::getIsDeleted, false);
    }
}
