package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.ApprovalRecord;
import org.buu.oa.entity.OvertimeApplication;
import org.buu.oa.mapper.ApprovalRecordMapper;
import org.buu.oa.mapper.OvertimeApplicationMapper;
import org.buu.oa.service.OvertimeApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 加班申请服务实现类
 * 实现加班申请创建、审批等加班相关功能
 */
@Service
public class OvertimeApplicationServiceImpl extends ServiceImpl<OvertimeApplicationMapper, OvertimeApplication> implements OvertimeApplicationService {

    private final ApprovalRecordMapper approvalRecordMapper;
    /** 序列号生成器，用于生成加班单号 */
    private final AtomicLong sequence = new AtomicLong(1);

    public OvertimeApplicationServiceImpl(ApprovalRecordMapper approvalRecordMapper) {
        this.approvalRecordMapper = approvalRecordMapper;
    }

    /**
     * 创建加班申请
     * 生成唯一的加班单号，设置状态为待审批
     * @param application 加班申请实体
     * @return 创建后的加班申请
     */
    @Override
    @Transactional
    public OvertimeApplication create(OvertimeApplication application) {
        // 生成加班单号：JB + 年月 + 3位序列号
        String no = "JB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + 
                     String.format("%03d", sequence.getAndIncrement());
        application.setOvertimeNo(no);
        application.setStatus("PENDING");  // 设置为待审批状态
        baseMapper.insert(application);
        return application;
    }

    /**
     * 审批加班申请
     * 更新申请状态，并记录审批记录
     * @param id 申请ID
     * @param approverId 审批人ID
     * @param result 审批结果（1-通过，2-驳回）
     * @param opinion 审批意见
     */
    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer result, String opinion) {
        OvertimeApplication application = baseMapper.selectById(id);
        if (application == null) {
            return;
        }

        // 更新申请状态
        if (result == 1) {
            application.setStatus("COMPLETED");  // 通过
        } else {
            application.setStatus("REJECTED");   // 驳回
        }
        baseMapper.updateById(application);

        // 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessType("OVERTIME");
        record.setBusinessId(id);
        record.setApproverId(approverId);
        record.setApprovalResult(result);
        record.setApprovalOpinion(opinion);
        record.setApprovalTime(LocalDateTime.now());
        approvalRecordMapper.insert(record);
    }

    /**
     * 查询部门待审批列表
     * @param deptId 部门ID
     * @return 待审批列表
     */
    @Override
    public List<OvertimeApplication> getPendingByDeptId(Long deptId) {
        return baseMapper.selectPendingByDeptId(deptId);
    }

    /**
     * 查询员工的加班记录
     * 按创建时间倒序排列
     * @param empId 员工ID
     * @return 加班记录列表
     */
    @Override
    public List<OvertimeApplication> getByEmpId(Long empId) {
        LambdaQueryWrapper<OvertimeApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OvertimeApplication::getEmpId, empId);
        wrapper.orderByDesc(OvertimeApplication::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}