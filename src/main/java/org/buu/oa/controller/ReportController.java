package org.buu.oa.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.buu.oa.common.Result;
import org.buu.oa.service.ReportService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dept-emp-count")
    public Result<List<Map<String, Object>>> getDeptEmpCount() {
        return Result.success(reportService.getDeptEmpCount());
    }

    @GetMapping("/leave-type-stats")
    public Result<List<Map<String, Object>>> getLeaveTypeStats() {
        return Result.success(reportService.getLeaveTypeStats());
    }

    @GetMapping("/expense-trend")
    public Result<List<Map<String, Object>>> getExpenseTrend() {
        return Result.success(reportService.getExpenseTrend());
    }

    @GetMapping("/attendance-abnormal-rate")
    public Result<Map<String, Object>> getAttendanceAbnormalRate(
            @RequestParam(defaultValue = "current") String period) {
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
        
        if ("current".equals(period)) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
        } else {
            startDate = endDate.minusMonths(6);
        }
        
        Map<String, Object> result = reportService.getAttendanceAbnormalRate(startDate, endDate);
        Long abnormalCount = result.get("abnormalCount") != null ? ((Number) result.get("abnormalCount")).longValue() : 0L;
        Long totalCount = result.get("totalCount") != null ? ((Number) result.get("totalCount")).longValue() : 0L;
        
        Map<String, Object> rateResult = new HashMap<>();
        rateResult.put("abnormalCount", abnormalCount);
        rateResult.put("totalCount", totalCount);
        rateResult.put("abnormalRate", totalCount > 0 ? (double) abnormalCount / totalCount * 100 : 0);
        
        return Result.success(rateResult);
    }

    @GetMapping("/export/employee")
    public void exportEmployee(HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = reportService.getEmployeeList();
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("员工台账_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream())
                .head(List.of(List.of("员工编号", "姓名", "部门", "岗位", "性别", "手机号", "邮箱", "入职日期", "状态")))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("员工台账")
                .doWrite(data.stream().map(row -> List.of(
                        row.get("empNo"),
                        row.get("name"),
                        row.get("deptName"),
                        row.get("position"),
                        row.get("gender"),
                        row.get("phone"),
                        row.get("email"),
                        row.get("entryDate"),
                        row.get("statusText")
                )).toList());
    }

    @GetMapping("/export/attendance")
    public void exportAttendance(HttpServletResponse response,
            @RequestParam(defaultValue = "current") String period) throws IOException {
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
        
        if ("current".equals(period)) {
            YearMonth currentMonth = YearMonth.now();
            startDate = currentMonth.atDay(1);
        } else {
            startDate = endDate.minusMonths(6);
        }
        
        List<Map<String, Object>> data = reportService.getAttendanceRecords(startDate, endDate);
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("考勤报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream())
                .head(List.of(List.of("打卡日期", "员工姓名", "部门", "上班时间", "下班时间", "状态")))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("考勤报表")
                .doWrite(data.stream().map(row -> List.of(
                        row.get("checkDate"),
                        row.get("empName"),
                        row.get("deptName"),
                        row.get("checkInTime"),
                        row.get("checkOutTime"),
                        row.get("statusText")
                )).toList());
    }

    @GetMapping("/export/expense")
    public void exportExpense(HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = reportService.getExpenseReportList();
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("报销明细_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        EasyExcel.write(response.getOutputStream())
                .head(List.of(List.of("报销单号", "员工姓名", "部门", "报销金额", "费用类型", "报销说明", "状态", "提交时间")))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("报销明细")
                .doWrite(data.stream().map(row -> List.of(
                        row.get("reportNo"),
                        row.get("empName"),
                        row.get("deptName"),
                        row.get("totalAmount"),
                        row.get("expenseType"),
                        row.get("description"),
                        row.get("statusText"),
                        row.get("createTime")
                )).toList());
    }
}