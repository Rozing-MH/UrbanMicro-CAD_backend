package com.urbanmicrocad.common.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.urbanmicrocad.auth.entity.SysUser;
import com.urbanmicrocad.auth.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final SysUserMapper userMapper;

    public CurrentUserService(SysUserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CurrentUser loadActiveUser(Long userId) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getId, userId)
            .eq(SysUser::getIsDeleted, false));
        if (user == null) return null;
        return new CurrentUser(user.getId(), user.getUsername(), user.getRole());
    }
}
