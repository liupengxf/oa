package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.AttendanceCheckin;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceCheckinMapper extends BaseMapper<AttendanceCheckin> {

    List<AttendanceCheckin> selectByEmpIdAndDateRange(Long empId, LocalDate startDate, LocalDate endDate);
}