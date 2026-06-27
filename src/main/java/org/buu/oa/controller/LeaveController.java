package org.buu.oa.controller;

import lombok.Data;
import org.buu.oa.common.Result;
import org.buu.oa.entity.LeaveApplication;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.LeaveApplicationService;
import org.buu.oa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 请假控制器
 * 处理请假申请、审批等请假相关接口
 */
@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    private final LeaveApplicationService leaveApplicationService;
    private final AuthService authService;

    public LeaveController(LeaveApplicationService leaveApplicationService, AuthService authService) {
        this.leaveApplicationService = leaveApplicationService;
        this.authService = authService;
    }

    /**
     * 请假申请请求参数
     */
    @Data
    public static class LeaveRequest {
        /** 请假类型（1-事假，2-病假，3-年假等） */
        private Integer leaveType;
        /** 开始日期 */
        private String startDate;
        /** 结束日期 */
        private String endDate;
        /** 请假原因 */
        private String reason;
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
     * 创建请假申请
     * @param request 请假请求
     * @return 创建的请假申请
     */
    @PostMapping
    public Result<LeaveApplication> create(@RequestBody LeaveRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<LeaveApplication>unauthorized("未登录");
        }
        
        LeaveApplication application = new LeaveApplication();
        application.setEmpId(user.getEmpId());
        application.setLeaveType(request.getLeaveType());
        application.setStartDate(java.time.LocalDate.parse(request.getStartDate()));
        application.setEndDate(java.time.LocalDate.parse(request.getEndDate()));
        // 计算请假天数（包含起止日期）
        application.setDays(java.math.BigDecimal.valueOf(
            java.time.temporal.ChronoUnit.DAYS.between(
                java.time.LocalDate.parse(request.getStartDate()),
                java.time.LocalDate.parse(request.getEndDate())) + 1));
        application.setReason(request.getReason());
        
        LeaveApplication created = leaveApplicationService.create(application);
        return Result.success("请假申请提交成功", created);
    }

    /**
     * 审批请假申请
     * @param request 审批请求
     * @return 操作结果
     */
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody ApproveRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Void>unauthorized("未登录");
        }
        leaveApplicationService.approve(request.getId(), user.getId(), request.getResult(), request.getOpinion());
        return Result.<Void>success(request.getResult() == 1 ? "审批通过" : "审批驳回", null);
    }

    /**
     * 查询部门待审批列表
     * @param deptId 部门ID
     * @return 待审批请假列表
     */
    @GetMapping("/pending")
    public Result<List<LeaveApplication>> getPending(@RequestParam Long deptId) {
        List<LeaveApplication> list = leaveApplicationService.getPendingByDeptId(deptId);
        return Result.success(list);
    }

    /**
     * 查询我的请假记录
     * @return 我的请假记录列表
     */
    @GetMapping("/my")
    public Result<List<LeaveApplication>> getMyLeaves() {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<List<LeaveApplication>>unauthorized("未登录");
        }
        List<LeaveApplication> list = leaveApplicationService.getByEmpId(user.getEmpId());
        return Result.success(list);
    }
}