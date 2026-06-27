package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知阅读记录实体
 * 记录用户阅读通知的时间
 */
@Data
@TableName("sys_notice_read")
public class SysNoticeRead {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 通知ID */
    @TableField("notice_id")
    private Long noticeId;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 阅读时间 */
    @TableField("read_time")
    private LocalDateTime readTime;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;
}