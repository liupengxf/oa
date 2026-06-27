package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 考勤打卡记录实体
 * 记录员工每日考勤打卡信息
 */
@Data
@TableName("attendance_checkin")
public class AttendanceCheckin {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 员工ID */
    @TableField("emp_id")
    private Long empId;

    /** 打卡日期 */
    @TableField("check_date")
    private LocalDate checkDate;

    /** 上班打卡时间 */
    @TableField("check_in_time")
    private LocalDateTime checkInTime;

    /** 下班打卡时间 */
    @TableField("check_out_time")
    private LocalDateTime checkOutTime;

    /** 打卡状态：1-正常，2-迟到 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}