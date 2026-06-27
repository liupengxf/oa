package org.buu.oa.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.buu.oa.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 拦截请求并验证JWT令牌，将用户信息存入SecurityContext
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    /**
     * 构造函数
     * @param jwtUtil JWT工具类实例
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 执行JWT认证过滤
     * 从请求头中提取JWT令牌，验证并设置认证信息
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 从请求头获取Authorization
        String authHeader = request.getHeader("Authorization");
        
        // 检查Authorization头是否存在且以Bearer开头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // 提取JWT令牌（去掉"Bearer "前缀）
        String token = authHeader.substring(7);
        
        try {
            // 从令牌中解析用户名
            String username = jwtUtil.getUsernameFromToken(token);
            // 如果用户名存在且当前未认证
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // 创建用户详情对象
                User userDetails = new User(username, "", Collections.singletonList(new SimpleGrantedAuthority("USER")));
                // 创建认证令牌
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                // 设置请求详情
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 将认证信息存入SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // JWT验证失败，记录警告日志
            logger.warn("JWT token validation failed: " + e.getMessage());
        }
        
        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}