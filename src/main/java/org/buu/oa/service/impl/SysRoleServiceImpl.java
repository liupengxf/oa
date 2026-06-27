package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.SysRole;
import org.buu.oa.entity.SysUserRole;
import org.buu.oa.mapper.SysRoleMapper;
import org.buu.oa.mapper.SysUserRoleMapper;
import org.buu.oa.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类
 * 实现角色查询等角色管理相关功能
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    private final SysUserRoleMapper sysUserRoleMapper;

    public SysRoleServiceImpl(SysUserRoleMapper sysUserRoleMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
    }

    /**
     * 根据用户ID查询角色列表
     * 通过用户角色关联表查询用户拥有的角色
     * @param userId 用户ID
     * @return 角色列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SysRole> listByUserId(Long userId) {
        // 查询用户角色关联记录
        LambdaQueryWrapper<SysUserRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(roleWrapper);
        
        // 提取角色ID列表
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        
        // 根据角色ID列表查询角色信息
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SysRole::getId, roleIds);
        return baseMapper.selectList(wrapper);
    }
}