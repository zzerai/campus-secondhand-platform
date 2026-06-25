package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.service.ITrTradeOrderService;

/**
 * 交易订单Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeOrderServiceImpl implements ITrTradeOrderService 
{
    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    /**
     * 查询交易订单
     * 
     * @param orderId 交易订单主键
     * @return 交易订单
     */
    @Override
    public TrTradeOrder selectTrTradeOrderByOrderId(Long orderId)
    {
        return trTradeOrderMapper.selectTrTradeOrderByOrderId(orderId);
    }

    /**
     * 查询交易订单列表
     * 
     * @param trTradeOrder 交易订单
     * @return 交易订单
     */
    @Override
    public List<TrTradeOrder> selectTrTradeOrderList(TrTradeOrder trTradeOrder)
    {
        return trTradeOrderMapper.selectTrTradeOrderList(trTradeOrder);
    }

    /**
     * 新增交易订单
     *
     * @param trTradeOrder 交易订单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTrTradeOrder(TrTradeOrder trTradeOrder)
    {
        String username = SecurityUtils.getUsername();
        trTradeOrder.setCreateBy(username);
        trTradeOrder.setCreateTime(DateUtils.getNowDate());
        trTradeOrder.setUpdateBy(username);
        trTradeOrder.setUpdateTime(DateUtils.getNowDate());
        return trTradeOrderMapper.insertTrTradeOrder(trTradeOrder);
    }

    /**
     * 修改交易订单（管理端通用 edit）
     *
     * <p>通用 edit 不允许直接改状态机/资金/参与方字段：
     * orderStatus / paymentStatus / paymentAmount / alipayTradeNo /
     * payTime / paymentTime / completeTime / cancelTime / cancelReason / buyerId / sellerId / goodsId / orderNo。
     * 这些字段必须由对应的业务流程（卖家确认、支付回调、买家完成、争议、取消）回写。
     * 服务端强制 null 化后，MyBatis 动态 SET 自动跳过这些列，已有值保持不变。</p>
     *
     * <p>管理员可改的"安全字段"包括：tradePlace / appointmentTime / buyerRemark / sellerRemark / remark。</p>
     *
     * @param trTradeOrder 交易订单
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTrTradeOrder(TrTradeOrder trTradeOrder)
    {
        if (trTradeOrder.getOrderId() == null)
        {
            throw new ServiceException("修改订单失败，订单ID不能为空");
        }
        // 状态机字段：必须走业务流程
        trTradeOrder.setOrderStatus(null);
        trTradeOrder.setPaymentStatus(null);
        // 资金/支付字段：禁止管理员篡改
        trTradeOrder.setPaymentAmount(null);
        trTradeOrder.setAlipayTradeNo(null);
        trTradeOrder.setPayTime(null);
        trTradeOrder.setPaymentTime(null);
        trTradeOrder.setConfirmTime(null);
        trTradeOrder.setCompleteTime(null);
        trTradeOrder.setCancelTime(null);
        trTradeOrder.setCancelReason(null);
        // 主键/关联键：不允许 edit 改归属
        trTradeOrder.setOrderNo(null);
        trTradeOrder.setBuyerId(null);
        trTradeOrder.setSellerId(null);
        trTradeOrder.setGoodsId(null);
        // 价格不可改：会破坏统计 + 支付一致性
        trTradeOrder.setTradePrice(null);

        trTradeOrder.setUpdateBy(SecurityUtils.getUsername());
        trTradeOrder.setUpdateTime(DateUtils.getNowDate());
        return trTradeOrderMapper.updateTrTradeOrder(trTradeOrder);
    }

    /**
     * 批量删除交易订单
     *
     * @param orderIds 需要删除的交易订单主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTrTradeOrderByOrderIds(Long[] orderIds)
    {
        return trTradeOrderMapper.deleteTrTradeOrderByOrderIds(orderIds);
    }

    /**
     * 删除交易订单信息
     *
     * @param orderId 交易订单主键
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTrTradeOrderByOrderId(Long orderId)
    {
        return trTradeOrderMapper.deleteTrTradeOrderByOrderId(orderId);
    }
}
