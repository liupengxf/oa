package org.buu.oa.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.buu.oa.entity.SysNotice;

import java.util.List;
import java.util.Map;

/**
 * 通知服务接口
 * 提供通知查询、标记已读等通知相关功能
 */
public interface SysNoticeService extends IService<SysNotice> {

    /**
     * 查询已发布的通知列表
     * @return 通知列表
     */
    List<SysNotice> listPublished();

    /**
     * 查询已发布的通知列表（带用户已读状态）
     * @param userId 用户ID
     * @return 通知列表（包含已读状态）
     */
    List<Map<String, Object>> listPublishedWithReadStatus(Long userId);

    /**
     * 标记通知为已读
     * @param noticeId 通知ID
     * @param userId 用户ID
     */
    void markAsRead(Long noticeId, Long userId);

    /**
     * 统计未读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    long countUnread(Long userId);

    /**
     * 创建公告
     * @param notice 公告实体
     * @return 创建后的公告
     */
    SysNotice create(SysNotice notice);

    /**
     * 一键全部已读
     * @param userId 用户ID
     */
    void readAll(Long userId);
}