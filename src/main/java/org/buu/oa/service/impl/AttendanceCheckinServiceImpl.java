package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.AttendanceCheckin;
import org.buu.oa.mapper.AttendanceCheckinMapper;
import org.buu.oa.service.AttendanceCheckinService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

/**
 * 考勤打卡服务实现类
 * 实现考勤打卡、查询等考勤相关功能
 */
@Service
public class AttendanceCheckinServiceImpl extends ServiceImpl<AttendanceCheckinMapper, AttendanceCheckin> implements AttendanceCheckinService {

    /** 上班时间阈值（9:00） */
    private static final LocalTime CHECKIN_THRESHOLD = LocalTime.of(9, 0);

    /**
     * 查询员工月度考勤记录
     * @param empId 员工ID
     * @param year 年份
     * @param month 月份
     * @return 考勤记录列表
     */
    @Override
    public List<AttendanceCheckin> getByEmpIdAndMonth(Long empId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return baseMapper.selectByEmpIdAndDateRange(empId, startDate, endDate);
    }

    /**
     * 上班打卡
     * 查询当天是否已有打卡记录，有则更新，无则创建
     * @param empId 员工ID
     */
    @Override
    public void checkIn(Long empId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<AttendanceCheckin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceCheckin::getEmpId, empId);
        wrapper.eq(AttendanceCheckin::getCheckDate, today);
        
        AttendanceCheckin checkin = baseMapper.selectOne(wrapper);
        if (checkin == null) {
            checkin = new AttendanceCheckin();
            checkin.setEmpId(empId);
            checkin.setCheckDate(today);
            checkin.setStatus(1);
        }
        
        checkin.setCheckInTime(LocalDateTime.now());
        updateStatus(checkin);
        
        if (checkin.getId() == null) {
            baseMapper.insert(checkin);
        } else {
            baseMapper.updateById(checkin);
        }
    }

    /**
     * 下班打卡
     * 查询当天是否已有打卡记录，有则更新，无则创建
     * @param empId 员工ID
     */
    @Override
    public void checkOut(Long empId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<AttendanceCheckin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceCheckin::getEmpId, empId);
        wrapper.eq(AttendanceCheckin::getCheckDate, today);
        
        AttendanceCheckin checkin = baseMapper.selectOne(wrapper);
        if (checkin == null) {
            checkin = new AttendanceCheckin();
            checkin.setEmpId(empId);
            checkin.setCheckDate(today);
            checkin.setStatus(1);
        }
        
        checkin.setCheckOutTime(LocalDateTime.now());
        updateStatus(checkin);
        
        if (checkin.getId() == null) {
            baseMapper.insert(checkin);
        } else {
            baseMapper.updateById(checkin);
        }
    }

    /**
     * 更新打卡状态
     * 状态值：1-正常，2-迟到，3-缺卡
     * @param checkin 打卡记录
     */
    private void updateStatus(AttendanceCheckin checkin) {
        if (checkin.getCheckInTime() == null && checkin.getCheckOutTime() == null) {
            checkin.setStatus(3);
        } else if (checkin.getCheckInTime() != null) {
            LocalTime checkinTime = checkin.getCheckInTime().toLocalTime();
            if (checkinTime.isAfter(CHECKIN_THRESHOLD)) {
                checkin.setStatus(2);
            } else {
                checkin.setStatus(1);
            }
        }
    }
}
