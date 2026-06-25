package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeFavorite;
import com.ruoyi.trade.domain.TrTradeGoods;

/**
 * 商品收藏Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeFavoriteService 
{
    /**
     * 查询商品收藏
     * 
     * @param favoriteId 商品收藏主键
     * @return 商品收藏
     */
    public TrTradeFavorite selectTrTradeFavoriteByFavoriteId(Long favoriteId);

    /**
     * 查询商品收藏列表
     * 
     * @param trTradeFavorite 商品收藏
     * @return 商品收藏集合
     */
    public List<TrTradeFavorite> selectTrTradeFavoriteList(TrTradeFavorite trTradeFavorite);

    /**
     * 新增商品收藏
     * 
     * @param trTradeFavorite 商品收藏
     * @return 结果
     */
    public int insertTrTradeFavorite(TrTradeFavorite trTradeFavorite);

    /**
     * 修改商品收藏
     * 
     * @param trTradeFavorite 商品收藏
     * @return 结果
     */
    public int updateTrTradeFavorite(TrTradeFavorite trTradeFavorite);

    /**
     * 批量删除商品收藏
     * 
     * @param favoriteIds 需要删除的商品收藏主键集合
     * @return 结果
     */
    public int deleteTrTradeFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * 删除商品收藏信息
     *
     * @param favoriteId 商品收藏主键
     * @return 结果
     */
    public int deleteTrTradeFavoriteByFavoriteId(Long favoriteId);

    /**
     * 新增收藏（业务方法，幂等）：
     * <ul>
     *     <li>商品必须存在、未逻辑删除、且处于"已上架"状态（goods_status='1'）；</li>
     *     <li>不允许收藏自己发布的商品；</li>
     *     <li>已被收藏过（包括软删除后再收藏）走 ON DUPLICATE KEY UPDATE 复活，避免 uk_user_goods 唯一键冲突；</li>
     *     <li>仅在真正从"未收藏"切换到"已收藏"时维护 goods.favorite_count + 1。</li>
     * </ul>
     *
     * @param userId   学生用户ID
     * @param goodsId  商品ID
     * @param createBy 创建者标识（学号 / 管理员名）
     * @return 1 = 本次新增/复活成功；0 = 原本就已收藏（前端可视为"已收藏"成功响应）
     */
    public int addFavorite(Long userId, Long goodsId, String createBy);

    /**
     * 取消收藏（移动端业务方法，幂等）：按 (userId, goodsId) 软删除，并维护 goods.favorite_count - 1。
     *
     * @return 1 = 本次取消成功；0 = 原本就未收藏
     */
    public int removeFavorite(Long userId, Long goodsId, String updateBy);

    /**
     * 当前用户是否已收藏指定商品。
     */
    public boolean existsFavorite(Long userId, Long goodsId);

    /**
     * 查询"我的收藏"商品列表，按收藏时间倒序。
     */
    public List<TrTradeGoods> selectMyFavoriteGoods(Long userId);
}
