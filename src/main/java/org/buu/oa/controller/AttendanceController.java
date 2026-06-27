package org.buu.oa.controller;

import org.buu.oa.common.Result;
import org.buu.oa.entity.AttendanceCheckin;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.AttendanceCheckinService;
import org.buu.oa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考勤控制器
 * 处理考勤打卡、查询等考勤相关接口
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceCheckinService attendanceCheckinService;
    private final AuthService authService;

    public AttendanceController(AttendanceCheckinService attendanceCheckinService, AuthService authService) {
        this.attendanceCheckinService = attendanceCheckinService;
        this.authService = authService;
    }

    /**
     * 上班打卡
     * @return 操作结果
     */
    @PostMapping("/checkin")
    public Result<Void> checkIn() {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<Void>unauthorized("未登录");
        }
        attendanceCheckinService.checkIn(user.getEmpId());
        return Result.<Void>success("上班打卡成功", null);
    }

    /**
     * 下班打卡
     * @return 操作结果
     */
    @PostMapping("/checkout")
    public Result<Void> checkOut() {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<Void>unauthorized("未登录");
        }
        attendanceCheckinService.checkOut(user.getEmpId());
        return Result.<Void>success("下班打卡成功", null);
    }

    /**
     * 查询月度考勤记录
     * @param year 年份（默认2026）
     * @param month 月份（默认5）
     * @return 考勤记录列表
     */
    @GetMapping("/monthly")
    public Result<List<AttendanceCheckin>> getMonthlyAttendance(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "5") int month) {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<List<AttendanceCheckin>>unauthorized("未登录");
        }
        List<AttendanceCheckin> records = attendanceCheckinService.getByEmpIdAndMonth(user.getEmpId(), year, month);
        return Result.success(records);
    }
}