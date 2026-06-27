package org.buu.oa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会议室实体
 * 记录会议室基本信息
 */
@Data
@TableName("meeting_room")
public class MeetingRoom {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 会议室名称 */
    @TableField("room_name")
    private String roomName;

    /** 会议室编码 */
    @TableField("room_code")
    private String roomCode;

    /** 容纳人数 */
    @TableField("capacity")
    private Integer capacity;

    /** 位置 */
    @TableField("location")
    private String location;

    /** 状态：1-有效，0-无效 */
    @TableField("status")
    private Integer status;

    /** 创建时间 */
    @TableField("create_time")
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField("update_time")
    private LocalDateTime updateTime;
}