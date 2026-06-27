package org.buu.oa.controller;

import org.buu.oa.common.Result;
import org.buu.oa.entity.AttendanceCheckin;
import org.buu.oa.service.AttendanceCheckinService;
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

    /** 默认员工ID（未实现登录功能时使用） */
    private static final Long DEFAULT_EMP_ID = 1L;

    public AttendanceController(AttendanceCheckinService attendanceCheckinService) {
        this.attendanceCheckinService = attendanceCheckinService;
    }

    /**
     * 上班打卡
     * @return 操作结果
     */
    @PostMapping("/checkin")
    public Result<Void> checkIn() {
        attendanceCheckinService.checkIn(DEFAULT_EMP_ID);
        return Result.<Void>success("上班打卡成功", null);
    }

    /**
     * 下班打卡
     * @return 操作结果
     */
    @PostMapping("/checkout")
    public Result<Void> checkOut() {
        attendanceCheckinService.checkOut(DEFAULT_EMP_ID);
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
        List<AttendanceCheckin> records = attendanceCheckinService.getByEmpIdAndMonth(DEFAULT_EMP_ID, year, month);
        return Result.success(records);
    }
}
