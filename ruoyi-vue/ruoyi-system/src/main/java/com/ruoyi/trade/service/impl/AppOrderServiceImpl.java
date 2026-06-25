package com.ruoyi.trade.service.impl;

import java.util.List;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeFastpayRefundQueryRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.alipay.api.response.AlipayTradeFastpayRefundQueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.trade.config.AlipayProperties;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppOrderCreateDto;
import com.ruoyi.trade.domain.vo.AppOrderPayResultVo;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.IAppOrderService;
import com.ruoyi.trade.utils.CreditConstants;
import com.ruoyi.trade.utils.OrderConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 移动端订单业务实现（闲鱼模式：下单免确认 → 支付 → 待收货 → 确认完成；交割前可退款，完成后可争议）。
 * 状态枚举与流程见 {@code docs/订单流程改造方案-闲鱼模式.md}。
 */
@Service
public class AppOrderServiceImpl implements IAppOrderService
{
    private static final Logger log = LoggerFactory.getLogger(AppOrderServiceImpl.class);

    // 商品状态枚举（与 CLAUDE.md / 01_ddl.sql 一致）：1-已上架 4-已售出
    private static final String GOODS_ON_SALE = "1";
    private static final String GOODS_SOLD = "4";

    // 订单/退款/支付状态——委托 OrderConstants 单一事实源，下方仅为短名引用
    private static final String ORDER_PENDING_PAY = OrderConstants.ORDER_PENDING_PAY;
    private static final String ORDER_AWAITING_RECEIPT = OrderConstants.ORDER_AWAITING_RECEIPT;
    private static final String ORDER_FINISHED = OrderConstants.ORDER_FINISHED;
    private static final String ORDER_CANCELED = OrderConstants.ORDER_CANCELED;
    private static final String ORDER_DISPUTING = OrderConstants.ORDER_DISPUTING;
    private static final String ORDER_REFUNDING = OrderConstants.ORDER_REFUNDING;
    private static final String REFUND_APPLYING = OrderConstants.REFUND_APPLYING;
    private static final String REFUND_NONE = OrderConstants.REFUND_NONE;
    private static final String PAYMENT_PAYING = OrderConstants.PAYMENT_PAYING;
    private static final String PAYMENT_PAID = OrderConstants.PAYMENT_PAID;
    private static final String PAYMENT_CANCELED = OrderConstants.PAYMENT_CANCELED;
    private static final String TRADE_METHOD_OFFLINE = OrderConstants.TRADE_METHOD_OFFLINE;
    private static final String ALIPAY_NOTIFY_CLOSED = "TRADE_CLOSED";
    private static final String ALIPAY_NOTIFY_SUCCESS = "TRADE_SUCCESS";
    private static final String ALIPAY_NOTIFY_FINISHED = "TRADE_FINISHED";

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private AlipayProperties alipayProperties;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TrTradeOrder createOrder(AppOrderCreateDto dto, Long buyerId, String createBy)
    {
        if (dto == null || dto.getGoodsId() == null)
        {
            throw new ServiceException("商品ID不能为空");
        }
        if (buyerId == null)
        {
            throw new ServiceException("买家ID不能为空");
        }
        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsForUpdate(dto.getGoodsId());
        if (goods == null)
        {
            throw new ServiceException("商品不存在");
        }
        if (!GOODS_ON_SALE.equals(goods.getGoodsStatus()))
        {
            throw new ServiceException("商品已下架或已售出");
        }
        if (goods.getSellerId().equals(buyerId))
        {
            throw new ServiceException("不能购买自己发布的商品");
        }

        // 仅从 DTO 取用户可填字段；价格、状态、参与方等由服务端推导，禁止客户端注入
        TrTradeOrder order = new TrTradeOrder();
        order.setGoodsId(goods.getGoodsId());
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getSellerId());
        order.setOrderNo(buildOrderNo());
        order.setGoodsTitle(goods.getTitle());
        order.setTradePrice(goods.getPrice());
        // 行锁查询不连图片表，单独取图片快照（图片存于 tr_trade_goods_image）
        order.setGoodsImages(trTradeGoodsMapper.selectGoodsImageUrls(goods.getGoodsId()));
        // 闲鱼模式：下单即"待支付"，无需卖家确认
        order.setOrderStatus(ORDER_PENDING_PAY);
        order.setTradeMethod(StringUtils.isEmpty(dto.getTradeMethod()) ? TRADE_METHOD_OFFLINE : dto.getTradeMethod());
        order.setTradePlace(StringUtils.isEmpty(dto.getTradePlace()) ? goods.getTradePlace() : dto.getTradePlace());
        order.setAppointmentTime(dto.getAppointmentTime());
        order.setBuyerRemark(dto.getBuyerRemark());
        order.setCreateBy(createBy);
        order.setUpdateBy(createBy);
        // createTime / updateTime 由 AuditTimeFillInterceptor 兜底，不需要在此显式 set

        trTradeOrderMapper.insertTrTradeOrder(order);
        trTradeGoodsMapper.updateGoodsStatus(goods.getGoodsId(), GOODS_SOLD, createBy, DateUtils.getNowDate());
        return trTradeOrderMapper.selectTrTradeOrderByOrderId(order.getOrderId());
    }

    @Override
    public TrTradeOrder selectTradeOrderById(Long orderId, Long userId)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderId(orderId);
        if (order == null || !isOrderParticipant(order, userId))
        {
            throw new ServiceException("订单不存在或无权访问");
        }
        return order;
    }

    @Override
    public List<TrTradeOrder> selectMyBuyOrderList(TrTradeOrder order)
    {
        return trTradeOrderMapper.selectMyBuyOrderList(order);
    }

    @Override
    public List<TrTradeOrder> selectMySellOrderList(TrTradeOrder order)
    {
        return trTradeOrderMapper.selectMySellOrderList(order);
    }

    @Override
    @Transactional
    public int cancelOrder(Long orderId, Long userId, String updateBy)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        checkOperableOrder(order, userId);
        // 闲鱼模式：仅"待支付"且未支付的订单可取消；已支付订单只能走退款/争议流程
        if (!ORDER_PENDING_PAY.equals(order.getOrderStatus()))
        {
            throw new ServiceException("当前订单状态不能取消");
        }
        // 阻断"支付中即取消"的资金漏洞：若已发起支付宝下单，必须等回调落定后再取消，
        // 否则用户可能已经在支付宝侧扣款，但本地订单变为"已取消"，资金对账缺失。
        if (PAYMENT_PAYING.equals(order.getPaymentStatus()))
        {
            throw new ServiceException("订单支付中，请等待支付结果后再操作");
        }
        if (PAYMENT_PAID.equals(order.getPaymentStatus()))
        {
            throw new ServiceException("订单已支付，请通过退款/完成流程处理");
        }
        // 原子取消：order_status='4'、payment_status='4'、cancel_time 一次落库，
        // 防止支付回调晚到造成的状态错配；同步回写 cancel_time 保证统计趋势 SQL 可命中。
        int rows = trTradeOrderMapper.updateOrderCancelled(
                orderId, ORDER_CANCELED, PAYMENT_CANCELED, updateBy, DateUtils.getNowDate());
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        trTradeGoodsMapper.restoreSoldGoodsToOnSale(order.getGoodsId(), GOODS_SOLD, GOODS_ON_SALE);
        // 待支付阶段取消属正常行为（未成交），不扣信用分
        return rows;
    }

    @Override
    @Transactional
    public int applyRefund(Long orderId, Long buyerId, String reason, String updateBy)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || buyerId == null || !order.getBuyerId().equals(buyerId))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (!ORDER_AWAITING_RECEIPT.equals(order.getOrderStatus()))
        {
            throw new ServiceException("当前订单状态不能申请退款");
        }
        int rows = trTradeOrderMapper.updateOrderRefundApplying(orderId, reason, DateUtils.getNowDate(), updateBy);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional
    public int agreeRefund(Long orderId, Long sellerId, String updateBy)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || sellerId == null || !order.getSellerId().equals(sellerId))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (!ORDER_REFUNDING.equals(order.getOrderStatus()) || !REFUND_APPLYING.equals(order.getRefundStatus()))
        {
            throw new ServiceException("当前订单没有待处理的退款申请");
        }
        // 先抢占 refund_status 1→2（条件更新+行锁，幂等），再调支付宝退款
        int rows = trTradeOrderMapper.updateOrderRefundAgreed(orderId, updateBy);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        executeRefund(order, true, updateBy);
        return rows;
    }

    @Override
    @Transactional
    public int rejectRefund(Long orderId, Long sellerId, String updateBy)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || sellerId == null || !order.getSellerId().equals(sellerId))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (!ORDER_REFUNDING.equals(order.getOrderStatus()) || !REFUND_APPLYING.equals(order.getRefundStatus()))
        {
            throw new ServiceException("当前订单没有待处理的退款申请");
        }
        // 拒绝后订单退回"待收货"、退款子状态置"卖家拒绝"，买家可据此发起争议（平台介入）
        int rows = trTradeOrderMapper.updateOrderRefundRejected(orderId, updateBy);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    @Override
    @Transactional
    public int sellerRefund(Long orderId, Long sellerId, String reason, String updateBy)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || sellerId == null || !order.getSellerId().equals(sellerId))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (!ORDER_AWAITING_RECEIPT.equals(order.getOrderStatus()) || !REFUND_NONE.equals(nullToZero(order.getRefundStatus())))
        {
            throw new ServiceException("当前订单状态不能主动退款");
        }
        // 卖家主动退：直接进入"退款执行中"，再调支付宝退款
        int rows = trTradeOrderMapper.updateOrderSellerRefunding(orderId, reason, DateUtils.getNowDate(), updateBy);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        executeRefund(order, true, updateBy);
        return rows;
    }

    @Override
    @Transactional
    public String createAlipayPayForm(Long orderId, Long buyerId, String updateBy)
    {
        if (!alipayProperties.isConfigured())
        {
            throw new ServiceException("支付宝沙盒配置不完整，请先配置 ALIPAY_APP_ID、密钥和回调地址");
        }
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || buyerId == null || !order.getBuyerId().equals(buyerId))
        {
            throw new ServiceException("订单不存在或无权支付");
        }
        if (PAYMENT_PAID.equals(order.getPaymentStatus()))
        {
            throw new ServiceException("订单已支付，请勿重复支付");
        }
        if (!ORDER_PENDING_PAY.equals(order.getOrderStatus()))
        {
            throw new ServiceException("当前订单状态不能支付");
        }
        if (order.getTradePrice() == null || order.getTradePrice().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("订单金额异常，不能发起支付");
        }

        trTradeOrderMapper.updateOrderPaying(orderId, updateBy);
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(alipayProperties.getReturnUrl());
        request.setBizContent(buildPayBizContent(order));
        try
        {
            AlipayTradeWapPayResponse response = buildAlipayClient().pageExecute(request);
            if (!response.isSuccess() && StringUtils.isNotEmpty(response.getSubMsg()))
            {
                throw new ServiceException("支付宝下单失败：" + response.getSubMsg());
            }
            return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>"
                    + response.getBody()
                    + "</body></html>";
        }
        catch (AlipayApiException e)
        {
            throw new ServiceException("支付宝下单失败：" + e.getErrMsg());
        }
    }

    @Override
    @Transactional
    public boolean handleAlipayNotify(Map<String, String> params)
    {
        if (params == null || params.isEmpty() || !alipayProperties.isConfigured())
        {
            return false;
        }
        try
        {
            boolean verified = AlipaySignature.rsaCheckV1(params, alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(), alipayProperties.getSignType());
            if (!verified)
            {
                return false;
            }
        }
        catch (AlipayApiException e)
        {
            return false;
        }

        if (!alipayProperties.getAppId().equals(params.get("app_id")))
        {
            return false;
        }
        String tradeStatus = params.get("trade_status");
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderNo(params.get("out_trade_no"));
        if (order == null)
        {
            return false;
        }

        if (ALIPAY_NOTIFY_CLOSED.equals(tradeStatus))
        {
            if (PAYMENT_PAID.equals(order.getPaymentStatus()))
            {
                return true;
            }
            trTradeOrderMapper.updateOrderPaymentFailed(order.getOrderId(), "alipay_notify");
            return true;
        }
        if (!ALIPAY_NOTIFY_SUCCESS.equals(tradeStatus) && !ALIPAY_NOTIFY_FINISHED.equals(tradeStatus))
        {
            return true;
        }

        BigDecimal paymentAmount = parseAmount(params.get("total_amount"));
        if (paymentAmount == null || order.getTradePrice() == null
                || paymentAmount.compareTo(order.getTradePrice()) != 0)
        {
            return false;
        }
        if (PAYMENT_PAID.equals(order.getPaymentStatus()))
        {
            return true;
        }
        Date payTime = parseAlipayTime(params.get("gmt_payment"));
        if (payTime == null)
        {
            payTime = DateUtils.getNowDate();
        }
        return trTradeOrderMapper.updateOrderPaid(order.getOrderId(), params.get("trade_no"), paymentAmount,
                payTime, "alipay_notify") > 0;
    }

    @Override
    public AppOrderPayResultVo getPayResult(Long orderId, Long userId)
    {
        TrTradeOrder order = selectTradeOrderById(orderId, userId);
        // Proactively query Alipay when payment is in progress — callbacks (notify/return)
        // may be unreachable in sandbox behind NAT tunnels.
        if (PAYMENT_PAYING.equals(order.getPaymentStatus()) && alipayProperties.isConfigured())
        {
            log.info("订单 {} 支付中，主动向支付宝查询支付状态...", order.getOrderNo());
            queryAlipayTradeStatus(order);
            order = selectTradeOrderById(orderId, userId);
            log.info("订单 {} 查询后支付状态: {}", order.getOrderNo(), order.getPaymentStatus());
        }
        AppOrderPayResultVo result = new AppOrderPayResultVo();
        result.setOrderId(order.getOrderId());
        result.setOrderNo(order.getOrderNo());
        result.setOrderStatus(order.getOrderStatus());
        result.setPaymentStatus(order.getPaymentStatus());
        result.setAlipayTradeNo(order.getAlipayTradeNo());
        result.setPaymentAmount(order.getPaymentAmount());
        return result;
    }

    @Override
    @Transactional
    public int finishOrder(Long orderId, Long userId, String updateBy)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        // 业务规则：由买家在线下交割后点击完成（与 DB 字段 complete_time "买家确认完成时间" 一致）。
        // 卖家提前点完成存在骗保风险，故只允许买家。
        if (order == null || userId == null || !order.getBuyerId().equals(userId))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
        if (!ORDER_AWAITING_RECEIPT.equals(order.getOrderStatus()))
        {
            throw new ServiceException("当前订单状态不能完成");
        }
        // 同步回写 complete_time，否则 selectDailyCompletedOrderCount 的 complete_time is not null 过滤会漏统计
        int rows = trTradeOrderMapper.updateOrderStatus(
                orderId, ORDER_FINISHED, updateBy, null, null, DateUtils.getNowDate(), null);
        // 交易完成 → 买卖双方各 +1 信用分（uk_event 按"用户+事件"去重，重复完成不会重复加）
        if (rows > 0)
        {
            eventPublisher.publishEvent(new CreditChangeEvent(order.getBuyerId(),
                    CreditConstants.TYPE_ORDER_COMPLETE, CreditConstants.DELTA_ORDER_COMPLETE,
                    CreditConstants.BIZ_ORDER, orderId, "交易完成（买家）"));
            eventPublisher.publishEvent(new CreditChangeEvent(order.getSellerId(),
                    CreditConstants.TYPE_ORDER_COMPLETE, CreditConstants.DELTA_ORDER_COMPLETE,
                    CreditConstants.BIZ_ORDER, orderId, "交易完成（卖家）"));
        }
        return rows;
    }

    @Override
    @Transactional
    public boolean cancelTimeoutOrder(Long orderId)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || !ORDER_PENDING_PAY.equals(order.getOrderStatus()))
        {
            return false;
        }
        // 支付中/已支付不取消（行锁内复检，避免与支付回调竞态）
        if (PAYMENT_PAYING.equals(order.getPaymentStatus()) || PAYMENT_PAID.equals(order.getPaymentStatus()))
        {
            return false;
        }
        int rows = trTradeOrderMapper.updateOrderCancelled(
                orderId, ORDER_CANCELED, PAYMENT_CANCELED, order.getCreateBy(), DateUtils.getNowDate());
        if (rows == 0)
        {
            return false;
        }
        trTradeGoodsMapper.restoreSoldGoodsToOnSale(order.getGoodsId(), GOODS_SOLD, GOODS_ON_SALE);
        log.info("待支付超时自动取消订单 {}", order.getOrderNo());
        return true;
    }

    @Override
    @Transactional
    public boolean autoConfirmOrder(Long orderId)
    {
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null || !ORDER_AWAITING_RECEIPT.equals(order.getOrderStatus()))
        {
            return false;
        }
        // 退款流程进行中（理论上状态已非 2，这里再防一层）不自动确认
        if (!REFUND_NONE.equals(nullToZero(order.getRefundStatus())))
        {
            return false;
        }
        int rows = trTradeOrderMapper.updateOrderStatus(
                orderId, ORDER_FINISHED, "system", null, null, DateUtils.getNowDate(), null);
        if (rows == 0)
        {
            return false;
        }
        // 与买家手动确认一致：买卖双方各 +1 信用分（uk_event 去重）
        eventPublisher.publishEvent(new CreditChangeEvent(order.getBuyerId(),
                CreditConstants.TYPE_ORDER_COMPLETE, CreditConstants.DELTA_ORDER_COMPLETE,
                CreditConstants.BIZ_ORDER, orderId, "超时自动确认完成（买家）"));
        eventPublisher.publishEvent(new CreditChangeEvent(order.getSellerId(),
                CreditConstants.TYPE_ORDER_COMPLETE, CreditConstants.DELTA_ORDER_COMPLETE,
                CreditConstants.BIZ_ORDER, orderId, "超时自动确认完成（卖家）"));
        log.info("待收货超时自动确认完成订单 {}", order.getOrderNo());
        return true;
    }

    /**
     * 调用支付宝退款并落库（整单全额退，out_request_no=order_no 保证幂等）。
     * 调用前 refund_status 须已抢占为"执行中(2)"。退款成功后置订单"已退款"，按需恢复商品上架。
     *
     * @param order        已行锁的订单
     * @param restoreGoods 是否把商品恢复为"已上架"（交割前退款=true；争议退款已交割=false）
     * @param updateBy     操作人
     */
    private void executeRefund(TrTradeOrder order, boolean restoreGoods, String updateBy)
    {
        BigDecimal refundAmount = callAlipayRefund(order);
        int rows = trTradeOrderMapper.updateOrderRefunded(order.getOrderId(), order.getOrderNo(),
                refundAmount, DateUtils.getNowDate(), updateBy);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        if (restoreGoods)
        {
            trTradeGoodsMapper.restoreSoldGoodsToOnSale(order.getGoodsId(), GOODS_SOLD, GOODS_ON_SALE);
        }
    }

    @Override
    @Transactional
    public int refundByArbitration(Long orderId, String updateBy)
    {
        // 管理员仲裁判退款给买家：订单须在"争议中(5)"，退款后不恢复商品上架（已交割）
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderForUpdate(orderId);
        if (order == null)
        {
            throw new ServiceException("订单不存在");
        }
        if (!ORDER_DISPUTING.equals(order.getOrderStatus()))
        {
            throw new ServiceException("订单不在争议中，无法退款");
        }
        BigDecimal refundAmount = callAlipayRefund(order);
        int rows = trTradeOrderMapper.updateOrderDisputeRefunded(orderId, order.getOrderNo(),
                refundAmount, DateUtils.getNowDate(), updateBy);
        if (rows == 0)
        {
            throw new ServiceException("订单状态已变更，请刷新后重试");
        }
        return rows;
    }

    /**
     * 调用支付宝退款（整单全额退，out_request_no=order_no 保证幂等）。退款失败用退款查询兜底；
     * 仅负责调用支付宝并校验结果，不改订单状态。
     *
     * @param order 订单（含 orderNo / paymentAmount / tradePrice）
     * @return 实际退款金额（两位小数）
     */
    private BigDecimal callAlipayRefund(TrTradeOrder order)
    {
        if (!alipayProperties.isConfigured())
        {
            throw new ServiceException("支付宝沙盒配置不完整，无法退款");
        }
        BigDecimal refundAmount = order.getPaymentAmount() != null ? order.getPaymentAmount() : order.getTradePrice();
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("退款金额异常，无法退款");
        }
        refundAmount = refundAmount.setScale(2);
        try
        {
            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent("{"
                    + "\"out_trade_no\":\"" + escapeJson(order.getOrderNo()) + "\","
                    + "\"refund_amount\":\"" + refundAmount.toPlainString() + "\","
                    + "\"out_request_no\":\"" + escapeJson(order.getOrderNo()) + "\""
                    + "}");
            AlipayTradeRefundResponse response = buildAlipayClient().execute(request);
            // 失败时用退款查询兜底：若实际已退（重复请求/幂等），视为成功
            if (!response.isSuccess() && !queryAlipayRefunded(order))
            {
                String msg = StringUtils.isNotEmpty(response.getSubMsg()) ? response.getSubMsg() : response.getMsg();
                throw new ServiceException("支付宝退款失败：" + msg);
            }
        }
        catch (AlipayApiException e)
        {
            throw new ServiceException("支付宝退款异常：" + e.getErrMsg());
        }
        return refundAmount;
    }

    /** 退款查询兜底：判断该订单是否已在支付宝侧完成退款。 */
    private boolean queryAlipayRefunded(TrTradeOrder order)
    {
        try
        {
            AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
            request.setBizContent("{"
                    + "\"out_trade_no\":\"" + escapeJson(order.getOrderNo()) + "\","
                    + "\"out_request_no\":\"" + escapeJson(order.getOrderNo()) + "\""
                    + "}");
            AlipayTradeFastpayRefundQueryResponse response = buildAlipayClient().execute(request);
            return response.isSuccess() && StringUtils.isNotEmpty(response.getRefundAmount());
        }
        catch (AlipayApiException e)
        {
            log.warn("支付宝退款查询异常: {}", e.getErrMsg());
            return false;
        }
    }

    private void checkOperableOrder(TrTradeOrder order, Long userId)
    {
        if (order == null || !isOrderParticipant(order, userId))
        {
            throw new ServiceException("订单不存在或无权操作");
        }
    }

    private boolean isOrderParticipant(TrTradeOrder order, Long userId)
    {
        return order.getBuyerId().equals(userId) || order.getSellerId().equals(userId);
    }

    private String nullToZero(String refundStatus)
    {
        return StringUtils.isEmpty(refundStatus) ? REFUND_NONE : refundStatus;
    }

    private AlipayClient buildAlipayClient()
    {
        return new DefaultAlipayClient(alipayProperties.getGatewayUrl(), alipayProperties.getAppId(),
                alipayProperties.getMerchantPrivateKey(), "json", alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(), alipayProperties.getSignType());
    }

    private String buildPayBizContent(TrTradeOrder order)
    {
        String subject = StringUtils.isEmpty(order.getGoodsTitle()) ? "校园二手交易订单" : order.getGoodsTitle();
        return "{"
                + "\"out_trade_no\":\"" + escapeJson(order.getOrderNo()) + "\","
                + "\"total_amount\":\"" + order.getTradePrice().setScale(2).toPlainString() + "\","
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"product_code\":\"QUICK_WAP_WAY\""
                + "}";
    }

    /**
     * Proactively query Alipay for trade status and update the order if paid.
     * This is the fallback when async notify / sync return are unreachable.
     */
    private void queryAlipayTradeStatus(TrTradeOrder order)
    {
        try
        {
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            request.setBizContent("{\"out_trade_no\":\"" + escapeJson(order.getOrderNo()) + "\"}");
            AlipayTradeQueryResponse response = buildAlipayClient().execute(request);
            log.info("支付宝查询响应: success={}, tradeStatus={}, tradeNo={}, totalAmount={}",
                    response.isSuccess(), response.getTradeStatus(), response.getTradeNo(),
                    response.getTotalAmount());
            if (!response.isSuccess())
            {
                log.warn("支付宝查询失败: code={}, subCode={}, subMsg={}",
                        response.getCode(), response.getSubCode(), response.getSubMsg());
                return;
            }
            if (!"TRADE_SUCCESS".equals(response.getTradeStatus())
                    && !"TRADE_FINISHED".equals(response.getTradeStatus()))
            {
                return;
            }
            BigDecimal amount = parseAmount(response.getTotalAmount());
            if (amount == null || order.getTradePrice() == null
                    || amount.compareTo(order.getTradePrice()) != 0)
            {
                return;
            }
            if (PAYMENT_PAID.equals(order.getPaymentStatus()))
            {
                return;
            }
            Date payTime = response.getSendPayDate();
            if (payTime == null)
            {
                payTime = DateUtils.getNowDate();
            }
            trTradeOrderMapper.updateOrderPaid(order.getOrderId(), response.getTradeNo(),
                    amount, payTime, "alipay_query");
            log.info("订单 {} 通过主动查询确认支付成功，tradeNo={}", order.getOrderNo(), response.getTradeNo());
        }
        catch (Exception e)
        {
            log.error("支付宝主动查询异常: {}", e.getMessage());
        }
    }

    private BigDecimal parseAmount(String amount)
    {
        if (StringUtils.isEmpty(amount))
        {
            return null;
        }
        try
        {
            return new BigDecimal(amount).setScale(2);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private Date parseAlipayTime(String value)
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        try
        {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(value);
        }
        catch (ParseException e)
        {
            return null;
        }
    }

    private String escapeJson(String value)
    {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String buildOrderNo()
    {
        return "CO" + DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 8);
    }
}
