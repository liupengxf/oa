package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.MeetingReservation;
import org.buu.oa.mapper.MeetingReservationMapper;
import org.buu.oa.service.MeetingReservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 会议室预约服务实现类
 * 实现会议室预约创建、可用性检查等功能
 */
@Service
public class MeetingReservationServiceImpl extends ServiceImpl<MeetingReservationMapper, MeetingReservation> implements MeetingReservationService {

    /** 序列号生成器，用于生成预约单号 */
    private final AtomicLong sequence = new AtomicLong(1);

    /**
     * 创建会议室预约
     * 生成唯一的预约单号，设置状态为有效，提醒状态为未提醒
     * @param reservation 预约实体
     * @return 创建后的预约
     */
    @Override
    public MeetingReservation create(MeetingReservation reservation) {
        // 生成预约单号：HY + 年月 + 3位序列号
        String no = "HY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + 
                     String.format("%03d", sequence.getAndIncrement());
        reservation.setReservationNo(no);
        reservation.setStatus(1);           // 设置为有效状态
        reservation.setRemindStatus(0);     // 设置为未提醒状态
        baseMapper.insert(reservation);
        return reservation;
    }

    /**
     * 检查会议室在指定时间段是否可用
     * 查询是否存在时间重叠的预约
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否可用
     */
    @Override
    public boolean checkAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<MeetingReservation> overlapping = baseMapper.selectOverlappingReservations(roomId, startTime, endTime);
        return overlapping.isEmpty();
    }

    /**
     * 查询员工的预约记录
     * 按开始时间倒序排列
     * @param empId 员工ID
     * @return 预约记录列表
     */
    @Override
    public List<MeetingReservation> getByEmpId(Long empId) {
        LambdaQueryWrapper<MeetingReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingReservation::getEmpId, empId);
        wrapper.orderByDesc(MeetingReservation::getStartTime);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查询会议室的有效预约记录
     * 按开始时间正序排列
     * @param roomId 会议室ID
     * @return 预约记录列表
     */
    @Override
    public List<MeetingReservation> getByRoomId(Long roomId) {
        LambdaQueryWrapper<MeetingReservation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingReservation::getRoomId, roomId);
        wrapper.eq(MeetingReservation::getStatus, 1);  // 只查询有效预约
        wrapper.orderByAsc(MeetingReservation::getStartTime);
        return baseMapper.selectList(wrapper);
    }
}