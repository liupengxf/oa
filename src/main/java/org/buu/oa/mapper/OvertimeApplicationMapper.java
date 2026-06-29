package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.OvertimeApplication;

import java.util.List;
import java.util.Map;

public interface OvertimeApplicationMapper extends BaseMapper<OvertimeApplication> {

    List<OvertimeApplication> selectPendingByDeptId(Long deptId);

    List<Map<String, Object>> selectAllWithEmp();
}