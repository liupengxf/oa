package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.MeetingRoom;

import java.util.List;

/**
 * 会议室服务接口
 * 提供会议室查询等会议室管理相关功能
 */
public interface MeetingRoomService extends IService<MeetingRoom> {

    /**
     * 查询可用的会议室列表
     * @return 会议室列表
     */
    List<MeetingRoom> listAvailable();
}