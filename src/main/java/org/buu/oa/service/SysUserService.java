package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.SysUser;

/**
 * 用户服务接口
 * 提供用户查询等用户管理相关功能
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);
}