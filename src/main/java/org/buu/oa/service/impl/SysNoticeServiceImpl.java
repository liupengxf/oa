package org.buu.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.buu.oa.entity.SysNotice;
import org.buu.oa.entity.SysNoticeRead;
import org.buu.oa.mapper.SysNoticeMapper;
import org.buu.oa.mapper.SysNoticeReadMapper;
import org.buu.oa.service.SysNoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知服务实现类
 * 实现通知查询、标记已读等通知相关功能
 */
@Service
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService {

    private final SysNoticeReadMapper sysNoticeReadMapper;

    public SysNoticeServiceImpl(SysNoticeReadMapper sysNoticeReadMapper) {
        this.sysNoticeReadMapper = sysNoticeReadMapper;
    }

    /**
     * 查询已发布的通知列表
     * 只查询状态为已发布的通知，按创建时间倒序排列
     * @return 通知列表
     */
    @Override
    public List<SysNotice> listPublished() {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getStatus, 1);          // 只查询已发布的通知
        wrapper.orderByDesc(SysNotice::getCreateTime); // 按创建时间倒序排列
        return baseMapper.selectList(wrapper);
    }

    /**
     * 查询已发布的通知列表（带用户已读状态）
     * @param userId 用户ID
     * @return 通知列表（包含已读状态）
     */
    @Override
    public List<Map<String, Object>> listPublishedWithReadStatus(Long userId) {
        List<SysNotice> published = listPublished();
        if (published.isEmpty()) {
            return List.of();
        }

        Set<Long> noticeIds = published.stream().map(SysNotice::getId).collect(Collectors.toSet());
        
        final Set<Long> readNoticeIds;
        if (userId != null) {
            LambdaQueryWrapper<SysNoticeRead> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysNoticeRead::getUserId, userId);
            wrapper.in(SysNoticeRead::getNoticeId, noticeIds);
            readNoticeIds = sysNoticeReadMapper.selectList(wrapper).stream()
                    .map(SysNoticeRead::getNoticeId)
                    .collect(Collectors.toSet());
        } else {
            readNoticeIds = Set.of();
        }

        return published.stream().map(notice -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", notice.getId());
            map.put("title", notice.getTitle());
            map.put("content", notice.getContent());
            map.put("publisherId", notice.getPublisherId());
            map.put("type", notice.getType());
            map.put("status", notice.getStatus());
            map.put("createTime", notice.getCreateTime());
            map.put("updateTime", notice.getUpdateTime());
            map.put("read", readNoticeIds.contains(notice.getId()));
            return map;
        }).collect(Collectors.toList());
    }

    /**
     * 标记通知为已读
     * 查询用户是否已阅读该通知，有则更新阅读时间，无则创建阅读记录
     * @param noticeId 通知ID
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void markAsRead(Long noticeId, Long userId) {
        LambdaQueryWrapper<SysNoticeRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNoticeRead::getNoticeId, noticeId);
        wrapper.eq(SysNoticeRead::getUserId, userId);
        
        SysNoticeRead read = sysNoticeReadMapper.selectOne(wrapper);
        if (read == null) {
            // 用户未阅读过该通知，创建新的阅读记录
            read = new SysNoticeRead();
            read.setNoticeId(noticeId);
            read.setUserId(userId);
            read.setReadTime(LocalDateTime.now());
            sysNoticeReadMapper.insert(read);
        } else {
            // 用户已阅读过该通知，更新阅读时间
            read.setReadTime(LocalDateTime.now());
            sysNoticeReadMapper.updateById(read);
        }
    }

    /**
     * 统计未读通知数量
     * 已发布通知总数减去已读通知数量
     * @param userId 用户ID
     * @return 未读数量
     */
    @Override
    public long countUnread(Long userId) {
        List<SysNotice> published = listPublished();
        if (published.isEmpty()) {
            return 0;
        }
        
        LambdaQueryWrapper<SysNoticeRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNoticeRead::getUserId, userId);
        wrapper.in(SysNoticeRead::getNoticeId, 
            published.stream().map(SysNotice::getId).toList());
        
        return published.size() - sysNoticeReadMapper.selectCount(wrapper);
    }

    /**
     * 创建公告
     * @param notice 公告实体
     * @return 创建后的公告
     */
    @Override
    @Transactional
    public SysNotice create(SysNotice notice) {
        baseMapper.insert(notice);
        return notice;
    }

    /**
     * 一键全部已读
     * @param userId 用户ID
     */
    @Override
    @Transactional
    public void readAll(Long userId) {
        List<SysNotice> published = listPublished();
        for (SysNotice notice : published) {
            markAsRead(notice.getId(), userId);
        }
    }
}