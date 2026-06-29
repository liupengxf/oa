package org.buu.oa.controller;

import lombok.Data;
import org.buu.oa.common.Result;
import org.buu.oa.entity.ExpenseReport;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.ExpenseReportService;
import org.buu.oa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报销控制器
 * 处理报销申请、审批等报销相关接口
 */
@RestController
@RequestMapping("/api/expense")
public class ExpenseController {

    private final ExpenseReportService expenseReportService;
    private final AuthService authService;

    public ExpenseController(ExpenseReportService expenseReportService, AuthService authService) {
        this.expenseReportService = expenseReportService;
        this.authService = authService;
    }

    /**
     * 报销申请请求参数
     */
    @Data
    public static class ExpenseRequest {
        /** 报销类型 */
        private String expenseType;
        /** 报销金额 */
        private java.math.BigDecimal totalAmount;
        /** 报销说明 */
        private String description;
    }

    /**
     * 审批请求参数
     */
    @Data
    public static class ApproveRequest {
        /** 申请ID */
        private Long id;
        /** 审批结果（1-通过，2-驳回） */
        private Integer result;
        /** 审批意见 */
        private String opinion;
    }

    /**
     * 创建报销申请
     * @param request 报销请求
     * @return 创建的报销申请
     */
    @PostMapping
    public Result<ExpenseReport> create(@RequestBody ExpenseRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<ExpenseReport>unauthorized("未登录");
        }
        
        ExpenseReport report = new ExpenseReport();
        report.setEmpId(user.getEmpId());
        report.setExpenseType(request.getExpenseType());
        report.setTotalAmount(request.getTotalAmount());
        report.setDescription(request.getDescription());
        
        ExpenseReport created = expenseReportService.create(report);
        return Result.success("报销申请提交成功", created);
    }

    /**
     * 审批报销申请
     * @param request 审批请求
     * @return 操作结果
     */
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody ApproveRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Void>unauthorized("未登录");
        }
        expenseReportService.approve(request.getId(), user.getId(), request.getResult(), request.getOpinion());
        return Result.<Void>success(request.getResult() == 1 ? "审批通过" : "审批驳回", null);
    }

    /**
     * 查询部门待审批列表
     * @param deptId 部门ID
     * @return 待审批报销列表
     */
    @GetMapping("/pending")
    public Result<List<ExpenseReport>> getPending(@RequestParam Long deptId) {
        List<ExpenseReport> list = expenseReportService.getPendingByDeptId(deptId);
        return Result.success(list);
    }

    /**
     * 查询我的报销记录
     * @return 我的报销记录列表
     */
    @GetMapping("/my")
    public Result<List<ExpenseReport>> getMyExpenses() {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<List<ExpenseReport>>unauthorized("未登录");
        }
        List<ExpenseReport> list = expenseReportService.getByEmpId(user.getEmpId());
        return Result.success(list);
    }

    /**
     * 查询所有报销申请列表
     * @return 报销申请列表
     */
    @GetMapping("/list")
    public Result<List<ExpenseReport>> list() {
        List<ExpenseReport> list = expenseReportService.list();
        return Result.success(list);
    }
}