package com.ruoyi.trade.service.impl;

import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeDispute;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.event.DisputeSubmittedEvent;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.IAppOrderService;
import com.ruoyi.trade.utils.CreditConstants;

/**
 * 交易争议处理 Service 单元测试，覆盖人工仲裁的状态校验与单条/批量路径。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeDisputeServiceImplTest
{
    private static final String FAKE_ADMIN = "admin";
    private static final Long FAKE_ADMIN_ID = 1L;

    @Mock private TrTradeDisputeMapper trTradeDisputeMapper;

    @Mock private TrTradeOrderMapper trTradeOrderMapper;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private IAppOrderService appOrderService;

    @InjectMocks
    private TrTradeDisputeServiceImpl service;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(FAKE_ADMIN_ID);
        sysUser.setUserName(FAKE_ADMIN);
        LoginUser loginUser = new LoginUser(FAKE_ADMIN_ID, 1L, sysUser,
                new HashSet<>(Collections.singletonList("*:*:*")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, "", loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    // -------- handle single --------

    @Test
    void handleShouldRejectNullId()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.handleDispute(null, "支持买家", null, false));
    }

    @Test
    void handleShouldRequireHandleResult()
    {
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.handleDispute(1L, "", null, false));
        Assertions.assertTrue(ex.getMessage().contains("仲裁结论"));
    }

    @Test
    void handleShouldRejectWhenDisputeMissing()
    {
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class,
                () -> service.handleDispute(1L, "支持买家", null, false));
        verify(trTradeDisputeMapper, never()).updateDisputeHandle(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void handleShouldRejectWhenAlreadyDone()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setHandleStatus("3"); // 已处理
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.handleDispute(1L, "支持买家", null, false));
        Assertions.assertTrue(ex.getMessage().contains("已处理"));
    }

    @Test
    void handleShouldPassFromPendingWithAdminContext()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setHandleStatus("0"); // 待AI分析，仍可直接人工仲裁
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);
        when(trTradeDisputeMapper.updateDisputeHandle(eq(1L), eq("3"), eq(FAKE_ADMIN_ID),
                any(), eq("支持买家，卖家退款"), eq(FAKE_ADMIN), any())).thenReturn(1);

        int rows = service.handleDispute(1L, "支持买家，卖家退款", null, false);

        Assertions.assertEquals(1, rows);
        verify(trTradeDisputeMapper).updateDisputeHandle(eq(1L), eq("3"), eq(FAKE_ADMIN_ID),
                any(), eq("支持买家，卖家退款"), eq(FAKE_ADMIN), any());
        // faultParty 为 null：不发信用扣分事件
        verify(eventPublisher, never()).publishEvent(any(CreditChangeEvent.class));
        // 未判退款：不触发订单退款
        verify(appOrderService, never()).refundByArbitration(any(), any());
    }

    @Test
    void handleShouldRefundBuyerWhenRefundToBuyerTrue()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setHandleStatus("2");
        exist.setOrderId(100L);
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);
        when(trTradeDisputeMapper.updateDisputeHandle(eq(1L), eq("3"), any(),
                any(), anyString(), anyString(), any())).thenReturn(1);

        int rows = service.handleDispute(1L, "支持买家，平台退款", CreditConstants.FAULT_RESPONDENT, true);

        Assertions.assertEquals(1, rows);
        // 判退款给买家 → 调订单仲裁退款（订单ID取自争议）
        verify(appOrderService).refundByArbitration(eq(100L), eq(FAKE_ADMIN));
    }

    @Test
    void handleShouldDeductRespondentWhenFaultRespondent()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setHandleStatus("2");
        exist.setApplicantId(11L);
        exist.setRespondentId(22L);
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);
        when(trTradeDisputeMapper.updateDisputeHandle(eq(1L), eq("3"), any(),
                any(), anyString(), anyString(), any())).thenReturn(1);

        service.handleDispute(1L, "被申诉人责任", CreditConstants.FAULT_RESPONDENT, false);

        ArgumentCaptor<CreditChangeEvent> captor = ArgumentCaptor.forClass(CreditChangeEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        CreditChangeEvent ev = captor.getValue();
        Assertions.assertEquals(22L, ev.getUserId());
        Assertions.assertEquals(CreditConstants.TYPE_DISPUTE_FAULT, ev.getChangeType());
        Assertions.assertEquals(CreditConstants.DELTA_DISPUTE_FAULT, ev.getChangeValue());
    }

    @Test
    void handleBothFaultDeductsBothParties()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setHandleStatus("2");
        exist.setApplicantId(11L);
        exist.setRespondentId(22L);
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);
        when(trTradeDisputeMapper.updateDisputeHandle(eq(1L), eq("3"), any(),
                any(), anyString(), anyString(), any())).thenReturn(1);

        service.handleDispute(1L, "双方各担", CreditConstants.FAULT_BOTH, false);

        verify(eventPublisher, times(2)).publishEvent(any(CreditChangeEvent.class));
    }

    // -------- submit dispute --------

    @Test
    void submitShouldRejectWhenOrderNotFinishedOrMissing()
    {
        TrTradeDispute dispute = new TrTradeDispute();
        dispute.setOrderId(100L);
        // 条件更新影响 0 行：订单不存在、已删除或不处于可发起争议的状态
        when(trTradeOrderMapper.updateOrderToDisputing(eq(100L))).thenReturn(0);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.submitDispute(dispute));
        Assertions.assertTrue(ex.getMessage().contains("发起争议"));
        // 订单状态未抢占成功时，绝不插入争议记录
        verify(trTradeDisputeMapper, never()).insertTrTradeDispute(any());
    }

    @Test
    void submitShouldInsertWhenOrderFinished()
    {
        TrTradeDispute dispute = new TrTradeDispute();
        dispute.setOrderId(100L);
        // 条件更新影响 1 行：订单确处于可发起争议状态，成功抢占为“争议中”
        when(trTradeOrderMapper.updateOrderToDisputing(eq(100L))).thenReturn(1);

        service.submitDispute(dispute);

        verify(trTradeOrderMapper).updateOrderToDisputing(eq(100L));
        verify(trTradeDisputeMapper).insertTrTradeDispute(dispute);
        // 必须发争议提交事件，由 AFTER_COMMIT 监听器异步触发 AI 仲裁
        verify(eventPublisher).publishEvent(any(DisputeSubmittedEvent.class));
    }

    @Test
    void submitShouldNotPublishEventWhenOrderConditionFails()
    {
        TrTradeDispute dispute = new TrTradeDispute();
        dispute.setOrderId(100L);
        when(trTradeOrderMapper.updateOrderToDisputing(eq(100L))).thenReturn(0);

        Assertions.assertThrows(ServiceException.class, () -> service.submitDispute(dispute));
        // 订单抢占失败：不入库、不发事件，避免误触 AI
        verify(trTradeDisputeMapper, never()).insertTrTradeDispute(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // -------- handle batch --------

    @Test
    void batchHandleShouldRejectEmptyIds()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.batchHandleDispute(new Long[0], "支持买家"));
    }

    @Test
    void batchHandleShouldSkipInvalidAndCountSuccess()
    {
        TrTradeDispute pending = new TrTradeDispute();
        pending.setDisputeId(1L);
        pending.setHandleStatus("2");
        TrTradeDispute done = new TrTradeDispute();
        done.setDisputeId(2L);
        done.setHandleStatus("3");
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(pending);
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(2L)).thenReturn(done);
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(3L)).thenReturn(null);
        when(trTradeDisputeMapper.updateDisputeHandle(eq(1L), anyString(), any(),
                any(), anyString(), anyString(), any())).thenReturn(1);

        int success = service.batchHandleDispute(new Long[]{1L, 2L, 3L}, "支持买家");

        Assertions.assertEquals(1, success);
        verify(trTradeDisputeMapper, times(1)).updateDisputeHandle(eq(1L), anyString(),
                any(), any(), anyString(), anyString(), any());
    }
}
