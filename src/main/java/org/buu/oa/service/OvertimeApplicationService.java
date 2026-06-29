package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.OvertimeApplication;

import java.util.List;

/**
 * 加班申请服务接口
 * 提供加班申请创建、审批等加班相关功能
 */
public interface OvertimeApplicationService extends IService<OvertimeApplication> {

    /**
     * 创建加班申请
     * @param application 加班申请实体
     * @return 创建后的加班申请
     */
    OvertimeApplication create(OvertimeApplication application);

    /**
     * 审批加班申请
     * @param id 申请ID
     * @param approverId 审批人ID
     * @param result 审批结果（1-通过，2-驳回）
     * @param opinion 审批意见
     */
    void approve(Long id, Long approverId, Integer result, String opinion);

    /**
     * 查询部门待审批列表
     * @param deptId 部门ID
     * @return 待审批列表
     */
    List<OvertimeApplication> getPendingByDeptId(Long deptId);

    /**
     * 查询员工的加班记录
     * @param empId 员工ID
     * @return 加班记录列表
     */
    List<OvertimeApplication> getByEmpId(Long empId);
}