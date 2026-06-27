package org.buu.oa.controller;

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
     * 查询已发布的通知列表
     * @return 通知列表
     */
    @GetMapping
    public Result<List<SysNotice>> list() {
        List<SysNotice> notices = sysNoticeService.listPublished();
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
        if (user == null) {
            return Result.<Map<String, Long>>unauthorized("未登录");
        }
        
        long count = sysNoticeService.countUnread(user.getId());
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
        if (user == null) {
            return Result.<Void>unauthorized("未登录");
        }
        
        sysNoticeService.markAsRead(id, user.getId());
        return Result.<Void>success("已标记为已读", null);
    }
}