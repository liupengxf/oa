package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.LeaveApplication;

import java.util.List;

public interface LeaveApplicationMapper extends BaseMapper<LeaveApplication> {

    List<LeaveApplication> selectPendingByDeptId(Long deptId);
}