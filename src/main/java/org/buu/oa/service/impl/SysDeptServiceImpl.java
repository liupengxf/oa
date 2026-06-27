package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.SysDept;
import org.buu.oa.mapper.SysDeptMapper;
import org.buu.oa.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现类
 * 实现部门查询等部门管理相关功能
 */
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    /**
     * 查询所有部门列表
     * 按排序字段正序排列
     * @return 部门列表
     */
    @Override
    public List<SysDept> listAll() {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysDept::getSort);  // 按排序字段正序排列
        return baseMapper.selectList(wrapper);
    }
}