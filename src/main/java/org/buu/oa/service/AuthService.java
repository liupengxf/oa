package org.buu.oa.service;

import org.buu.oa.entity.SysUser;

/**
 * 认证服务接口
 * 提供用户登录、获取当前用户信息等认证相关功能
 */
public interface AuthService {

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return JWT令牌
     */
    String login(String username, String password);

    /**
     * 获取当前登录用户信息
     * @return 当前用户信息，未登录返回null
     */
    SysUser getCurrentUser();
}