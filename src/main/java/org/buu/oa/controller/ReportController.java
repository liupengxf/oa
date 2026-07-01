package org.buu.oa.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
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
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String fileName = URLEncoder.encode("员工台账_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"; filename*=UTF-8''" + fileName + ".xlsx");
        
        java.util.ArrayList<List<String>> rows = new java.util.ArrayList<>();
        for (Map<String, Object> row : data) {
            rows.add(java.util.List.of(
                    String.valueOf(row.get("empNo")),
                    String.valueOf(row.get("name")),
                    String.valueOf(row.get("deptName")),
                    String.valueOf(row.get("position")),
                    String.valueOf(row.get("gender")),
                    String.valueOf(row.get("phone")),
                    String.valueOf(row.get("email")),
                    String.valueOf(row.get("entryDate")),
                    String.valueOf(row.get("statusText"))
            ));
        }
        
        EasyExcel.write(response.getOutputStream())
                .head(List.of(
                        List.of("员工编号"), List.of("姓名"), List.of("部门"), List.of("岗位"), 
                        List.of("性别"), List.of("手机号"), List.of("邮箱"), List.of("入职日期"), List.of("状态")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("员工台账")
                .doWrite(rows);
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
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String fileName = URLEncoder.encode("考勤报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"; filename*=UTF-8''" + fileName + ".xlsx");
        
        java.util.ArrayList<List<String>> rows = new java.util.ArrayList<>();
        for (Map<String, Object> row : data) {
            rows.add(java.util.List.of(
                    String.valueOf(row.get("checkDate")),
                    String.valueOf(row.get("empName")),
                    String.valueOf(row.get("deptName")),
                    String.valueOf(row.get("checkInTime")),
                    String.valueOf(row.get("checkOutTime")),
                    String.valueOf(row.get("statusText"))
            ));
        }
        
        EasyExcel.write(response.getOutputStream())
                .head(List.of(
                        List.of("打卡日期"), List.of("员工姓名"), List.of("部门"), 
                        List.of("上班时间"), List.of("下班时间"), List.of("状态")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("考勤报表")
                .doWrite(rows);
    }

    @GetMapping("/export/expense")
    public void exportExpense(HttpServletResponse response) throws IOException {
        List<Map<String, Object>> data = reportService.getExpenseReportList();
        
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String fileName = URLEncoder.encode("报销明细_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"; filename*=UTF-8''" + fileName + ".xlsx");
        
        java.util.ArrayList<List<String>> rows = new java.util.ArrayList<>();
        for (Map<String, Object> row : data) {
            rows.add(java.util.List.of(
                    String.valueOf(row.get("reportNo")),
                    String.valueOf(row.get("empName")),
                    String.valueOf(row.get("deptName")),
                    String.valueOf(row.get("totalAmount")),
                    String.valueOf(row.get("expenseType")),
                    String.valueOf(row.get("description")),
                    String.valueOf(row.get("statusText")),
                    String.valueOf(row.get("createTime"))
            ));
        }
        
        EasyExcel.write(response.getOutputStream())
                .head(List.of(
                        List.of("报销单号"), List.of("员工姓名"), List.of("部门"), List.of("报销金额"), 
                        List.of("费用类型"), List.of("报销说明"), List.of("状态"), List.of("提交时间")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet("报销明细")
                .doWrite(rows);
    }

    @GetMapping("/export/comprehensive")
    public void exportComprehensiveReport(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        String fileName = URLEncoder.encode("综合报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + ".xlsx\"; filename*=UTF-8''" + fileName + ".xlsx");
        
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
        
        WriteSheet deptSheet = EasyExcel.writerSheet(0, "部门人数")
                .head(List.of(List.of("部门名称"), List.of("员工人数")))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build();
        List<Map<String, Object>> deptData = reportService.getDeptEmpCount();
        java.util.ArrayList<List<String>> deptRows = new java.util.ArrayList<>();
        for (Map<String, Object> row : deptData) {
            deptRows.add(java.util.List.of(
                    String.valueOf(row.get("deptName")),
                    String.valueOf(row.get("empCount"))
            ));
        }
        excelWriter.write(deptRows, deptSheet);
        
        WriteSheet leaveSheet = EasyExcel.writerSheet(1, "请假数据")
                .head(List.of(
                        List.of("员工姓名"), List.of("部门"), List.of("请假类型"), 
                        List.of("开始日期"), List.of("结束日期"), List.of("请假原因"), List.of("状态")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build();
        List<Map<String, Object>> leaveData = reportService.getLeaveList();
        java.util.ArrayList<List<String>> leaveRows = new java.util.ArrayList<>();
        for (Map<String, Object> row : leaveData) {
            leaveRows.add(java.util.List.of(
                    String.valueOf(row.get("empName")),
                    String.valueOf(row.get("deptName")),
                    String.valueOf(row.get("leaveTypeName")),
                    String.valueOf(row.get("startDate")),
                    String.valueOf(row.get("endDate")),
                    String.valueOf(row.get("reason")),
                    String.valueOf(row.get("statusText"))
            ));
        }
        excelWriter.write(leaveRows, leaveSheet);
        
        WriteSheet expenseSheet = EasyExcel.writerSheet(2, "报销数据")
                .head(List.of(
                        List.of("报销单号"), List.of("员工姓名"), List.of("部门"), List.of("报销金额"), 
                        List.of("费用类型"), List.of("报销说明"), List.of("状态"), List.of("提交时间")
                ))
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .build();
        List<Map<String, Object>> expenseData = reportService.getExpenseList();
        java.util.ArrayList<List<String>> expenseRows = new java.util.ArrayList<>();
        for (Map<String, Object> row : expenseData) {
            expenseRows.add(java.util.List.of(
                    String.valueOf(row.get("reportNo")),
                    String.valueOf(row.get("empName")),
                    String.valueOf(row.get("deptName")),
                    String.valueOf(row.get("totalAmount")),
                    String.valueOf(row.get("expenseType")),
                    String.valueOf(row.get("description")),
                    String.valueOf(row.get("statusText")),
                    String.valueOf(row.get("createTime"))
            ));
        }
        excelWriter.write(expenseRows, expenseSheet);
        
        excelWriter.finish();
    }
}