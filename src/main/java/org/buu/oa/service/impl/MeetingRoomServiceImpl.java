package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.MeetingRoom;
import org.buu.oa.mapper.MeetingRoomMapper;
import org.buu.oa.service.MeetingRoomService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会议室服务实现类
 * 实现会议室查询等会议室管理相关功能
 */
@Service
public class MeetingRoomServiceImpl extends ServiceImpl<MeetingRoomMapper, MeetingRoom> implements MeetingRoomService {

    /**
     * 查询可用的会议室列表
     * 只查询状态为有效的会议室，按ID正序排列
     * @return 会议室列表
     */
    @Override
    public List<MeetingRoom> listAvailable() {
        LambdaQueryWrapper<MeetingRoom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MeetingRoom::getStatus, 1);  // 只查询有效状态的会议室
        wrapper.orderByAsc(MeetingRoom::getId); // 按ID正序排列
        return baseMapper.selectList(wrapper);
    }
}