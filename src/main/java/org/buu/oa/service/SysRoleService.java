package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 * 提供角色查询等角色管理相关功能
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID查询角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> listByUserId(Long userId);
}