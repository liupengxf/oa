package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 加班申请实体
 * 记录员工加班申请信息
 */
@Data
@TableName("overtime_application")
public class OvertimeApplication {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 加班单号 */
    @TableField("overtime_no")
    private String overtimeNo;

    /** 员工ID */
    @TableField("emp_id")
    private Long empId;

    /** 加班类型：1-工作日加班，2-周末加班，3-节假日加班 */
    @TableField("overtime_type")
    private Integer overtimeType;

    /** 开始时间 */
    @TableField("start_time")
    private LocalDateTime startTime;

    /** 结束时间 */
    @TableField("end_time")
    private LocalDateTime endTime;

    /** 加班时长（小时） */
    @TableField("hours")
    private BigDecimal hours;

    /** 加班原因 */
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