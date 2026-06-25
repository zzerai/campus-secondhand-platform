package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeOrder;

/**
 * 交易订单Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeOrderService 
{
    /**
     * 查询交易订单
     * 
     * @param orderId 交易订单主键
     * @return 交易订单
     */
    public TrTradeOrder selectTrTradeOrderByOrderId(Long orderId);

    /**
     * 查询交易订单列表
     * 
     * @param trTradeOrder 交易订单
     * @return 交易订单集合
     */
    public List<TrTradeOrder> selectTrTradeOrderList(TrTradeOrder trTradeOrder);

    /**
     * 新增交易订单
     * 
     * @param trTradeOrder 交易订单
     * @return 结果
     */
    public int insertTrTradeOrder(TrTradeOrder trTradeOrder);

    /**
     * 修改交易订单
     * 
     * @param trTradeOrder 交易订单
     * @return 结果
     */
    public int updateTrTradeOrder(TrTradeOrder trTradeOrder);

    /**
     * 批量删除交易订单
     * 
     * @param orderIds 需要删除的交易订单主键集合
     * @return 结果
     */
    public int deleteTrTradeOrderByOrderIds(Long[] orderIds);

    /**
     * 删除交易订单信息
     * 
     * @param orderId 交易订单主键
     * @return 结果
     */
    public int deleteTrTradeOrderByOrderId(Long orderId);
}
