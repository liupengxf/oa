package org.buu.oa.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import org.buu.oa.common.Result;
import org.buu.oa.service.ReportService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
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

    @GetMapping("/test/leave-list")
    public Result<List<Map<String, Object>>> testLeaveList() {
        List<Map<String, Object>> data = reportService.getLeaveList();
        return Result.success(data);
    }

    @GetMapping("/test/expense-list")
    public Result<List<Map<String, Object>>> testExpenseList() {
        List<Map<String, Object>> data = reportService.getExpenseList();
        return Result.success(data);
    }

    @GetMapping("/export/comprehensive")
    public void exportComprehensiveReport(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("综合报表_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), StandardCharsets.UTF_8).replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            ExcelWriter excelWriter = EasyExcel.write(outputStream).build();
            
            List<Map<String, Object>> deptData = reportService.getDeptEmpCount();
            WriteSheet deptSheet = EasyExcel.writerSheet(0, "部门人数").build();
            List<List<Object>> deptRows = new java.util.ArrayList<>();
            deptRows.add(List.of("部门名称", "人数"));
            for (Map<String, Object> row : deptData) {
                List<Object> rowData = new java.util.ArrayList<>();
                rowData.add(row.get("deptName") != null ? row.get("deptName").toString() : "");
                rowData.add(row.get("empCount") != null ? row.get("empCount") : 0);
                deptRows.add(rowData);
            }
            excelWriter.write(deptRows, deptSheet);
            
            List<Map<String, Object>> leaveData = reportService.getLeaveList();
            WriteSheet leaveSheet = EasyExcel.writerSheet(1, "请假数据").build();
            List<List<Object>> leaveRows = new java.util.ArrayList<>();
            leaveRows.add(List.of("员工姓名", "部门", "请假类型", "开始日期", "结束日期", "原因", "状态"));
            for (Map<String, Object> row : leaveData) {
                List<Object> rowData = new java.util.ArrayList<>();
                rowData.add(row.get("empName") != null ? row.get("empName").toString() : "");
                rowData.add(row.get("deptName") != null ? row.get("deptName").toString() : "");
                rowData.add(row.get("leaveTypeName") != null ? row.get("leaveTypeName").toString() : "");
                rowData.add(row.get("startDate") != null ? row.get("startDate").toString() : "");
                rowData.add(row.get("endDate") != null ? row.get("endDate").toString() : "");
                rowData.add(row.get("reason") != null ? row.get("reason").toString() : "");
                rowData.add(row.get("statusText") != null ? row.get("statusText").toString() : "");
                leaveRows.add(rowData);
            }
            excelWriter.write(leaveRows, leaveSheet);
            
            List<Map<String, Object>> expenseData = reportService.getExpenseList();
            WriteSheet expenseSheet = EasyExcel.writerSheet(2, "报销数据").build();
            List<List<Object>> expenseRows = new java.util.ArrayList<>();
            expenseRows.add(List.of("报销单号", "员工姓名", "部门", "金额", "类型", "说明", "状态", "时间"));
            for (Map<String, Object> row : expenseData) {
                List<Object> rowData = new java.util.ArrayList<>();
                rowData.add(row.get("reportNo") != null ? row.get("reportNo").toString() : "");
                rowData.add(row.get("empName") != null ? row.get("empName").toString() : "");
                rowData.add(row.get("deptName") != null ? row.get("deptName").toString() : "");
                rowData.add(row.get("totalAmount") != null ? row.get("totalAmount") : 0);
                rowData.add(row.get("expenseType") != null ? row.get("expenseType").toString() : "");
                rowData.add(row.get("description") != null ? row.get("description").toString() : "");
                rowData.add(row.get("statusText") != null ? row.get("statusText").toString() : "");
                rowData.add(row.get("createTime") != null ? row.get("createTime").toString() : "");
                expenseRows.add(rowData);
            }
            excelWriter.write(expenseRows, expenseSheet);
            
            excelWriter.finish();
            
            byte[] bytes = outputStream.toByteArray();
            response.setContentLength(bytes.length);
            response.getOutputStream().write(bytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("导出失败: " + e.getMessage(), e);
        }
    }
}