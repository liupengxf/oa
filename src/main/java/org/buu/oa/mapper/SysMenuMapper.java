package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.SysMenu;

import java.util.List;

public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectMenusByRoleId(Long roleId);
}