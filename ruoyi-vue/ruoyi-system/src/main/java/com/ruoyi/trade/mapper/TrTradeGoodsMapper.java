package com.ruoyi.trade.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsCategoryVo;

/**
 * 闲置商品Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeGoodsMapper 
{
    /**
     * 查询闲置商品
     *
     * @param goodsId 闲置商品主键
     * @return 闲置商品
     */
    public TrTradeGoods selectTrTradeGoodsByGoodsId(Long goodsId);

    /**
     * 行锁查询商品（仅锁本行，不连表，不聚合），供下单等并发场景使用。
     *
     * @param goodsId 商品ID
     * @return 商品
     */
    public TrTradeGoods selectTrTradeGoodsForUpdate(Long goodsId);

    /**
     * 查询闲置商品列表
     *
     * @param trTradeGoods 闲置商品
     * @return 闲置商品集合
     */
    public List<TrTradeGoods> selectTrTradeGoodsList(TrTradeGoods trTradeGoods);

    /**
     * 移动端公开商品列表（仅返回已上架，匿名脱敏列：不含 contact_way / audit_* / del_flag / create_by / update_by）。
     * 支持 categoryId / keyword / sort 过滤。
     */
    public List<TrTradeGoods> selectAppGoodsList(TrTradeGoods trTradeGoods);

    /**
     * 卖家"我的商品"列表（按 sellerId + 可选 goodsStatus / keyword 过滤）。
     */
    public List<TrTradeGoods> selectMyGoodsList(TrTradeGoods trTradeGoods);

    /** 浏览次数批量回刷：view_count += delta（由 Redis 缓冲定时回刷调用，避免每次浏览写库）。 */
    public int increaseViewCountBy(@Param("goodsId") Long goodsId, @Param("delta") long delta);

    /** 收藏次数 +1（仅对未软删商品生效）。 */
    public int increaseFavoriteCount(Long goodsId);

    /** 收藏次数 -1（带 favorite_count &gt; 0 保护，避免负数）。 */
    public int decreaseFavoriteCount(Long goodsId);

    /** 卖家本人 + 期望状态校验下切换商品状态，影响 0 行表示状态不符或非属主。 */
    public int updateGoodsStatusByOwner(@Param("goodsId") Long goodsId,
                                        @Param("sellerId") Long sellerId,
                                        @Param("goodsStatus") String goodsStatus,
                                        @Param("expectStatus") String expectStatus);

    /** 仅在商品当前状态等于 soldStatus 时回退为 onSaleStatus（订单取消时使用）。 */
    public int restoreSoldGoodsToOnSale(@Param("goodsId") Long goodsId,
                                        @Param("soldStatus") String soldStatus,
                                        @Param("onSaleStatus") String onSaleStatus);

    /** 卖家本人逻辑删除商品（已售出商品不可删）。 */
    public int deleteGoodsByOwner(@Param("goodsId") Long goodsId,
                                  @Param("sellerId") Long sellerId);

    /** 统计某卖家指定状态的商品数（用户公开主页：在售 '1' / 已售 '4'）。 */
    public Long countSellerGoodsByStatus(@Param("sellerId") Long sellerId,
                                         @Param("goodsStatus") String goodsStatus);

    /** 查询商品图片 URL，多张以逗号拼接。 */
    public String selectGoodsImageUrls(Long goodsId);

    /** 逻辑删除某商品的全部图片。 */
    public int deleteGoodsImagesByGoodsId(Long goodsId);

    /** 批量写入商品图片，列表顺序即排序。 */
    public int batchInsertGoodsImages(@Param("goodsId") Long goodsId,
                                      @Param("urls") List<String> urls,
                                      @Param("createBy") String createBy);

    /**
     * 根据卖家ID和标题查询商品（用于导入去重）。
     *
     * @param sellerId 卖家学生用户ID
     * @param title 商品标题
     * @return 商品（可能多个，取第一个）
     */
    public TrTradeGoods selectBySellerAndTitle(@Param("sellerId") Long sellerId, @Param("title") String title);

    /**
     * Query total goods count.
     *
     * @return total count
     */
    public Long selectGoodsTotalCount();

    /**
     * Query pending audit goods count.
     *
     * @return pending audit count
     */
    public Long selectPendingAuditGoodsCount();

    /**
     * Query on-shelf goods count.
     *
     * @return on-shelf count
     */
    public Long selectOnShelfGoodsCount();

    /**
     * Query category goods statistics.
     *
     * @return category statistics
     */
    public List<TrTradeStatisticsCategoryVo> selectCategoryStatistics();

    /**
     * 新增闲置商品
     * 
     * @param trTradeGoods 闲置商品
     * @return 结果
     */
    public int insertTrTradeGoods(TrTradeGoods trTradeGoods);

    /**
     * 修改闲置商品
     *
     * @param trTradeGoods 闲置商品
     * @return 结果
     */
    public int updateTrTradeGoods(TrTradeGoods trTradeGoods);

    /**
     * 审核：仅更新 goods_status / audit_user_id / audit_time / audit_remark / update_*
     */
    public int updateGoodsAudit(@Param("goodsId") Long goodsId,
                                @Param("goodsStatus") String goodsStatus,
                                @Param("auditUserId") Long auditUserId,
                                @Param("auditTime") java.util.Date auditTime,
                                @Param("auditRemark") String auditRemark,
                                @Param("updateBy") String updateBy,
                                @Param("updateTime") java.util.Date updateTime);

    /**
     * 仅切换状态（上下架）
     */
    public int updateGoodsStatus(@Param("goodsId") Long goodsId,
                                 @Param("goodsStatus") String goodsStatus,
                                 @Param("updateBy") String updateBy,
                                 @Param("updateTime") java.util.Date updateTime);

    /**
     * 删除闲置商品
     * 
     * @param goodsId 闲置商品主键
     * @return 结果
     */
    public int deleteTrTradeGoodsByGoodsId(Long goodsId);

    /**
     * 批量删除闲置商品
     * 
     * @param goodsIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeGoodsByGoodsIds(Long[] goodsIds);
}
