package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.config.AlipayProperties;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppOrderCreateDto;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;

/**
 * 移动端订单业务单测（闲鱼模式：下单免确认 / 退款 / 完成）。
 */
@ExtendWith(MockitoExtension.class)
class AppOrderServiceImplTest
{
    @Mock
    private TrTradeOrderMapper trTradeOrderMapper;

    @Mock
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Mock
    private AlipayProperties alipayProperties;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AppOrderServiceImpl appOrderService;

    @Test
    void createOrderShouldLockGoodsAndMarkSold()
    {
        TrTradeGoods goods = onSaleGoods();
        AppOrderCreateDto dto = new AppOrderCreateDto();
        dto.setGoodsId(20L);

        TrTradeOrder saved = new TrTradeOrder();
        saved.setOrderId(100L);
        saved.setGoodsId(20L);
        saved.setBuyerId(5L);

        when(trTradeGoodsMapper.selectTrTradeGoodsForUpdate(20L)).thenReturn(goods);
        ArgumentCaptor<TrTradeOrder> captor = ArgumentCaptor.forClass(TrTradeOrder.class);
        when(trTradeOrderMapper.insertTrTradeOrder(captor.capture())).thenAnswer(invocation -> {
            TrTradeOrder order = invocation.getArgument(0);
            order.setOrderId(100L);
            return 1;
        });
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(saved);

        TrTradeOrder result = appOrderService.createOrder(dto, 5L, "buyer");

        Assertions.assertSame(saved, result);
        TrTradeOrder inserted = captor.getValue();
        Assertions.assertNotNull(inserted.getOrderNo());
        Assertions.assertEquals(5L, inserted.getBuyerId());
        Assertions.assertEquals(3L, inserted.getSellerId());
        // 闲鱼模式：下单即"待支付(0)"，无需卖家确认
        Assertions.assertEquals("0", inserted.getOrderStatus());
        Assertions.assertEquals("offline", inserted.getTradeMethod());
        Assertions.assertEquals("buyer", inserted.getCreateBy());
        verify(trTradeGoodsMapper).updateGoodsStatus(eq(20L), eq("4"), eq("buyer"), any(Date.class));
    }

    @Test
    void createOrderShouldIgnoreClientInjectedSensitiveFields()
    {
        // DTO 不允许传 paymentStatus 等敏感字段 —— 这条用例从 API 边界证明该约束：
        // DTO 上根本没有这些 setter，所以即使前端塞了 JSON 字段，反序列化也不会落到实体上。
        AppOrderCreateDto dto = new AppOrderCreateDto();
        dto.setGoodsId(20L);
        dto.setBuyerRemark("尽快交易");

        when(trTradeGoodsMapper.selectTrTradeGoodsForUpdate(20L)).thenReturn(onSaleGoods());
        ArgumentCaptor<TrTradeOrder> captor = ArgumentCaptor.forClass(TrTradeOrder.class);
        when(trTradeOrderMapper.insertTrTradeOrder(captor.capture())).thenReturn(1);
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(any())).thenReturn(new TrTradeOrder());

        appOrderService.createOrder(dto, 5L, "buyer");

        TrTradeOrder inserted = captor.getValue();
        Assertions.assertNull(inserted.getPaymentStatus());
        Assertions.assertNull(inserted.getPaymentAmount());
        Assertions.assertNull(inserted.getAlipayTradeNo());
        Assertions.assertNull(inserted.getRefundStatus());
        Assertions.assertNull(inserted.getDelFlag());
        Assertions.assertNull(inserted.getCompleteTime());
        Assertions.assertNull(inserted.getCancelTime());
        Assertions.assertEquals("尽快交易", inserted.getBuyerRemark());
    }

    @Test
    void createOrderShouldRejectBuyingOwnGoods()
    {
        AppOrderCreateDto dto = new AppOrderCreateDto();
        dto.setGoodsId(20L);
        when(trTradeGoodsMapper.selectTrTradeGoodsForUpdate(20L)).thenReturn(onSaleGoods());

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.createOrder(dto, 3L, "owner"));

        Assertions.assertTrue(ex.getMessage().contains("不能购买自己发布的商品"));
        verify(trTradeOrderMapper, never()).insertTrTradeOrder(any(TrTradeOrder.class));
    }

    @Test
    void cancelOrderShouldOnlyAllowPendingPayAndNotPenalize()
    {
        // 闲鱼模式：仅"待支付(0)"可取消，且未成交不扣信用分
        TrTradeOrder order = pendingPayOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderCancelled(eq(100L), eq("4"), eq("4"), eq("buyer"),
                any(Date.class))).thenReturn(1);

        int rows = appOrderService.cancelOrder(100L, 5L, "buyer");

        Assertions.assertEquals(1, rows);
        verify(trTradeOrderMapper).updateOrderCancelled(eq(100L), eq("4"), eq("4"), eq("buyer"),
                any(Date.class));
        verify(trTradeGoodsMapper).restoreSoldGoodsToOnSale(20L, "4", "1");
        // 待支付取消不扣分，不应发信用事件
        verify(eventPublisher, never()).publishEvent(any(CreditChangeEvent.class));
    }

    @Test
    void cancelOrderShouldRejectWhenAwaitingReceipt()
    {
        // 已支付进入"待收货(2)"后不能再取消，只能走退款/争议
        TrTradeOrder order = pendingPayOrder();
        order.setOrderStatus("2");
        order.setPaymentStatus("2");
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.cancelOrder(100L, 5L, "buyer"));

        Assertions.assertTrue(ex.getMessage().contains("当前订单状态不能取消"));
        verify(trTradeOrderMapper, never()).updateOrderCancelled(any(), any(), any(), any(), any());
    }

    @Test
    void cancelOrderShouldRejectWhenPaymentInProgress()
    {
        // 已发起支付宝下单（payment_status='1' 支付中）时，必须等回调落定才能取消，
        // 否则可能出现"用户已扣款但订单被取消"的资金漏洞。
        TrTradeOrder order = pendingPayOrder();
        order.setPaymentStatus("1");
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.cancelOrder(100L, 5L, "buyer"));

        Assertions.assertTrue(ex.getMessage().contains("支付中"));
        verify(trTradeOrderMapper, never()).updateOrderCancelled(any(), any(), any(), any(), any());
        verify(trTradeGoodsMapper, never()).restoreSoldGoodsToOnSale(any(), any(), any());
    }

    @Test
    void cancelOrderShouldRejectNonParticipant()
    {
        TrTradeOrder order = pendingPayOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.cancelOrder(100L, 99L, "other"));

        Assertions.assertTrue(ex.getMessage().contains("订单不存在或无权操作"));
        verify(trTradeOrderMapper, never()).updateOrderCancelled(any(), any(), any(), any(), any());
    }

    @Test
    void applyRefundShouldRequireBuyerAndAwaitingReceipt()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderRefundApplying(eq(100L), eq("不想要了"),
                any(Date.class), eq("buyer"))).thenReturn(1);

        int rows = appOrderService.applyRefund(100L, 5L, "不想要了", "buyer");

        Assertions.assertEquals(1, rows);
        verify(trTradeOrderMapper).updateOrderRefundApplying(eq(100L), eq("不想要了"),
                any(Date.class), eq("buyer"));
    }

    @Test
    void applyRefundShouldRejectSeller()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.applyRefund(100L, 3L, "x", "seller"));

        Assertions.assertTrue(ex.getMessage().contains("无权操作"));
        verify(trTradeOrderMapper, never()).updateOrderRefundApplying(any(), any(), any(), any());
    }

    @Test
    void applyRefundShouldRejectWrongStatus()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        order.setOrderStatus("3"); // 已完成不能申请退款（应走争议）
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.applyRefund(100L, 5L, "x", "buyer"));

        Assertions.assertTrue(ex.getMessage().contains("不能申请退款"));
        verify(trTradeOrderMapper, never()).updateOrderRefundApplying(any(), any(), any(), any());
    }

    @Test
    void rejectRefundShouldRequireSellerAndApplyingStatus()
    {
        TrTradeOrder order = refundingApplyingOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderRefundRejected(100L, "seller")).thenReturn(1);

        int rows = appOrderService.rejectRefund(100L, 3L, "seller");

        Assertions.assertEquals(1, rows);
        verify(trTradeOrderMapper).updateOrderRefundRejected(100L, "seller");
    }

    @Test
    void rejectRefundShouldRejectWhenNoPendingApplication()
    {
        TrTradeOrder order = awaitingReceiptOrder(); // 没有退款申请
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.rejectRefund(100L, 3L, "seller"));

        Assertions.assertTrue(ex.getMessage().contains("没有待处理的退款申请"));
        verify(trTradeOrderMapper, never()).updateOrderRefundRejected(any(), any());
    }

    @Test
    void agreeRefundShouldFailWhenAlipayNotConfigured()
    {
        // 抢占 refund 1→2 后调支付宝退款，未配置则抛异常（事务整体回滚）
        TrTradeOrder order = refundingApplyingOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderRefundAgreed(100L, "seller")).thenReturn(1);
        when(alipayProperties.isConfigured()).thenReturn(false);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.agreeRefund(100L, 3L, "seller"));

        Assertions.assertTrue(ex.getMessage().contains("无法退款"));
        verify(trTradeOrderMapper, never()).updateOrderRefunded(any(), any(), any(), any(), any());
    }

    @Test
    void sellerRefundShouldFailWhenAlipayNotConfigured()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderSellerRefunding(eq(100L), eq("缺货"),
                any(Date.class), eq("seller"))).thenReturn(1);
        when(alipayProperties.isConfigured()).thenReturn(false);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.sellerRefund(100L, 3L, "缺货", "seller"));

        Assertions.assertTrue(ex.getMessage().contains("无法退款"));
        verify(trTradeOrderMapper, never()).updateOrderRefunded(any(), any(), any(), any(), any());
    }

    @Test
    void sellerRefundShouldRejectWrongStatus()
    {
        TrTradeOrder order = refundingApplyingOrder(); // 已在退款流程中，不能再主动退
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.sellerRefund(100L, 3L, "x", "seller"));

        Assertions.assertTrue(ex.getMessage().contains("不能主动退款"));
        verify(trTradeOrderMapper, never()).updateOrderSellerRefunding(any(), any(), any(), any());
    }

    @Test
    void finishOrderShouldAuditBuyer()
    {
        // 业务规则：线下交割后由买家点击完成（与 DB 列 complete_time "买家确认完成时间" 一致）
        TrTradeOrder order = awaitingReceiptOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        // 完成订单 → 状态置为 ORDER_FINISHED = "3"，并回写 complete_time
        when(trTradeOrderMapper.updateOrderStatus(eq(100L), eq("3"), eq("buyer"),
                isNull(), isNull(), any(Date.class), isNull())).thenReturn(1);

        int rows = appOrderService.finishOrder(100L, 5L, "buyer");

        Assertions.assertEquals(1, rows);
        verify(trTradeOrderMapper).updateOrderStatus(eq(100L), eq("3"), eq("buyer"),
                isNull(), isNull(), any(Date.class), isNull());
        // 交易完成 → 买卖双方各加分，发两次信用事件
        verify(eventPublisher, times(2)).publishEvent(any(CreditChangeEvent.class));
    }

    @Test
    void finishOrderShouldRejectSeller()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.finishOrder(100L, 3L, "seller"));

        Assertions.assertNotNull(ex.getMessage());
        verify(trTradeOrderMapper, never()).updateOrderStatus(eq(100L), eq("3"), eq("seller"),
                isNull(), isNull(), any(Date.class), isNull());
    }

    @Test
    void payShouldRequireConfiguredAlipay()
    {
        when(alipayProperties.isConfigured()).thenReturn(false);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appOrderService.createAlipayPayForm(100L, 5L, "buyer"));

        Assertions.assertTrue(ex.getMessage().contains("支付宝沙盒配置不完整"));
        verify(trTradeOrderMapper, never()).selectTrTradeOrderForUpdate(100L);
    }

    @Test
    void cancelTimeoutOrderShouldCancelUnpaidAndRestoreGoods()
    {
        TrTradeOrder order = pendingPayOrder();
        order.setCreateBy("buyer");
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderCancelled(eq(100L), eq("4"), eq("4"), eq("buyer"),
                any(Date.class))).thenReturn(1);

        boolean cancelled = appOrderService.cancelTimeoutOrder(100L);

        Assertions.assertTrue(cancelled);
        verify(trTradeGoodsMapper).restoreSoldGoodsToOnSale(20L, "4", "1");
    }

    @Test
    void cancelTimeoutOrderShouldSkipWhenAlreadyPaid()
    {
        TrTradeOrder order = pendingPayOrder();
        order.setPaymentStatus("2");
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        boolean cancelled = appOrderService.cancelTimeoutOrder(100L);

        Assertions.assertFalse(cancelled);
        verify(trTradeOrderMapper, never()).updateOrderCancelled(any(), any(), any(), any(), any());
    }

    @Test
    void autoConfirmOrderShouldFinishAndCreditBothSides()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);
        when(trTradeOrderMapper.updateOrderStatus(eq(100L), eq("3"), eq("system"),
                isNull(), isNull(), any(Date.class), isNull())).thenReturn(1);

        boolean confirmed = appOrderService.autoConfirmOrder(100L);

        Assertions.assertTrue(confirmed);
        verify(eventPublisher, times(2)).publishEvent(any(CreditChangeEvent.class));
    }

    @Test
    void autoConfirmOrderShouldSkipWhenRefundInProgress()
    {
        TrTradeOrder order = awaitingReceiptOrder();
        order.setRefundStatus("1"); // 退款申请中，不自动确认
        when(trTradeOrderMapper.selectTrTradeOrderForUpdate(100L)).thenReturn(order);

        boolean confirmed = appOrderService.autoConfirmOrder(100L);

        Assertions.assertFalse(confirmed);
        verify(trTradeOrderMapper, never()).updateOrderStatus(any(), any(), any(), any(), any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(CreditChangeEvent.class));
    }

    private TrTradeGoods onSaleGoods()
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(20L);
        goods.setSellerId(3L);
        goods.setTitle("二手教材");
        goods.setPrice(new BigDecimal("20.00"));
        goods.setGoodsStatus("1");
        goods.setTradePlace("一食堂门口");
        return goods;
    }

    /** 待支付(0) 订单。 */
    private TrTradeOrder pendingPayOrder()
    {
        TrTradeOrder order = baseOrder();
        order.setOrderStatus("0");
        return order;
    }

    /** 待收货(2) 订单（已支付）。 */
    private TrTradeOrder awaitingReceiptOrder()
    {
        TrTradeOrder order = baseOrder();
        order.setOrderStatus("2");
        order.setPaymentStatus("2");
        order.setRefundStatus("0");
        order.setPaymentAmount(new BigDecimal("20.00"));
        return order;
    }

    /** 退款中(6) + 买家申请待处理(1) 订单。 */
    private TrTradeOrder refundingApplyingOrder()
    {
        TrTradeOrder order = baseOrder();
        order.setOrderStatus("6");
        order.setPaymentStatus("2");
        order.setRefundStatus("1");
        order.setPaymentAmount(new BigDecimal("20.00"));
        return order;
    }

    private TrTradeOrder baseOrder()
    {
        TrTradeOrder order = new TrTradeOrder();
        order.setOrderId(100L);
        order.setOrderNo("CO-TEST-100");
        order.setGoodsId(20L);
        order.setSellerId(3L);
        order.setBuyerId(5L);
        order.setTradePrice(new BigDecimal("20.00"));
        return order;
    }
}
