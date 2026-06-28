package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.MeetingReservation;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会议室预约服务接口
 * 提供会议室预约创建、可用性检查等功能
 */
public interface MeetingReservationService extends IService<MeetingReservation> {

    /**
     * 创建会议室预约
     * @param reservation 预约实体
     * @return 创建后的预约
     */
    MeetingReservation create(MeetingReservation reservation);

    /**
     * 检查会议室在指定时间段是否可用
     * @param roomId 会议室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否可用
     */
    boolean checkAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询员工的预约记录
     * @param empId 员工ID
     * @return 预约记录列表
     */
    List<MeetingReservation> getByEmpId(Long empId);

    /**
     * 查询会议室的预约记录
     * @param roomId 会议室ID
     * @return 预约记录列表
     */
    List<MeetingReservation> getByRoomId(Long roomId);

    /**
     * 查询指定日期范围内的预约记录
     * @param roomId 会议室ID（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 预约记录列表
     */
    List<MeetingReservation> getByDateRange(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
}