package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.SysMenu;
import org.buu.oa.entity.SysRole;
import org.buu.oa.mapper.SysMenuMapper;
import org.buu.oa.service.SysMenuService;
import org.buu.oa.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务实现类
 * 实现菜单查询等菜单管理相关功能
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private final SysRoleService sysRoleService;

    public SysMenuServiceImpl(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    /**
     * 根据用户ID查询菜单列表
     * 查询用户所有角色对应的菜单，去重后按排序字段排序
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getMenusByUserId(Long userId) {
        // 查询用户所有角色
        List<SysRole> roles = sysRoleService.listByUserId(userId);
        List<SysMenu> menus = new ArrayList<>();
        
        // 遍历角色，查询每个角色对应的菜单
        for (SysRole role : roles) {
            List<SysMenu> roleMenus = baseMapper.selectMenusByRoleId(role.getId());
            // 去重添加菜单
            for (SysMenu menu : roleMenus) {
                if (!menus.contains(menu)) {
                    menus.add(menu);
                }
            }
        }
        
        // 按排序字段正序排列
        menus.sort((a, b) -> Integer.compare(a.getSort(), b.getSort()));
        return menus;
    }
}