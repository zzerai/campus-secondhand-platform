package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.dto.AppGoodsPublishDto;
import com.ruoyi.trade.domain.dto.AppGoodsUpdateDto;

/**
 * 移动端商品业务接口（发布、修改、下架、删除、列表、详情）。
 * 与管理端 {@link ITrTradeGoodsService}（审核、上下架、CRUD）共享同一 Entity 与 Mapper。
 */
public interface IAppGoodsService
{
    /** 移动端公开商品列表（仅已上架）。 */
    public List<TrTradeGoods> selectAppGoodsList(TrTradeGoods goods);

    /**
     * 查询商品详情。
     *
     * @param goodsId       商品ID
     * @param currentUserId 当前登录用户ID，匿名访问时为 {@code null}
     * @return 非卖家本人仅能查看已上架商品，否则返回 {@code null}
     */
    public TrTradeGoods selectTradeGoodsById(Long goodsId, Long currentUserId);

    /** 当前用户发布的商品列表。 */
    public List<TrTradeGoods> selectMyGoodsList(TrTradeGoods goods);

    /**
     * 发布商品（移动端）。
     *
     * @param dto      入参 DTO，仅含用户可填写字段
     * @param sellerId 卖家ID（从 token 取）
     * @param createBy 卖家用户名（写 create_by 审计列）
     * @return 影响行数
     */
    public int publishGoods(AppGoodsPublishDto dto, Long sellerId, String createBy);

    /**
     * 修改商品（移动端）。状态会被服务端重置为待审核。
     *
     * @param dto      入参 DTO
     * @param sellerId 当前用户ID（用于属主校验）
     * @param updateBy 用户名（写 update_by 审计列）
     * @return 影响行数
     */
    public int updateGoods(AppGoodsUpdateDto dto, Long sellerId, String updateBy);

    public int offlineGoods(Long goodsId, Long userId);

    public int deleteGoods(Long goodsId, Long userId);
}
