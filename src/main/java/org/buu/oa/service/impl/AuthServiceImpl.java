package org.buu.oa.service.impl;

import org.buu.oa.entity.SysUser;
import org.buu.oa.service.AuthService;
import org.buu.oa.service.SysUserService;
import org.buu.oa.util.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类
 * 实现用户登录、获取当前用户信息等认证相关功能
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(SysUserService sysUserService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 用户登录
     * 验证用户名和密码，生成JWT令牌
     * @param username 用户名
     * @param password 密码
     * @return JWT令牌
     * @throws org.springframework.security.authentication.BadCredentialsException 用户名或密码错误时抛出
     */
    @Override
    public String login(String username, String password) {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("用户名或密码错误");
        }
        return jwtUtil.generateToken(user.getId(), username);
    }

    /**
     * 获取当前登录用户信息
     * 从SecurityContext中获取认证信息，查询用户详情
     * @return 当前用户信息，未登录返回null
     */
    @Override
    public SysUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return sysUserService.getByUsername(username);
        }
        return null;
    }
}