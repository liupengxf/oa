package org.buu.oa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.buu.oa.entity.ApprovalRecord;

import java.util.List;

public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    List<ApprovalRecord> selectByBusinessTypeAndId(String businessType, Long businessId);
}