package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.ExpenseReport;

import java.util.List;
import java.util.Map;

/**
 * 费用报销服务接口
 * 提供费用报销创建、审批等报销相关功能
 */
public interface ExpenseReportService extends IService<ExpenseReport> {

    /**
     * 创建费用报销
     * @param report 报销实体
     * @return 创建后的报销
     */
    ExpenseReport create(ExpenseReport report);

    /**
     * 审批费用报销
     * @param id 报销ID
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
    List<ExpenseReport> getPendingByDeptId(Long deptId);

    /**
     * 查询员工的报销记录
     * @param empId 员工ID
     * @return 报销记录列表
     */
    List<ExpenseReport> getByEmpId(Long empId);

    /**
     * 查询所有报销申请（包含员工信息）
     * @return 报销申请列表
     */
    List<Map<String, Object>> getAllWithEmp();
}