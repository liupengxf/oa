package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.MeetingReservation;

import java.time.LocalDateTime;
import java.util.List;

public interface MeetingReservationMapper extends BaseMapper<MeetingReservation> {

    List<MeetingReservation> selectOverlappingReservations(Long roomId, LocalDateTime startTime, LocalDateTime endTime);
}