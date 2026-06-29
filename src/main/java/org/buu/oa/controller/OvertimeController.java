package org.buu.oa.controller;

import lombok.Data;
import org.buu.oa.common.Result;
import org.buu.oa.entity.OvertimeApplication;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.OvertimeApplicationService;
import org.buu.oa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 加班控制器
 * 处理加班申请、审批等加班相关接口
 */
@RestController
@RequestMapping("/api/overtime")
public class OvertimeController {

    private final OvertimeApplicationService overtimeApplicationService;
    private final AuthService authService;

    public OvertimeController(OvertimeApplicationService overtimeApplicationService, AuthService authService) {
        this.overtimeApplicationService = overtimeApplicationService;
        this.authService = authService;
    }

    /**
     * 加班申请请求参数
     */
    @Data
    public static class OvertimeRequest {
        /** 加班类型（1-工作日加班，2-周末加班，3-节假日加班） */
        private Integer overtimeType;
        /** 开始时间 */
        private String startTime;
        /** 结束时间 */
        private String endTime;
        /** 加班原因 */
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
     * 创建加班申请
     * @param request 加班请求
     * @return 创建的加班申请
     */
    @PostMapping
    public Result<OvertimeApplication> create(@RequestBody OvertimeRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<OvertimeApplication>unauthorized("未登录");
        }
        
        OvertimeApplication application = new OvertimeApplication();
        application.setEmpId(user.getEmpId());
        application.setOvertimeType(request.getOvertimeType());
        application.setStartTime(java.time.LocalDateTime.parse(request.getStartTime()));
        application.setEndTime(java.time.LocalDateTime.parse(request.getEndTime()));
        
        // 计算加班时长（小时）
        long hours = java.time.Duration.between(
            java.time.LocalDateTime.parse(request.getStartTime()),
            java.time.LocalDateTime.parse(request.getEndTime())).toHours();
        application.setHours(java.math.BigDecimal.valueOf(hours));
        application.setReason(request.getReason());
        
        OvertimeApplication created = overtimeApplicationService.create(application);
        return Result.success("加班申请提交成功", created);
    }

    /**
     * 审批加班申请
     * @param request 审批请求
     * @return 操作结果
     */
    @PostMapping("/approve")
    public Result<Void> approve(@RequestBody ApproveRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Void>unauthorized("未登录");
        }
        overtimeApplicationService.approve(request.getId(), user.getId(), request.getResult(), request.getOpinion());
        return Result.<Void>success(request.getResult() == 1 ? "审批通过" : "审批驳回", null);
    }

    /**
     * 查询部门待审批列表
     * @param deptId 部门ID
     * @return 待审批加班列表
     */
    @GetMapping("/pending")
    public Result<List<OvertimeApplication>> getPending(@RequestParam Long deptId) {
        List<OvertimeApplication> list = overtimeApplicationService.getPendingByDeptId(deptId);
        return Result.success(list);
    }

    /**
     * 查询我的加班记录
     * @return 我的加班记录列表
     */
    @GetMapping("/my")
    public Result<List<OvertimeApplication>> getMyOvertimes() {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<List<OvertimeApplication>>unauthorized("未登录");
        }
        List<OvertimeApplication> list = overtimeApplicationService.getByEmpId(user.getEmpId());
        return Result.success(list);
    }

    /**
     * 查询所有加班申请记录（包含员工信息）
     * @return 所有加班申请列表
     */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getAllOvertimes() {
        List<Map<String, Object>> list = overtimeApplicationService.getAllWithEmp();
        return Result.success(list);
    }
}