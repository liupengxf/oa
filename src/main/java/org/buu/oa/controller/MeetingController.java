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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
     * 会议室请求参数
     */
    @Data
    public static class RoomRequest {
        private Long id;
        private String roomName;
        private String roomCode;
        private Integer capacity;
        private String location;
        private Integer status;
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

    // ==================== 会议室管理接口 ====================

    /**
     * 查询所有会议室（不分状态）
     * @return 会议室列表
     */
    @GetMapping("/rooms/all")
    public Result<List<MeetingRoom>> getAllRooms() {
        List<MeetingRoom> rooms = meetingRoomService.list();
        return Result.success(rooms);
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
     * 根据ID查询会议室
     * @param id 会议室ID
     * @return 会议室信息
     */
    @GetMapping("/rooms/{id}")
    public Result<MeetingRoom> getRoom(@PathVariable Long id) {
        MeetingRoom room = meetingRoomService.getById(id);
        if (room == null) {
            return Result.<MeetingRoom>notFound("会议室不存在");
        }
        return Result.success(room);
    }

    /**
     * 创建会议室
     * @param request 会议室信息
     * @return 创建的会议室
     */
    @PostMapping("/rooms")
    public Result<MeetingRoom> createRoom(@RequestBody RoomRequest request) {
        MeetingRoom room = new MeetingRoom();
        room.setRoomName(request.getRoomName());
        room.setRoomCode(request.getRoomCode());
        room.setCapacity(request.getCapacity());
        room.setLocation(request.getLocation());
        room.setStatus(1);
        meetingRoomService.save(room);
        return Result.success("会议室创建成功", room);
    }

    /**
     * 更新会议室
     * @param id 会议室ID
     * @param request 会议室信息
     * @return 更新结果
     */
    @PutMapping("/rooms/{id}")
    public Result<Void> updateRoom(@PathVariable Long id, @RequestBody RoomRequest request) {
        MeetingRoom room = meetingRoomService.getById(id);
        if (room == null) {
            return Result.<Void>notFound("会议室不存在");
        }
        room.setRoomName(request.getRoomName());
        room.setRoomCode(request.getRoomCode());
        room.setCapacity(request.getCapacity());
        room.setLocation(request.getLocation());
        if (request.getStatus() != null) {
            room.setStatus(request.getStatus());
        }
        meetingRoomService.updateById(room);
        return Result.<Void>success("会议室更新成功", null);
    }

    /**
     * 删除会议室
     * @param id 会议室ID
     * @return 删除结果
     */
    @DeleteMapping("/rooms/{id}")
    public Result<Void> deleteRoom(@PathVariable Long id) {
        MeetingRoom room = meetingRoomService.getById(id);
        if (room == null) {
            return Result.<Void>notFound("会议室不存在");
        }
        room.setStatus(0);
        meetingRoomService.updateById(room);
        return Result.<Void>success("会议室已删除", null);
    }

    // ==================== 会议室预约接口 ====================

    /**
     * 查询我的会议预约
     * @return 我的会议预约列表
     */
    @GetMapping("/reservations/my")
    public Result<List<MeetingReservation>> getMyReservations() {
        SysUser user = authService.getCurrentUser();
        Long empId = (user != null && user.getEmpId() != null) ? user.getEmpId() : DEFAULT_EMP_ID;
        List<MeetingReservation> list = meetingReservationService.getByEmpId(empId);
        return Result.success(list);
    }

    /**
     * 查询会议室的预约记录
     * @param roomId 会议室ID
     * @return 预约记录列表
     */
    @GetMapping("/reservations/room/{roomId}")
    public Result<List<MeetingReservation>> getRoomReservations(@PathVariable Long roomId) {
        List<MeetingReservation> list = meetingReservationService.getByRoomId(roomId);
        return Result.success(list);
    }

    /**
     * 日历视图查询
     * 查询指定日期范围内所有会议室的预约情况
     * @param roomId 会议室ID（可选，为空则查询所有会议室）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日历视图数据
     */
    @GetMapping("/calendar")
    public Result<Map<String, Object>> getCalendarView(
            @RequestParam(required = false) Long roomId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        LocalDateTime startDateTime = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime endDateTime = LocalDateTime.parse(endDate + "T23:59:59");
        
        List<MeetingReservation> reservations = meetingReservationService.getByDateRange(roomId, startDateTime, endDateTime);
        List<MeetingRoom> rooms = meetingRoomService.listAvailable();
        
        return Result.success(Map.of(
            "reservations", reservations,
            "rooms", rooms
        ));
    }

    private static final Long DEFAULT_EMP_ID = 1L;

    /**
     * 创建会议预约
     * 先检查会议室在指定时间段是否可用
     * @param request 预约请求
     * @return 创建的会议预约
     */
    @PostMapping("/reservations")
    public Result<MeetingReservation> createReservation(@RequestBody ReservationRequest request) {
        SysUser user = authService.getCurrentUser();
        Long empId = (user != null && user.getEmpId() != null) ? user.getEmpId() : DEFAULT_EMP_ID;
        
        LocalDateTime startTime = LocalDateTime.parse(request.getStartTime());
        LocalDateTime endTime = LocalDateTime.parse(request.getEndTime());
        
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            return Result.<MeetingReservation>error(400, "结束时间必须晚于开始时间");
        }
        
        if (!meetingReservationService.checkAvailability(request.getRoomId(), startTime, endTime)) {
            return Result.<MeetingReservation>error(400, "该时间段会议室已被占用");
        }
        
        MeetingReservation reservation = new MeetingReservation();
        reservation.setRoomId(request.getRoomId());
        reservation.setEmpId(empId);
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
        
        if (!reservation.getEmpId().equals(user.getEmpId())) {
            return Result.<Void>forbidden("无权取消该预约");
        }
        
        reservation.setStatus(0);
        meetingReservationService.updateById(reservation);
        return Result.<Void>success("预约已取消", null);
    }
}