package org.buu.oa.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {

    @Select("SELECT d.dept_name as deptName, COUNT(e.id) as empCount " +
            "FROM sys_dept d " +
            "LEFT JOIN emp_employee e ON d.id = e.dept_id AND e.status = 1 " +
            "GROUP BY d.id, d.dept_name " +
            "ORDER BY d.sort")
    List<Map<String, Object>> selectDeptEmpCount();

    @Select("SELECT CASE l.leave_type " +
            "WHEN 1 THEN '事假' WHEN 2 THEN '病假' WHEN 3 THEN '年假' ELSE '其他' END as leaveTypeName, " +
            "COUNT(l.id) as count " +
            "FROM leave_application l " +
            "GROUP BY l.leave_type")
    List<Map<String, Object>> selectLeaveTypeStats();

    @Select("SELECT DATE_FORMAT(r.create_time, '%Y-%m') as month, SUM(r.total_amount) as totalAmount " +
            "FROM expense_report r " +
            "GROUP BY DATE_FORMAT(r.create_time, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> selectExpenseTrend();

    @Select("SELECT COUNT(CASE WHEN a.status IN (2, 3) THEN 1 END) as abnormalCount, COUNT(a.id) as totalCount " +
            "FROM attendance_checkin a " +
            "WHERE a.check_date >= #{startDate} AND a.check_date <= #{endDate}")
    Map<String, Object> selectAttendanceAbnormalRate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT e.emp_no as empNo, e.name, d.dept_name as deptName, e.position, " +
            "CASE e.gender WHEN 1 THEN '男' ELSE '女' END as gender, " +
            "e.phone, e.email, e.entry_date as entryDate, " +
            "CASE e.status WHEN 1 THEN '在职' ELSE '离职' END as statusText " +
            "FROM emp_employee e " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "ORDER BY d.sort, e.id")
    List<Map<String, Object>> selectEmployeeList();

    @Select("SELECT a.check_date as checkDate, e.name as empName, d.dept_name as deptName, " +
            "a.check_in_time as checkInTime, a.check_out_time as checkOutTime, " +
            "CASE a.status WHEN 1 THEN '正常' WHEN 2 THEN '迟到' WHEN 3 THEN '缺卡' ELSE '病假' END as statusText " +
            "FROM attendance_checkin a " +
            "LEFT JOIN emp_employee e ON a.emp_id = e.id " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "WHERE a.check_date >= #{startDate} AND a.check_date <= #{endDate} " +
            "ORDER BY a.check_date DESC, e.id")
    List<Map<String, Object>> selectAttendanceRecords(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT r.report_no as reportNo, e.name as empName, d.dept_name as deptName, " +
            "r.total_amount as totalAmount, r.expense_type as expenseType, r.description, " +
            "CASE r.status WHEN 'PENDING' THEN '待审批' WHEN 'COMPLETED' THEN '已通过' ELSE '已驳回' END as statusText, " +
            "r.create_time as createTime " +
            "FROM expense_report r " +
            "LEFT JOIN emp_employee e ON r.emp_id = e.id " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "ORDER BY r.create_time DESC")
    List<Map<String, Object>> selectExpenseReportList();

    @Select("SELECT e.name as empName, d.dept_name as deptName, " +
            "CASE l.leave_type WHEN 1 THEN '事假' WHEN 2 THEN '病假' WHEN 3 THEN '年假' ELSE '其他' END as leaveTypeName, " +
            "l.start_date as startDate, l.end_date as endDate, l.reason, " +
            "CASE l.status WHEN 'PENDING' THEN '待审批' WHEN 'COMPLETED' THEN '已通过' WHEN 'REJECTED' THEN '已驳回' ELSE '未知' END as statusText " +
            "FROM leave_application l " +
            "LEFT JOIN emp_employee e ON l.emp_id = e.id " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "ORDER BY l.create_time DESC")
    List<Map<String, Object>> selectLeaveAll();

    @Select("SELECT r.report_no as reportNo, e.name as empName, d.dept_name as deptName, " +
            "r.total_amount as totalAmount, r.expense_type as expenseType, r.description, " +
            "CASE r.status WHEN 'PENDING' THEN '待审批' WHEN 'COMPLETED' THEN '已通过' ELSE '已驳回' END as statusText, " +
            "r.create_time as createTime " +
            "FROM expense_report r " +
            "LEFT JOIN emp_employee e ON r.emp_id = e.id " +
            "LEFT JOIN sys_dept d ON e.dept_id = d.id " +
            "ORDER BY r.create_time DESC")
    List<Map<String, Object>> selectExpenseAll();
}