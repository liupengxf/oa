package org.buu.oa.service;

import org.buu.oa.mapper.ReportMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {

    List<Map<String, Object>> getDeptEmpCount();

    List<Map<String, Object>> getLeaveTypeStats();

    List<Map<String, Object>> getExpenseTrend();

    Map<String, Object> getAttendanceAbnormalRate(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getEmployeeList();

    List<Map<String, Object>> getAttendanceRecords(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getExpenseReportList();
}