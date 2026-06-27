package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.SysDept;

import java.util.List;

/**
 * 部门服务接口
 * 提供部门查询等部门管理相关功能
 */
public interface SysDeptService extends IService<SysDept> {

    /**
     * 查询所有部门列表
     * @return 部门列表
     */
    List<SysDept> listAll();
}