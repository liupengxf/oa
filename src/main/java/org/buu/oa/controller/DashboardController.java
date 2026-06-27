package org.buu.oa.controller;

import org.buu.oa.common.Result;
import org.buu.oa.entity.EmpEmployee;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供工作台首页数据统计功能
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AuthService authService;
    private final SysDeptService sysDeptService;
    private final EmpEmployeeService empEmployeeService;
    private final LeaveApplicationService leaveApplicationService;
    private final OvertimeApplicationService overtimeApplicationService;
    private final ExpenseReportService expenseReportService;
    private final SysNoticeService sysNoticeService;

    public DashboardController(AuthService authService,
                              SysDeptService sysDeptService,
                              EmpEmployeeService empEmployeeService,
                              LeaveApplicationService leaveApplicationService,
                              OvertimeApplicationService overtimeApplicationService,
                              ExpenseReportService expenseReportService,
                              SysNoticeService sysNoticeService) {
        this.authService = authService;
        this.sysDeptService = sysDeptService;
        this.empEmployeeService = empEmployeeService;
        this.leaveApplicationService = leaveApplicationService;
        this.overtimeApplicationService = overtimeApplicationService;
        this.expenseReportService = expenseReportService;
        this.sysNoticeService = sysNoticeService;
    }

    /**
     * 获取仪表盘统计数据
     * 包含部门数量、员工数量、待审批数量、未读通知数量等
     * @return 统计数据Map
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Map<String, Object>>unauthorized("未登录");
        }
        
        Map<String, Object> stats = new HashMap<>();
        
        // 基础统计数据
        stats.put("deptCount", sysDeptService.count());
        stats.put("empCount", empEmployeeService.count());
        
        // 待审批数据（根据用户所属部门查询）
        if (user.getEmpId() != null) {
            EmpEmployee employee = empEmployeeService.getById(user.getEmpId());
            if (employee != null) {
                stats.put("pendingLeaveCount", 
                    leaveApplicationService.getPendingByDeptId(employee.getDeptId()).size());
                stats.put("pendingOvertimeCount", 
                    overtimeApplicationService.getPendingByDeptId(employee.getDeptId()).size());
                stats.put("pendingExpenseCount", 
                    expenseReportService.getPendingByDeptId(employee.getDeptId()).size());
                stats.put("unreadNoticeCount", sysNoticeService.countUnread(user.getId()));
            }
        }
        
        return Result.success(stats);
    }
}