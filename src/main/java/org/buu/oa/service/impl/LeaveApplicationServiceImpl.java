package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.ApprovalRecord;
import org.buu.oa.entity.LeaveApplication;
import org.buu.oa.mapper.ApprovalRecordMapper;
import org.buu.oa.mapper.LeaveApplicationMapper;
import org.buu.oa.service.AttendanceCheckinService;
import org.buu.oa.service.LeaveApplicationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请假申请服务实现类
 * 实现请假申请创建、审批等请假相关功能
 */
@Service
public class LeaveApplicationServiceImpl extends ServiceImpl<LeaveApplicationMapper, LeaveApplication> implements LeaveApplicationService {

    private final ApprovalRecordMapper approvalRecordMapper;
    private final AttendanceCheckinService attendanceCheckinService;
    /** 序列号生成器，用于生成请假单号 */
    private final AtomicLong sequence = new AtomicLong(1);

    public LeaveApplicationServiceImpl(ApprovalRecordMapper approvalRecordMapper, AttendanceCheckinService attendanceCheckinService) {
        this.approvalRecordMapper = approvalRecordMapper;
        this.attendanceCheckinService = attendanceCheckinService;
    }

    /**
     * 创建请假申请
     * 生成唯一的请假单号，设置状态为待审批
     * @param application 请假申请实体
     * @return 创建后的请假申请
     */
    @Override
    @Transactional
    public LeaveApplication create(LeaveApplication application) {
        // 生成请假单号：QJ + 年月 + 3位序列号
        String no = "QJ" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM")) + 
                     String.format("%03d", sequence.getAndIncrement());
        application.setLeaveNo(no);
        application.setStatus("PENDING");  // 设置为待审批状态
        baseMapper.insert(application);
        return application;
    }

    /**
     * 审批请假申请
     * 更新申请状态，并记录审批记录
     * @param id 申请ID
     * @param approverId 审批人ID
     * @param result 审批结果（1-通过，2-驳回）
     * @param opinion 审批意见
     */
    @Override
    @Transactional
    public void approve(Long id, Long approverId, Integer result, String opinion) {
        LeaveApplication application = baseMapper.selectById(id);
        if (application == null) {
            return;
        }

        // 更新申请状态
        if (result == 1) {
            application.setStatus("COMPLETED");  // 通过
            
            // 审批通过且为病假时，更新考勤记录
            if (application.getLeaveType() != null && application.getLeaveType() == 2) {
                attendanceCheckinService.markLeaveDates(
                    application.getEmpId(), 
                    application.getStartDate(), 
                    application.getEndDate(), 
                    4  // status=4 病假
                );
            }
        } else {
            application.setStatus("REJECTED");   // 驳回
        }
        baseMapper.updateById(application);

        // 创建审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setBusinessType("LEAVE");
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
    public List<LeaveApplication> getPendingByDeptId(Long deptId) {
        return baseMapper.selectPendingByDeptId(deptId);
    }

    /**
     * 查询员工的请假记录
     * 按创建时间倒序排列
     * @param empId 员工ID
     * @return 请假记录列表
     */
    @Override
    public List<LeaveApplication> getByEmpId(Long empId) {
        LambdaQueryWrapper<LeaveApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaveApplication::getEmpId, empId);
        wrapper.orderByDesc(LeaveApplication::getCreateTime);
        return baseMapper.selectList(wrapper);
    }
}