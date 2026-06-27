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
import java.util.List;

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
        
        // 未读数量 = 已发布通知总数 - 已读通知数量
        return published.size() - sysNoticeReadMapper.selectCount(wrapper);
    }
}