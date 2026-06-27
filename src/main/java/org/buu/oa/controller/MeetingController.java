package org.buu.oa.controller;

import lombok.Data;
import org.buu.oa.common.Result;
import org.buu.oa.entity.MeetingRoom;
import org.buu.oa.entity.MeetingReservation;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.MeetingRoomService;
import org.buu.oa.service.MeetingReservationService;
import org.buu.oa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会议控制器
 * 处理会议室查询、会议预约、预约取消等会议相关接口
 */
@RestController
@RequestMapping("/api/meeting")
public class MeetingController {

    private final MeetingRoomService meetingRoomService;
    private final MeetingReservationService meetingReservationService;
    private final AuthService authService;

    public MeetingController(MeetingRoomService meetingRoomService, 
                            MeetingReservationService meetingReservationService,
                            AuthService authService) {
        this.meetingRoomService = meetingRoomService;
        this.meetingReservationService = meetingReservationService;
        this.authService = authService;
    }

    /**
     * 会议预约请求参数
     */
    @Data
    public static class ReservationRequest {
        /** 会议室ID */
        private Long roomId;
        /** 会议主题 */
        private String meetingTitle;
        /** 开始时间 */
        private String startTime;
        /** 结束时间 */
        private String endTime;
        /** 会议说明 */
        private String description;
    }

    /**
     * 查询可用会议室列表
     * @return 可用会议室列表
     */
    @GetMapping("/rooms")
    public Result<List<MeetingRoom>> getRooms() {
        List<MeetingRoom> rooms = meetingRoomService.listAvailable();
        return Result.success(rooms);
    }

    /**
     * 查询我的会议预约
     * @return 我的会议预约列表
     */
    @GetMapping("/reservations/my")
    public Result<List<MeetingReservation>> getMyReservations() {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<List<MeetingReservation>>unauthorized("未登录");
        }
        List<MeetingReservation> list = meetingReservationService.getByEmpId(user.getEmpId());
        return Result.success(list);
    }

    /**
     * 创建会议预约
     * 先检查会议室在指定时间段是否可用
     * @param request 预约请求
     * @return 创建的会议预约
     */
    @PostMapping("/reservations")
    public Result<MeetingReservation> createReservation(@RequestBody ReservationRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null || user.getEmpId() == null) {
            return Result.<MeetingReservation>unauthorized("未登录");
        }
        
        java.time.LocalDateTime startTime = java.time.LocalDateTime.parse(request.getStartTime());
        java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(request.getEndTime());
        
        // 检查会议室可用性
        if (!meetingReservationService.checkAvailability(request.getRoomId(), startTime, endTime)) {
            return Result.<MeetingReservation>error(400, "该时间段会议室已被占用");
        }
        
        MeetingReservation reservation = new MeetingReservation();
        reservation.setRoomId(request.getRoomId());
        reservation.setEmpId(user.getEmpId());
        reservation.setMeetingTitle(request.getMeetingTitle());
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setDescription(request.getDescription());
        
        MeetingReservation created = meetingReservationService.create(reservation);
        return Result.success("会议预约成功", created);
    }

    /**
     * 取消会议预约
     * 只有预约创建者可以取消
     * @param id 预约ID
     * @return 操作结果
     */
    @DeleteMapping("/reservations/{id}")
    public Result<Void> cancelReservation(@PathVariable Long id) {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Void>unauthorized("未登录");
        }
        
        MeetingReservation reservation = meetingReservationService.getById(id);
        if (reservation == null) {
            return Result.<Void>notFound("预约不存在");
        }
        
        // 验证权限：只有预约者本人可以取消
        if (!reservation.getEmpId().equals(user.getEmpId())) {
            return Result.<Void>forbidden("无权取消该预约");
        }
        
        reservation.setStatus(0);
        meetingReservationService.updateById(reservation);
        return Result.<Void>success("预约已取消", null);
    }
}