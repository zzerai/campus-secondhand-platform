package com.ruoyi.trade.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeAnnouncement;

/**
 * 交易公告Mapper接口
 *
 * @author ruoyi
 * @date 2026-05-25
 */
public interface TrTradeAnnouncementMapper
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
     * @param announcementId 交易公告主键
     * @return 结果
     */
    public int deleteTrTradeAnnouncementByAnnouncementId(Long announcementId);

    /**
     * 批量删除交易公告（逻辑删除）
     *
     * @param announcementIds 需要删除的交易公告主键集合
     * @return 结果
     */
    public int deleteTrTradeAnnouncementByAnnouncementIds(Long[] announcementIds);

    /**
     * 发布公告
     *
     * @param announcementId 交易公告主键
     * @param updateBy 更新者
     * @return 结果
     */
    public int publishAnnouncement(@Param("announcementId") Long announcementId, @Param("updateBy") String updateBy);

    /**
     * 撤回公告
     *
     * @param announcementId 交易公告主键
     * @param updateBy 更新者
     * @return 结果
     */
    public int retractAnnouncement(@Param("announcementId") Long announcementId, @Param("updateBy") String updateBy);
}
