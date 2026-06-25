package com.ruoyi.trade.service.impl;

import java.util.List;
import com.alibaba.fastjson2.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.mapper.TrTradeAnnouncementMapper;
import com.ruoyi.trade.domain.TrTradeAnnouncement;
import com.ruoyi.trade.service.ITrTradeAnnouncementService;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 交易公告Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-25
 */
@Service
public class TrTradeAnnouncementServiceImpl implements ITrTradeAnnouncementService
{
    /** 移动端公告列表缓存键 */
    private static final String NOTICE_CACHE_KEY = CacheConstants.TRADE_ANNOUNCEMENT_KEY + "app_list";

    @Autowired
    private TrTradeAnnouncementMapper trTradeAnnouncementMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 查询交易公告
     *
     * @param announcementId 交易公告主键
     * @return 交易公告
     */
    @Override
    public TrTradeAnnouncement selectTrTradeAnnouncementByAnnouncementId(Long announcementId)
    {
        return trTradeAnnouncementMapper.selectTrTradeAnnouncementByAnnouncementId(announcementId);
    }

    /**
     * 查询交易公告列表
     *
     * @param trTradeAnnouncement 交易公告
     * @return 交易公告
     */
    @Override
    public List<TrTradeAnnouncement> selectTrTradeAnnouncementList(TrTradeAnnouncement trTradeAnnouncement)
    {
        return trTradeAnnouncementMapper.selectTrTradeAnnouncementList(trTradeAnnouncement);
    }

    @Override
    public List<TrTradeAnnouncement> selectAppNoticeList()
    {
        JSONArray cached = redisCache.getCacheObject(NOTICE_CACHE_KEY);
        if (cached != null)
        {
            // FastJson2 序列化器读回为 JSONArray，需显式转回实体（与 DictUtils 同一约定）
            return cached.toList(TrTradeAnnouncement.class);
        }
        // 移动端只展示已发布公告（publish_status='1'），与 /app/notice/detail 的草稿拦截保持一致；
        // 按置顶优先、发布时间倒序由 Mapper 排序
        TrTradeAnnouncement query = new TrTradeAnnouncement();
        query.setPublishStatus("1");
        List<TrTradeAnnouncement> list = trTradeAnnouncementMapper.selectTrTradeAnnouncementList(query);
        redisCache.setCacheObject(NOTICE_CACHE_KEY, list);
        return list;
    }

    /** 任一公告写操作后清除移动端公告列表缓存，下次查询回源重建。 */
    private void evictNoticeCache()
    {
        redisCache.deleteObject(NOTICE_CACHE_KEY);
    }

    /**
     * 新增交易公告
     *
     * @param trTradeAnnouncement 交易公告
     * @return 结果
     */
    @Override
    public int insertTrTradeAnnouncement(TrTradeAnnouncement trTradeAnnouncement)
    {
        String username = SecurityUtils.getUsername();
        trTradeAnnouncement.setCreateBy(username);
        trTradeAnnouncement.setUpdateBy(username);
        // createTime / updateTime 由 AuditTimeFillInterceptor 兜底
        int rows = trTradeAnnouncementMapper.insertTrTradeAnnouncement(trTradeAnnouncement);
        evictNoticeCache();
        return rows;
    }

    /**
     * 修改交易公告
     *
     * @param trTradeAnnouncement 交易公告
     * @return 结果
     */
    @Override
    public int updateTrTradeAnnouncement(TrTradeAnnouncement trTradeAnnouncement)
    {
        trTradeAnnouncement.setUpdateBy(SecurityUtils.getUsername());
        // updateTime 由 AuditTimeFillInterceptor 兜底
        int rows = trTradeAnnouncementMapper.updateTrTradeAnnouncement(trTradeAnnouncement);
        evictNoticeCache();
        return rows;
    }

    /**
     * 删除交易公告（逻辑删除）
     *
     * @param announcementIds 需要删除的交易公告主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeAnnouncementByAnnouncementIds(Long[] announcementIds)
    {
        int rows = trTradeAnnouncementMapper.deleteTrTradeAnnouncementByAnnouncementIds(announcementIds);
        evictNoticeCache();
        return rows;
    }

    /**
     * 删除交易公告信息（逻辑删除）
     *
     * @param announcementId 交易公告主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeAnnouncementByAnnouncementId(Long announcementId)
    {
        int rows = trTradeAnnouncementMapper.deleteTrTradeAnnouncementByAnnouncementId(announcementId);
        evictNoticeCache();
        return rows;
    }

    /**
     * 发布公告
     *
     * @param announcementId 交易公告主键
     * @return 结果
     */
    @Override
    public int publishAnnouncement(Long announcementId)
    {
        int rows = trTradeAnnouncementMapper.publishAnnouncement(announcementId, SecurityUtils.getUsername());
        evictNoticeCache();
        return rows;
    }

    /**
     * 撤回公告
     *
     * @param announcementId 交易公告主键
     * @return 结果
     */
    @Override
    public int retractAnnouncement(Long announcementId)
    {
        int rows = trTradeAnnouncementMapper.retractAnnouncement(announcementId, SecurityUtils.getUsername());
        evictNoticeCache();
        return rows;
    }
}
