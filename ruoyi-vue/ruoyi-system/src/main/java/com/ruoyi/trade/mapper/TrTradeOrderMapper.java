package com.ruoyi.trade.mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsDailyCountVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsDailyPaymentVo;

/**
 * 交易订单Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeOrderMapper 
{
    /**
     * 查询交易订单
     *
     * @param orderId 交易订单主键
     * @return 交易订单
     */
    public TrTradeOrder selectTrTradeOrderByOrderId(Long orderId);

    /**
     * Query order by order no.
     *
     * @param orderNo order no
     * @return order
     */
    public TrTradeOrder selectTrTradeOrderByOrderNo(String orderNo);

    /**
     * 行锁查询订单（仅锁本行，不连表），供下单/取消/完成等并发场景使用。
     *
     * @param orderId 订单ID
     * @return 订单
     */
    public TrTradeOrder selectTrTradeOrderForUpdate(Long orderId);

    /**
     * 查询交易订单列表
     *
     * @param trTradeOrder 交易订单
     * @return 交易订单集合
     */
    public List<TrTradeOrder> selectTrTradeOrderList(TrTradeOrder trTradeOrder);

    /**
     * 移动端"我买到的"订单列表（按 buyerId + 可选 orderStatus 过滤，连表查买卖双方学号/昵称）。
     *
     * @param trTradeOrder 包含 buyerId 与可选 orderStatus
     * @return 订单集合
     */
    public List<TrTradeOrder> selectMyBuyOrderList(TrTradeOrder trTradeOrder);

    /**
     * 移动端"我卖出的"订单列表（按 sellerId + 可选 orderStatus 过滤）。
     *
     * @param trTradeOrder 包含 sellerId 与可选 orderStatus
     * @return 订单集合
     */
    public List<TrTradeOrder> selectMySellOrderList(TrTradeOrder trTradeOrder);

    /**
     * 仅更新订单状态（不带状态机校验，调用方需自行加行锁或前置判断）。
     *
     * <p>cancelTime / completeTime / payTime 任一非空即同步回写，方便取消/完成/支付链路一次性
     * 落库对应时间戳，避免后续统计 SQL 因这些列为 NULL 而漏统计。传 null 时该列保持不变。</p>
     *
     * @param orderId 订单ID
     * @param orderStatus 新订单状态
     * @param updateBy 更新人
     * @param cancelTime 取消时间（取消时传，其它场景传 null）
     * @param completeTime 完成时间（完成时传，其它场景传 null）
     * @param payTime 支付时间（支付时传，其它场景传 null）
     * @return 影响行数
     */
    public int updateOrderStatus(@Param("orderId") Long orderId,
            @Param("orderStatus") String orderStatus,
            @Param("updateBy") String updateBy,
            @Param("confirmTime") Date confirmTime,
            @Param("cancelTime") Date cancelTime,
            @Param("completeTime") Date completeTime,
            @Param("payTime") Date payTime);

    /**
     * Mark an awaiting-payment order as paying.
     *
     * @param orderId order id
     * @param updateBy update user
     * @return affected rows
     */
    public int updateOrderPaying(@Param("orderId") Long orderId, @Param("updateBy") String updateBy);

    /**
     * 原子取消订单：同时回写 order_status、payment_status 与 cancel_time。
     *
     * <p>where 条件强制 order_status ∈ (0,1) 且 payment_status ∉ (1,2)，
     * 与移动端取消接口的 Java 前置校验形成双重保险：避免"支付中/已支付"订单被取消
     * 导致钱已扣但订单丢失。即便存在并发支付回调，行锁与条件过滤亦能阻止
     * 状态错配。</p>
     *
     * @param orderId order id
     * @param orderStatus 目标订单状态（取消应传 '4'）
     * @param paymentStatus 目标支付状态（取消应传 '4' 已取消/退款）
     * @param updateBy 更新人
     * @param cancelTime 取消时间
     * @return 影响行数（0 表示状态条件未满足，应作为业务异常处理）
     */
    public int updateOrderCancelled(@Param("orderId") Long orderId,
            @Param("orderStatus") String orderStatus,
            @Param("paymentStatus") String paymentStatus,
            @Param("updateBy") String updateBy,
            @Param("cancelTime") java.util.Date cancelTime);

    /**
     * Mark order paid after Alipay notification verification.
     *
     * @param orderId order id
     * @param alipayTradeNo Alipay trade no
     * @param paymentAmount paid amount
     * @param payTime payment time
     * @param updateBy update user
     * @return affected rows
     */
    public int updateOrderPaid(@Param("orderId") Long orderId,
            @Param("alipayTradeNo") String alipayTradeNo,
            @Param("paymentAmount") BigDecimal paymentAmount,
            @Param("payTime") Date payTime,
            @Param("updateBy") String updateBy);

    /**
     * Mark order payment failed or closed after Alipay notification verification.
     *
     * @param orderId order id
     * @param updateBy update user
     * @return affected rows
     */
    public int updateOrderPaymentFailed(@Param("orderId") Long orderId, @Param("updateBy") String updateBy);

    /**
     * 买家申请退款：待收货(2)+无退款(0) → 退款中(6)+申请中(1)。
     *
     * @param orderId 订单ID
     * @param refundReason 退款原因
     * @param applyTime 申请时间
     * @param updateBy 更新人
     * @return 影响行数（0 表示状态条件未满足）
     */
    public int updateOrderRefundApplying(@Param("orderId") Long orderId,
            @Param("refundReason") String refundReason,
            @Param("applyTime") Date applyTime,
            @Param("updateBy") String updateBy);

    /**
     * 卖家同意退款：退款中(6)+申请中(1) → 已同意执行中(2)。
     *
     * @param orderId 订单ID
     * @param updateBy 更新人
     * @return 影响行数
     */
    public int updateOrderRefundAgreed(@Param("orderId") Long orderId, @Param("updateBy") String updateBy);

    /**
     * 卖家拒绝退款：退款中(6)+申请中(1) → 退回待收货(2)+拒绝(4)。
     *
     * @param orderId 订单ID
     * @param updateBy 更新人
     * @return 影响行数
     */
    public int updateOrderRefundRejected(@Param("orderId") Long orderId, @Param("updateBy") String updateBy);

    /**
     * 卖家主动退款：待收货(2)+无退款(0) → 退款中(6)+执行中(2)。
     *
     * @param orderId 订单ID
     * @param refundReason 退款原因
     * @param applyTime 申请时间
     * @param updateBy 更新人
     * @return 影响行数
     */
    public int updateOrderSellerRefunding(@Param("orderId") Long orderId,
            @Param("refundReason") String refundReason,
            @Param("applyTime") Date applyTime,
            @Param("updateBy") String updateBy);

    /**
     * 退款成功落库：退款中(6)+执行中(2) → 已退款(7)，支付状态置已退款(4)。
     *
     * @param orderId 订单ID
     * @param alipayRefundNo 支付宝退款流水号
     * @param refundAmount 退款金额
     * @param refundTime 退款成功时间
     * @param updateBy 更新人
     * @return 影响行数
     */
    public int updateOrderRefunded(@Param("orderId") Long orderId,
            @Param("alipayRefundNo") String alipayRefundNo,
            @Param("refundAmount") BigDecimal refundAmount,
            @Param("refundTime") Date refundTime,
            @Param("updateBy") String updateBy);

    /**
     * 发起争议时将订单转入"争议中(5)"。允许两种来源状态（闲鱼模式）：
     * ① 已完成(3)；② 待收货(2) 且退款被卖家拒绝(refund_status=4)。
     * 条件更新 + 行锁保证并发/重复提交只有一个能命中。
     *
     * @param orderId 订单ID
     * @return 影响行数（0 表示状态不符或已删除）
     */
    public int updateOrderToDisputing(@Param("orderId") Long orderId);

    /**
     * 争议仲裁退款落库：争议中(5) → 已退款(7)，支付状态置已退款(4)、退款子状态置已退款(3)。
     * 不恢复商品上架（已交割）。
     *
     * @param orderId 订单ID
     * @param alipayRefundNo 支付宝退款流水号
     * @param refundAmount 退款金额
     * @param refundTime 退款成功时间
     * @param updateBy 更新人
     * @return 影响行数
     */
    public int updateOrderDisputeRefunded(@Param("orderId") Long orderId,
            @Param("alipayRefundNo") String alipayRefundNo,
            @Param("refundAmount") BigDecimal refundAmount,
            @Param("refundTime") Date refundTime,
            @Param("updateBy") String updateBy);

    /**
     * 扫描待支付超时订单ID：order_status=0 且未进入支付中/已支付，且创建时间早于 before。
     * 供定时任务自动取消使用。
     *
     * @param before 创建时间下限（早于此时间视为超时）
     * @return 订单ID列表
     */
    public List<Long> selectPayTimeoutOrderIds(@Param("before") Date before);

    /**
     * 扫描待收货超时订单ID：order_status=2 且无退款，且支付成功时间早于 before。
     * 供定时任务自动确认收货使用。
     *
     * @param before 支付成功时间下限（早于此时间视为超时）
     * @return 订单ID列表
     */
    public List<Long> selectAutoConfirmOrderIds(@Param("before") Date before);

    /**
     * Query total order count.
     *
     * @return total count
     */
    public Long selectOrderTotalCount();

    /**
     * Query today's new order count.
     *
     * @return today count
     */
    public Long selectTodayNewOrderCount();

    /**
     * Query completed order count.
     *
     * @return completed count
     */
    public Long selectCompletedOrderCount();

    /**
     * Query total trade amount for completed orders.
     *
     * @return total amount
     */
    public BigDecimal selectCompletedTradeAmount();

    /**
     * Query daily new orders.
     *
     * @param startDate start date time
     * @param endDate end date time
     * @return daily counts
     */
    public List<TrTradeStatisticsDailyCountVo> selectDailyNewOrderCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Query daily completed orders.
     *
     * @param startDate start date time
     * @param endDate end date time
     * @return daily counts
     */
    public List<TrTradeStatisticsDailyCountVo> selectDailyCompletedOrderCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Query daily cancelled orders.
     *
     * @param startDate start date time
     * @param endDate end date time
     * @return daily counts
     */
    public List<TrTradeStatisticsDailyCountVo> selectDailyCancelledOrderCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * Query daily successful payments.
     *
     * @param startDate start date time
     * @param endDate end date time
     * @return daily payment stats
     */
    public List<TrTradeStatisticsDailyPaymentVo> selectDailyPaymentTrend(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

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
     * 统计某商品的活跃订单数（order_status 在 0/1/2/5，即未完成且未取消的订单）。
     * 用于管理端删除商品前的关联校验，避免悬空订单。
     *
     * @param goodsId 商品ID
     * @return 活跃订单数
     */
    public int countActiveOrdersByGoodsId(@Param("goodsId") Long goodsId);

    /**
     * 条件更新订单状态：仅当订单存在、未逻辑删除且当前状态等于期望状态时才更新。
     * 借助 InnoDB 行锁可实现并发安全的状态流转（如发起争议时 已完成→争议中）。
     *
     * @param orderId 订单ID
     * @param expectStatus 期望的当前状态
     * @param targetStatus 目标状态
     * @return 受影响行数（0 表示订单不存在、已删除或状态不匹配）
     */
    public int updateOrderStatusConditional(@Param("orderId") Long orderId,
            @Param("expectStatus") String expectStatus,
            @Param("targetStatus") String targetStatus);

    /**
     * 删除交易订单
     * 
     * @param orderId 交易订单主键
     * @return 结果
     */
    public int deleteTrTradeOrderByOrderId(Long orderId);

    /**
     * 批量删除交易订单
     * 
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeOrderByOrderIds(Long[] orderIds);
}
