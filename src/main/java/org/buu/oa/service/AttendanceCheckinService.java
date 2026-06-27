package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.AttendanceCheckin;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤打卡服务接口
 * 提供考勤打卡、查询等考勤相关功能
 */
public interface AttendanceCheckinService extends IService<AttendanceCheckin> {

    /**
     * 查询员工月度考勤记录
     * @param empId 员工ID
     * @param year 年份
     * @param month 月份
     * @return 考勤记录列表
     */
    List<AttendanceCheckin> getByEmpIdAndMonth(Long empId, int year, int month);

    /**
     * 上班打卡
     * @param empId 员工ID
     */
    void checkIn(Long empId);

    /**
     * 下班打卡
     * @param empId 员工ID
     */
    void checkOut(Long empId);

    /**
     * 标记请假日期的考勤记录
     * @param empId 员工ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param status 考勤状态
     */
    void markLeaveDates(Long empId, LocalDate startDate, LocalDate endDate, Integer status);
}