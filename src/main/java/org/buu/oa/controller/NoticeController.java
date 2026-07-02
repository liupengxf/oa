package org.buu.oa.controller;

import lombok.Data;
import org.buu.oa.common.Result;
import org.buu.oa.entity.SysNotice;
import org.buu.oa.entity.SysUser;
import org.buu.oa.service.SysNoticeService;
import org.buu.oa.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 * 处理通知公告的查询、阅读、标记已读等接口
 */
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final SysNoticeService sysNoticeService;
    private final AuthService authService;

    public NoticeController(SysNoticeService sysNoticeService, AuthService authService) {
        this.sysNoticeService = sysNoticeService;
        this.authService = authService;
    }

    /**
     * 查询已发布的通知列表（带用户已读状态）
     * @return 通知列表
     */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        SysUser user = authService.getCurrentUser();
        Long userId = user != null ? user.getId() : 1L;
        List<Map<String, Object>> notices = sysNoticeService.listPublishedWithReadStatus(userId);
        return Result.success(notices);
    }

    /**
     * 查询单个通知详情
     * 查询时自动标记为已读
     * @param id 通知ID
     * @return 通知详情
     */
    @GetMapping("/{id}")
    public Result<SysNotice> getById(@PathVariable Long id) {
        SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            return Result.<SysNotice>notFound("公告不存在");
        }
        
        // 自动标记为已读
        SysUser user = authService.getCurrentUser();
        if (user != null) {
            sysNoticeService.markAsRead(id, user.getId());
        }
        
        return Result.success(notice);
    }

    /**
     * 查询未读通知数量
     * @return 未读数量
     */
    @GetMapping("/unread/count")
    public Result<Map<String, Long>> countUnread() {
        SysUser user = authService.getCurrentUser();
        Long userId = user != null ? user.getId() : 1L;
        
        long count = sysNoticeService.countUnread(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", count);
        return Result.success(result);
    }

    /**
     * 手动标记通知为已读
     * @param id 通知ID
     * @return 操作结果
     */
    @PostMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        SysUser user = authService.getCurrentUser();
        Long userId = user != null ? user.getId() : 1L;
        
        sysNoticeService.markAsRead(id, userId);
        return Result.<Void>success("已标记为已读", null);
    }

    /**
     * 发布新公告
     * @param request 公告请求
     * @return 创建的公告
     */
    @PostMapping
    public Result<SysNotice> create(@RequestBody NoticeRequest request) {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            user = new SysUser();
            user.setId(1L);
            user.setUsername("admin");
        }
        
        SysNotice notice = new SysNotice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setPublisherId(user.getId());
        notice.setType(request.getType() != null ? request.getType() : 1);
        notice.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        
        SysNotice created = sysNoticeService.create(notice);
        return Result.success("公告发布成功", created);
    }

    /**
     * 一键全部已读
     * @return 操作结果
     */
    @PostMapping("/read-all")
    public Result<Void> readAll() {
        SysUser user = authService.getCurrentUser();
        if (user == null) {
            return Result.<Void>unauthorized("未登录");
        }
        
        sysNoticeService.readAll(user.getId());
        return Result.<Void>success("已全部标记为已读", null);
    }

    /**
     * 公告请求参数
     */
    @Data
    public static class NoticeRequest {
        private String title;
        private String content;
        private Integer type;
        private Integer status;
    }
}