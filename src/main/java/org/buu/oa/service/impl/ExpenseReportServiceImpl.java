package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.ApprovalRecord;
import org.buu.oa.entity.ExpenseReport;
import org.buu.oa.mapper.ApprovalRecordMapper;
import org.buu.oa.mapper.ExpenseReportMapper;
import org.buu.oa.service.ExpenseReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 费用报销服务实现类
 * 实现费用报销创建、审批等报销相关功能
 */
@Service
public class ExpenseReportServiceImpl extends ServiceImpl<ExpenseReportMapper, ExpenseReport> implements ExpenseReportService {

    private final ApprovalRecordMapper approvalRecordMapper;
    /** 序列号生成器，用于生成报销单号 */
    private final AtomicLong sequence = new AtomicLong(1);

    public ExpenseReportServiceImpl(ApprovalRecordMapper approvalRecordMapper) {
        this.approvalRecordMapper = approvalRecordMapper;
    }

    /**
     * 创建费用报销
     * 生成唯一的报销单号，设置状态为待审批
     * @param report 报销实体
     * @return 创建后的报销
     */
    @Override
    @Transactional
    public ExpenseReport create(ExpenseReport report) {
        // 生成报销单号：BX + 年月日 + 3位序列号
        String no = "BX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + 
                     String.format("%03d", sequence.getAndIncrement());
        report.setReportNo(no);
        report.setStatus("PENDING");  // 设置为待审批状态
        baseMapper.insert(report);
        return report;
    }

    /**
     * 审批费用报销
     * 更新报销状态，并记录审批记录
     * @param id 报销ID
     * @param approverId 审批人ID
     * @param result 审批结果（1-通过，2-驳回）
     * @param opinion 审批意见
     */
    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer result, String opinion) {
        ExpenseReport report = baseMapper.selectById(id);
        if (report == null) {
            return;
        }

        // 更新报销状态
        if (result == 1) {
            report.setStatus("COMPLETED");  // 通过
        } else {
            report.setStatus("REJECTED");   // 驳回
        }
        baseMapper.updateById(report);

        // 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessType("EXPENSE");
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
    public List<ExpenseReport> getPendingByDeptId(Long deptId) {
        return baseMapper.selectPendingByDeptId(deptId);
    }

    /**
     * 查询员工的报销记录
     * 按创建时间倒序排列
     * @param empId 员工ID
     * @return 报销记录列表
     */
    @Override
    public List<ExpenseReport> getByEmpId(Long empId) {
        LambdaQueryWrapper<ExpenseReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseReport::getEmpId, empId);
        wrapper.orderByDesc(ExpenseReport::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}