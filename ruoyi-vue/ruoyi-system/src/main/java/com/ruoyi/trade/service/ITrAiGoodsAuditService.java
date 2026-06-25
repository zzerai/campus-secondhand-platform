package com.ruoyi.trade.service;

import com.ruoyi.trade.domain.TrAiAuditRecord;

/**
 * AI商品审核Service接口
 *
 * @author daj
 * @date 2026-05-20
 */
public interface ITrAiGoodsAuditService
{
    /**
     * 对指定商品进行AI审核，返回审核记录
     *
     * @param goodsId 商品ID
     * @return AI审核记录
     */
    TrAiAuditRecord auditGoods(Long goodsId);
}
