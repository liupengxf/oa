package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.SysUser;
import org.buu.oa.mapper.SysUserMapper;
import org.buu.oa.service.SysUserService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 实现用户查询等用户管理相关功能
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息，不存在返回null
     */
    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return baseMapper.selectOne(wrapper);
    }
}