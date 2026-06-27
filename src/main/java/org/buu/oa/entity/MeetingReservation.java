package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议室预约实体
 * 记录会议室预约信息
 */
@Data
@TableName("meeting_reservation")
public class MeetingReservation {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 预约单号 */
    @TableField("reservation_no")
    private String reservationNo;

    /** 会议室ID */
    @TableField("room_id")
    private Long roomId;

    /** 预约人ID */
    @TableField("emp_id")
    private Long empId;

    /** 会议主题 */
    @TableField("meeting_title")
    private String meetingTitle;

    /** 开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;

    /** 结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;

    /** 会议说明 */
    @TableField("description")
    private String description;

    /** 提醒状态：0-未提醒，1-已提醒 */
    @TableField("remind_status")
    private Integer remindStatus;

    /** 状态：1-有效，0-取消 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}