package com.ruoyi.trade.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeFavorite;
import com.ruoyi.trade.domain.TrTradeGoods;

/**
 * 商品收藏Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeFavoriteMapper 
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
     * 删除商品收藏
     * 
     * @param favoriteId 商品收藏主键
     * @return 结果
     */
    public int deleteTrTradeFavoriteByFavoriteId(Long favoriteId);

    /**
     * 批量删除商品收藏
     *
     * @param favoriteIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeFavoriteByFavoriteIds(Long[] favoriteIds);

    /**
     * upsert 收藏：用 INSERT ... ON DUPLICATE KEY UPDATE 解决软删行 + uk_user_goods 唯一键冲突。
     * 已存在（无论 del_flag）的同 (user_id, goods_id) 行被复活为 del_flag='0' 并更新 update_*。
     *
     * <p>注意：MySQL 在 ON DUPLICATE KEY UPDATE 触发且新值与旧值完全一致时会返回 0 行，
     * 因此 service 层不能仅凭返回值判断"是否新增了一行收藏"，需配合 {@link #existsFavorite}
     * 在 upsert 之前查询活跃收藏，准确维护 goods.favorite_count。</p>
     *
     * @param userId   学生用户ID
     * @param goodsId  商品ID
     * @param createBy 创建者（学号）
     * @return 受影响行数：1 = insert 新行；2 = update 已存在行；0 = 已存在且无字段变化
     */
    public int upsertFavorite(@Param("userId") Long userId,
                              @Param("goodsId") Long goodsId,
                              @Param("createBy") String createBy);

    /**
     * 按 (user_id, goods_id) 软删除当前活跃的收藏行，幂等。
     *
     * @param userId   学生用户ID
     * @param goodsId  商品ID
     * @param updateBy 更新者（学号）
     * @return 受影响行数：1 = 取消成功；0 = 原本就未收藏
     */
    public int softDeleteByUserAndGoods(@Param("userId") Long userId,
                                        @Param("goodsId") Long goodsId,
                                        @Param("updateBy") String updateBy);

    /**
     * 查询当前用户是否已收藏指定商品（仅看活跃行）。
     *
     * @return 1 = 已收藏；0 = 未收藏
     */
    public int existsFavorite(@Param("userId") Long userId,
                              @Param("goodsId") Long goodsId);

    /**
     * 我的收藏列表：联表 tr_trade_goods / tr_trade_category / tr_student_user，
     * 仅返回未被卖家软删除的商品（包含已下架/已售出，以便前端展示状态）；
     * 按收藏时间倒序返回。
     */
    public List<TrTradeGoods> selectMyFavoriteGoods(@Param("userId") Long userId);
}
