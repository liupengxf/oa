package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.LeaveApplication;

import java.util.List;

/**
 * 请假申请服务接口
 * 提供请假申请创建、审批等请假相关功能
 */
public interface LeaveApplicationService extends IService<LeaveApplication> {

    /**
     * 创建请假申请
     * @param application 请假申请实体
     * @return 创建后的请假申请
     */
    LeaveApplication create(LeaveApplication application);

    /**
     * 审批请假申请
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
    List<LeaveApplication> getPendingByDeptId(Long deptId);

    /**
     * 查询员工的请假记录
     * @param empId 员工ID
     * @return 请假记录列表
     */
    List<LeaveApplication> getByEmpId(Long empId);
}