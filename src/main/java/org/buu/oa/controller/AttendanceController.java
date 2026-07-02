package org.buu.oa.controller;

import org.buu.oa.common.Result;
import org.buu.oa.entity.AttendanceCheckin;
import org.buu.oa.entity.LeaveApplication;
import org.buu.oa.service.AttendanceCheckinService;
import org.buu.oa.service.LeaveApplicationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 考勤控制器
 * 处理考勤打卡、查询等考勤相关接口
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceCheckinService attendanceCheckinService;
    private final LeaveApplicationService leaveApplicationService;

    /** 默认员工ID（未实现登录功能时使用） */
    private static final Long DEFAULT_EMP_ID = 1L;

    public AttendanceController(AttendanceCheckinService attendanceCheckinService, 
                               LeaveApplicationService leaveApplicationService) {
        this.attendanceCheckinService = attendanceCheckinService;
        this.leaveApplicationService = leaveApplicationService;
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
     * 合并打卡记录和请假记录（仅用于显示）
     * @param year 年份（默认2026）
     * @param month 月份（默认5）
     * @return 考勤记录列表
     */
    @GetMapping("/monthly")
    public Result<List<AttendanceCheckin>> getMonthlyAttendance(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "5") int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<AttendanceCheckin> checkinRecords = attendanceCheckinService.getByEmpIdAndMonth(DEFAULT_EMP_ID, year, month);
        
        Map<LocalDate, AttendanceCheckin> recordMap = new HashMap<>();
        for (AttendanceCheckin record : checkinRecords) {
            recordMap.put(record.getCheckDate(), record);
        }
        
        List<LeaveApplication> leaveApplications = leaveApplicationService.getByEmpId(DEFAULT_EMP_ID);
        for (LeaveApplication leave : leaveApplications) {
            if (!"COMPLETED".equals(leave.getStatus())) {
                continue;
            }
            
            LocalDate leaveStart = leave.getStartDate();
            LocalDate leaveEnd = leave.getEndDate();
            
            if (leaveEnd.isBefore(startDate) || leaveStart.isAfter(endDate)) {
                continue;
            }
            
            LocalDate currentDate = leaveStart.isBefore(startDate) ? startDate : leaveStart;
            LocalDate lastDate = leaveEnd.isAfter(endDate) ? endDate : leaveEnd;
            
            while (!currentDate.isAfter(lastDate)) {
                if (!recordMap.containsKey(currentDate)) {
                    AttendanceCheckin record = new AttendanceCheckin();
                    record.setEmpId(DEFAULT_EMP_ID);
                    record.setCheckDate(currentDate);
                    record.setStatus(4);
                    recordMap.put(currentDate, record);
                }
                
                currentDate = currentDate.plusDays(1);
            }
        }
        
        return Result.success(new ArrayList<>(recordMap.values()));
    }

    /**
     * 查询月度请假统计
     * @param year 年份
     * @param month 月份
     * @return 请假统计数据
     */
    @GetMapping("/leave-stats")
    public Result<Map<String, Object>> getLeaveStats(
            @RequestParam(defaultValue = "2026") int year,
            @RequestParam(defaultValue = "5") int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        List<LeaveApplication> leaveApplications = leaveApplicationService.getByEmpId(DEFAULT_EMP_ID);
        
        int leaveDays = 0;
        for (LeaveApplication leave : leaveApplications) {
            if (!"COMPLETED".equals(leave.getStatus())) {
                continue;
            }
            
            LocalDate leaveStart = leave.getStartDate();
            LocalDate leaveEnd = leave.getEndDate();
            
            if (leaveEnd.isBefore(startDate) || leaveStart.isAfter(endDate)) {
                continue;
            }
            
            LocalDate currentDate = leaveStart.isBefore(startDate) ? startDate : leaveStart;
            LocalDate lastDate = leaveEnd.isAfter(endDate) ? endDate : leaveEnd;
            
            while (!currentDate.isAfter(lastDate)) {
                leaveDays++;
                currentDate = currentDate.plusDays(1);
            }
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("leaveDays", leaveDays);
        
        return Result.success(stats);
    }
}
