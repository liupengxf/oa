package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.EmpEmployee;
import org.buu.oa.entity.SysUser;
import org.buu.oa.mapper.EmpEmployeeMapper;
import org.buu.oa.mapper.SysUserMapper;
import org.buu.oa.service.EmpEmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 员工服务实现类
 * 实现员工查询等员工管理相关功能
 */
@Service
public class EmpEmployeeServiceImpl extends ServiceImpl<EmpEmployeeMapper, EmpEmployee> implements EmpEmployeeService {

    private final SysUserMapper sysUserMapper;

    public EmpEmployeeServiceImpl(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    /**
     * 根据用户ID查询员工信息
     * 通过用户表关联查询员工表获取员工详情
     * @param userId 用户ID
     * @return 员工信息，不存在返回null
     */
    @Override
    @Transactional(readOnly = true)
    public EmpEmployee getByUserId(Long userId) {
        // 查询用户信息，获取员工ID
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getEmpId() == null) {
            return null;
        }
        // 根据员工ID查询员工详情
        return baseMapper.selectById(user.getEmpId());
    }

    /**
     * 根据部门ID查询员工列表
     * 按员工编号正序排列
     * @param deptId 部门ID
     * @return 员工列表
     */
    @Override
    public List<EmpEmployee> listByDeptId(Long deptId) {
        LambdaQueryWrapper<EmpEmployee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmpEmployee::getDeptId, deptId);  // 查询指定部门的员工
        wrapper.orderByAsc(EmpEmployee::getEmpNo);   // 按员工编号正序排列
        return baseMapper.selectList(wrapper);
    }
}