package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("approval_record")
public class ApprovalRecord {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("business_type")
    private String businessType;

    @TableField("business_id")
    private Long businessId;

    @TableField("approver_id")
    private Long approverId;

    @TableField("approval_result")
    private Integer approvalResult;

    @TableField("approval_opinion")
    private String approvalOpinion;

    @TableField("approval_time")
    private LocalDateTime approvalTime;

    @TableField("create_time")
    private LocalDateTime createTime;
}