package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 费用报销实体
 * 记录员工费用报销信息
 */
@Data
@TableName("expense_report")
public class ExpenseReport {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 报销单号 */
    @TableField("report_no")
    private String reportNo;

    /** 员工ID */
    @TableField("emp_id")
    private Long empId;

    /** 总金额 */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /** 费用类型：差旅费、办公用品费、招待费等 */
    @TableField("expense_type")
    private String expenseType;

    /** 发票附件URL */
    @TableField("invoice_url")
    private String invoiceUrl;

    /** 报销说明 */
    @TableField("description")
    private String description;

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