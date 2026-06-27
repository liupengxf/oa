package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.ExpenseReportItem;

import java.util.List;

public interface ExpenseReportItemMapper extends BaseMapper<ExpenseReportItem> {

    List<ExpenseReportItem> selectByReportId(Long reportId);
}