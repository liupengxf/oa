package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.ExpenseReport;

import java.util.List;

public interface ExpenseReportMapper extends BaseMapper<ExpenseReport> {

    List<ExpenseReport> selectPendingByDeptId(Long deptId);
}