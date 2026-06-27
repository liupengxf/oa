package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.EmpEmployee;

import java.util.List;

/**
 * 员工服务接口
 * 提供员工查询等员工管理相关功能
 */
public interface EmpEmployeeService extends IService<EmpEmployee> {

    /**
     * 根据用户ID查询员工信息
     * @param userId 用户ID
     * @return 员工信息
     */
    EmpEmployee getByUserId(Long userId);

    /**
     * 根据部门ID查询员工列表
     * @param deptId 部门ID
     * @return 员工列表
     */
    List<EmpEmployee> listByDeptId(Long deptId);
}