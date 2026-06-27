package org.buu.oa.controller;

import org.buu.oa.common.Result;
import org.buu.oa.entity.EmpEmployee;
import org.buu.oa.service.EmpEmployeeService;
import org.springframework.web.bind.annotation.*;

/**
 * 员工控制器
 * 处理员工信息查询相关接口
 */
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmpEmployeeService empEmployeeService;

    public EmployeeController(EmpEmployeeService empEmployeeService) {
        this.empEmployeeService = empEmployeeService;
    }

    /**
     * 根据ID查询员工信息
     * @param id 员工ID
     * @return 员工信息
     */
    @GetMapping("/{id}")
    public Result<EmpEmployee> getById(@PathVariable Long id) {
        EmpEmployee employee = empEmployeeService.getById(id);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error(404, "员工不存在");
    }
}