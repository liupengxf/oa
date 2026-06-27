package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 请假申请实体
 * 记录员工请假申请信息
 */
@Data
@TableName("leave_application")
public class LeaveApplication {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 请假单号 */
    @TableField("leave_no")
    private String leaveNo;

    /** 员工ID */
    @TableField("emp_id")
    private Long empId;

    /** 请假类型：1-事假，2-病假，3-年假，4-其他 */
    @TableField("leave_type")
    private Integer leaveType;

    /** 开始日期 */
    @TableField("start_date")
    private LocalDate startDate;

    /** 结束日期 */
    @TableField("end_date")
    private LocalDate endDate;

    /** 请假天数 */
    @TableField("days")
    private BigDecimal days;

    /** 请假原因 */
    @TableField("reason")
    private String reason;

    /** 状态：PENDING-待审批，COMPLETED-已通过，REJECTED-已驳回 */
    @TableField("status")
    private String status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}