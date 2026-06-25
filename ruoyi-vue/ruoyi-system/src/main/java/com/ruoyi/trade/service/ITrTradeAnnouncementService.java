package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeAnnouncement;

/**
 * 交易公告Service接口
 *
 * @author ruoyi
 * @date 2026-05-25
 */
public interface ITrTradeAnnouncementService
{
    /**
     * 查询交易公告
     *
     * @param announcementId 交易公告主键
     * @return 交易公告
     */
    public TrTradeAnnouncement selectTrTradeAnnouncementByAnnouncementId(Long announcementId);

    /**
     * 查询交易公告列表
     *
     * @param trTradeAnnouncement 交易公告
     * @return 交易公告集合
     */
    public List<TrTradeAnnouncement> selectTrTradeAnnouncementList(TrTradeAnnouncement trTradeAnnouncement);

    /**
     * 查询移动端公告列表（带 Redis 缓存）。
     * 供匿名高频读的 /app/notice/list 使用，公告写操作后自动失效缓存。
     *
     * @return 交易公告集合
     */
    public List<TrTradeAnnouncement> selectAppNoticeList();

    /**
     * 新增交易公告
     *
     * @param trTradeAnnouncement 交易公告
     * @return 结果
     */
    public int insertTrTradeAnnouncement(TrTradeAnnouncement trTradeAnnouncement);

    /**
     * 修改交易公告
     *
     * @param trTradeAnnouncement 交易公告
     * @return 结果
     */
    public int updateTrTradeAnnouncement(TrTradeAnnouncement trTradeAnnouncement);

    /**
     * 删除交易公告（逻辑删除）
     *
     * @param announcementIds 需要删除的交易公告主键集合
     * @return 结果
     */
    public int deleteTrTradeAnnouncementByAnnouncementIds(Long[] announcementIds);

    /**
     * 删除交易公告信息（逻辑删除）
     *
     * @param announcementId 交易公告主键
     * @return 结果
     */
    public int deleteTrTradeAnnouncementByAnnouncementId(Long announcementId);

    /**
     * 发布公告
     *
     * @param announcementId 交易公告主键
     * @return 结果
     */
    public int publishAnnouncement(Long announcementId);

    /**
     * 撤回公告
     *
     * @param announcementId 交易公告主键
     * @return 结果
     */
    public int retractAnnouncement(Long announcementId);
}
