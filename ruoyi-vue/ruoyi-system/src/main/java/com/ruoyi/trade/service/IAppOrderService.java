package com.ruoyi.trade.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppOrderCreateDto;
import com.ruoyi.trade.domain.vo.AppOrderPayResultVo;

/**
 * 移动端订单业务接口（含行锁、属主校验、状态流转）。
 * 与管理端 {@link ITrTradeOrderService}（纯 CRUD）共享同一 Entity 与 Mapper。
 */
public interface IAppOrderService
{
    /**
     * 移动端下单：行锁商品 → 校验已上架/非自购 → 写商品快照 → 订单置待确认 → 商品置已售出。
     *
     * @param dto      入参 DTO，仅含商品ID、交易方式、地点、预约时间、买家备注
     * @param buyerId  下单人（必须从 token 取，禁止从入参取）
     * @param createBy 下单人用户名（写 create_by 审计列）
     * @return 入库后的订单
     */
    public TrTradeOrder createOrder(AppOrderCreateDto dto, Long buyerId, String createBy);

    /** 订单详情（仅订单买卖双方可见）。 */
    public TrTradeOrder selectTradeOrderById(Long orderId, Long userId);

    /** "我买到的"订单列表。 */
    public List<TrTradeOrder> selectMyBuyOrderList(TrTradeOrder order);

    /** "我卖出的"订单列表。 */
    public List<TrTradeOrder> selectMySellOrderList(TrTradeOrder order);

    /** 取消订单（仅待支付且未支付）：行锁 → 校验属主与前置状态 → 订单置已取消 → 商品回退已上架。 */
    public int cancelOrder(Long orderId, Long userId, String updateBy);

    /** 买家申请退款（仅待收货）：行锁 → 校验买家 → 订单置退款中、退款子状态置申请中。 */
    public int applyRefund(Long orderId, Long buyerId, String reason, String updateBy);

    /** 卖家同意退款（退款申请中）：行锁 → 校验卖家 → 调支付宝退款 → 订单置已退款、商品回退已上架。 */
    public int agreeRefund(Long orderId, Long sellerId, String updateBy);

    /** 卖家拒绝退款（退款申请中）：行锁 → 校验卖家 → 订单退回待收货、退款子状态置拒绝（买家可发起争议）。 */
    public int rejectRefund(Long orderId, Long sellerId, String updateBy);

    /** 卖家主动退款（仅待收货）：行锁 → 校验卖家 → 调支付宝退款 → 订单置已退款、商品回退已上架。 */
    public int sellerRefund(Long orderId, Long sellerId, String reason, String updateBy);

    /** 争议仲裁退款（管理员判退款给买家）：订单须在争议中(5) → 调支付宝退款 → 订单置已退款（不恢复商品上架）。 */
    public int refundByArbitration(Long orderId, String updateBy);

    /** 发起支付宝沙盒支付，返回可渲染的支付表单 HTML。 */
    public String createAlipayPayForm(Long orderId, Long buyerId, String updateBy);

    /** 处理支付宝异步通知，验签并幂等更新订单支付状态。 */
    public boolean handleAlipayNotify(Map<String, String> params);

    /** 查询支付结果，供移动端轮询。 */
    public AppOrderPayResultVo getPayResult(Long orderId, Long userId);

    public int finishOrder(Long orderId, Long userId, String updateBy);

    /**
     * 系统自动取消待支付超时订单（行锁 + 条件更新，状态不符则静默跳过）。供定时任务逐单调用。
     *
     * @param orderId 订单ID
     * @return 是否实际取消
     */
    public boolean cancelTimeoutOrder(Long orderId);

    /**
     * 系统自动确认收货（待收货超时，行锁 + 条件更新，状态不符则静默跳过）。供定时任务逐单调用。
     * 等价于买家确认完成：订单置已完成、买卖双方各 +1 信用分。
     *
     * @param orderId 订单ID
     * @return 是否实际完成
     */
    public boolean autoConfirmOrder(Long orderId);
}
