package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;

/**
 * 管理端订单 Service 单测。
 * 当前重点：覆盖 updateTrTradeOrder 的越权防御 —— 通用 edit 不应允许改状态机/资金/参与方字段。
 */
@ExtendWith(MockitoExtension.class)
class TrTradeOrderServiceImplTest
{
    @Mock
    private TrTradeOrderMapper trTradeOrderMapper;

    @InjectMocks
    private TrTradeOrderServiceImpl trTradeOrderService;

    @Test
    void updateShouldRejectMissingOrderId()
    {
        TrTradeOrder order = new TrTradeOrder();
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trTradeOrderService.updateTrTradeOrder(order));
        Assertions.assertTrue(ex.getMessage().contains("订单ID"));
    }

    @Test
    void updateShouldStripSensitiveFieldsAndStampUpdater()
    {
        TrTradeOrder input = new TrTradeOrder();
        input.setOrderId(100L);
        // 管理员可改的安全字段
        input.setTradePlace("一食堂门口");
        input.setBuyerRemark("尽快交易");
        input.setSellerRemark("已确认");
        input.setRemark("管理员备注");
        // 越权字段：以下全部应被服务端 null 化
        input.setOrderStatus("3");
        input.setPaymentStatus("2");
        input.setPaymentAmount(new BigDecimal("0.01"));
        input.setAlipayTradeNo("2026FAKE");
        input.setPayTime(new Date());
        input.setPaymentTime(new Date());
        input.setConfirmTime(new Date());
        input.setCompleteTime(new Date());
        input.setCancelTime(new Date());
        input.setCancelReason("管理员伪造");
        input.setOrderNo("CO20260101FAKE");
        input.setBuyerId(999L);
        input.setSellerId(888L);
        input.setGoodsId(777L);
        input.setTradePrice(new BigDecimal("0.01"));

        when(trTradeOrderMapper.updateTrTradeOrder(any(TrTradeOrder.class))).thenReturn(1);

        try (MockedStatic<SecurityUtils> sec = Mockito.mockStatic(SecurityUtils.class))
        {
            sec.when(SecurityUtils::getUsername).thenReturn("admin");
            int rows = trTradeOrderService.updateTrTradeOrder(input);
            Assertions.assertEquals(1, rows);
        }

        ArgumentCaptor<TrTradeOrder> captor = ArgumentCaptor.forClass(TrTradeOrder.class);
        verify(trTradeOrderMapper).updateTrTradeOrder(captor.capture());
        TrTradeOrder saved = captor.getValue();

        // 安全字段保留
        Assertions.assertEquals(100L, saved.getOrderId());
        Assertions.assertEquals("一食堂门口", saved.getTradePlace());
        Assertions.assertEquals("尽快交易", saved.getBuyerRemark());
        Assertions.assertEquals("已确认", saved.getSellerRemark());
        Assertions.assertEquals("管理员备注", saved.getRemark());
        Assertions.assertEquals("admin", saved.getUpdateBy());
        Assertions.assertNotNull(saved.getUpdateTime());

        // 越权字段全部 null
        Assertions.assertNull(saved.getOrderStatus());
        Assertions.assertNull(saved.getPaymentStatus());
        Assertions.assertNull(saved.getPaymentAmount());
        Assertions.assertNull(saved.getAlipayTradeNo());
        Assertions.assertNull(saved.getPayTime());
        Assertions.assertNull(saved.getPaymentTime());
        Assertions.assertNull(saved.getConfirmTime());
        Assertions.assertNull(saved.getCompleteTime());
        Assertions.assertNull(saved.getCancelTime());
        Assertions.assertNull(saved.getCancelReason());
        Assertions.assertNull(saved.getOrderNo());
        Assertions.assertNull(saved.getBuyerId());
        Assertions.assertNull(saved.getSellerId());
        Assertions.assertNull(saved.getGoodsId());
        Assertions.assertNull(saved.getTradePrice());
    }
}
