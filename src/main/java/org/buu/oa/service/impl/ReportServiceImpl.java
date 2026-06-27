package org.buu.oa.service.impl;

import org.buu.oa.mapper.ReportMapper;
import org.buu.oa.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;

    public ReportServiceImpl(ReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }

    @Override
    public List<Map<String, Object>> getDeptEmpCount() {
        return reportMapper.selectDeptEmpCount();
    }

    @Override
    public List<Map<String, Object>> getLeaveTypeStats() {
        return reportMapper.selectLeaveTypeStats();
    }

    @Override
    public List<Map<String, Object>> getExpenseTrend() {
        return reportMapper.selectExpenseTrend();
    }

    @Override
    public Map<String, Object> getAttendanceAbnormalRate(LocalDate startDate, LocalDate endDate) {
        return reportMapper.selectAttendanceAbnormalRate(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getEmployeeList() {
        return reportMapper.selectEmployeeList();
    }

    @Override
    public List<Map<String, Object>> getAttendanceRecords(LocalDate startDate, LocalDate endDate) {
        return reportMapper.selectAttendanceRecords(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getExpenseReportList() {
        return reportMapper.selectExpenseReportList();
    }
}