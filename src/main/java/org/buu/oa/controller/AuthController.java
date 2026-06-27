package org.buu.oa.controller;

import lombok.Data;
import org.buu.oa.common.Result;
import org.buu.oa.entity.EmpEmployee;
import org.buu.oa.entity.SysMenu;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.AuthService;
import org.buu.oa.service.EmpEmployeeService;
import org.buu.oa.service.SysMenuService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、获取用户信息、登出等认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmpEmployeeService empEmployeeService;
    private final SysMenuService sysMenuService;

    public AuthController(AuthService authService, EmpEmployeeService empEmployeeService, SysMenuService sysMenuService) {
        this.authService = authService;
        this.empEmployeeService = empEmployeeService;
        this.sysMenuService = sysMenuService;
    }

    /**
     * 登录请求参数
     */
    @Data
    public static class LoginRequest {
        /** 用户名 */
        private String username;
        /** 密码 */
        private String password;
    }

    /**
     * 用户登录
     * @param request 登录请求（包含用户名和密码）
     * @return 登录成功后返回token、用户信息和菜单列表
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        SysUser user = authService.getCurrentUser();
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        
        // 查询员工信息和菜单权限
        if (user != null && user.getEmpId() != null) {
            EmpEmployee employee = empEmployeeService.getById(user.getEmpId());
            data.put("employee", employee);
            
            List<SysMenu> menus = sysMenuService.getMenusByUserId(user.getId());
            data.put("menus", menus);
        }
        
        return Result.success(data);
    }

    /**
     * 获取当前用户信息
     * @return 用户信息、员工信息和菜单列表
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo() {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Map<String, Object>>unauthorized("未登录");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        
        // 查询员工信息和菜单权限
        if (user.getEmpId() != null) {
            EmpEmployee employee = empEmployeeService.getById(user.getEmpId());
            data.put("employee", employee);
            
            List<SysMenu> menus = sysMenuService.getMenusByUserId(user.getId());
            data.put("menus", menus);
        }
        
        return Result.success(data);
    }

    /**
     * 用户登出
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}