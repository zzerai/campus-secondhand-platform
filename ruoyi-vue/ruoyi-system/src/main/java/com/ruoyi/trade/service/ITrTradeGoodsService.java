package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.vo.BatchAuditResult;
import com.ruoyi.trade.domain.vo.ImportResult;

/**
 * 闲置商品Service接口
 *
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeGoodsService
{
    /**
     * 查询闲置商品（含联表展示信息 + 图片列表）
     */
    public TrTradeGoods selectTrTradeGoodsByGoodsId(Long goodsId);

    /**
     * 查询闲置商品列表（含联表展示信息）
     */
    public List<TrTradeGoods> selectTrTradeGoodsList(TrTradeGoods trTradeGoods);

    /**
     * 新增闲置商品（管理端：附带分类/卖家有效性校验）
     */
    public int insertTrTradeGoods(TrTradeGoods trTradeGoods);

    /**
     * 修改闲置商品
     */
    public int updateTrTradeGoods(TrTradeGoods trTradeGoods);

    /**
     * 逻辑删除
     */
    public int deleteTrTradeGoodsByGoodsIds(Long[] goodsIds);

    public int deleteTrTradeGoodsByGoodsId(Long goodsId);

    /**
     * 审核单个商品。
     *
     * @param goodsId     商品ID
     * @param goodsStatus 目标状态：'1' 通过、'2' 拒绝
     * @param auditRemark 审核意见（拒绝时建议必填）
     * @return 影响行数
     */
    public int auditGoods(Long goodsId, String goodsStatus, String auditRemark);

    /**
     * 批量审核：仅允许通过('1')或拒绝('2')，原状态必须为待审核('0')。
     * 返回结构化结果，含成功条数与失败明细，便于前端提示部分失败。
     */
    public BatchAuditResult batchAuditGoods(Long[] goodsIds, String goodsStatus, String auditRemark);

    /**
     * 强制下架：仅允许从 '1' 已上架 -> '3' 已下架。
     */
    public int offlineGoods(Long goodsId);

    /**
     * 恢复上架：仅允许从 '3' 已下架 -> '1' 已上架。
     */
    public int onlineGoods(Long goodsId);

    /**
     * 批量导入商品
     *
     * @param goodsList 商品列表（Excel解析后的实体）
     * @param updateSupport 是否支持更新（true=存在按标题+卖家匹配则更新，false=存在则跳过）
     * @return 导入结果
     */
    public ImportResult importGoods(List<TrTradeGoods> goodsList, boolean updateSupport);
}
