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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeEvaluation;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.mapper.TrTradeEvaluationMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.utils.CreditConstants;

/**
 * 交易评价 Service 单测，覆盖评分范围 / 订单已完成 / 参与方 / 重复评价校验。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeEvaluationServiceImplTest
{
    @Mock private TrTradeEvaluationMapper trTradeEvaluationMapper;
    @Mock private TrTradeOrderMapper trTradeOrderMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TrTradeEvaluationServiceImpl service;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName("admin");
        LoginUser loginUser = new LoginUser(1L, 1L, sysUser,
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

    @Test
    void insertShouldRejectInvalidScore()
    {
        TrTradeEvaluation e = baseEvaluation();
        e.setScore(0L);
        Assertions.assertThrows(ServiceException.class, () -> service.insertTrTradeEvaluation(e));
        e.setScore(6L);
        Assertions.assertThrows(ServiceException.class, () -> service.insertTrTradeEvaluation(e));
        e.setScore(null);
        Assertions.assertThrows(ServiceException.class, () -> service.insertTrTradeEvaluation(e));
        verify(trTradeEvaluationMapper, never()).insertTrTradeEvaluation(any());
    }

    @Test
    void insertShouldRejectWhenOrderMissing()
    {
        TrTradeEvaluation e = baseEvaluation();
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class, () -> service.insertTrTradeEvaluation(e));
    }

    @Test
    void insertShouldRejectWhenOrderNotFinished()
    {
        TrTradeEvaluation e = baseEvaluation();
        TrTradeOrder order = new TrTradeOrder();
        order.setOrderId(100L);
        order.setOrderStatus("2"); // 待交割
        order.setBuyerId(5L);
        order.setSellerId(6L);
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(order);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeEvaluation(e));
        Assertions.assertTrue(ex.getMessage().contains("已完成"));
    }

    @Test
    void insertShouldRejectWhenUserNotPartyOfOrder()
    {
        TrTradeEvaluation e = baseEvaluation();
        e.setFromUserId(99L); // 既不是买家也不是卖家
        TrTradeOrder order = finishedOrder();
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(order);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeEvaluation(e));
        Assertions.assertTrue(ex.getMessage().contains("买卖双方"));
    }

    @Test
    void insertShouldRejectDuplicateEvaluation()
    {
        TrTradeEvaluation e = baseEvaluation();
        TrTradeOrder order = finishedOrder();
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(order);
        when(trTradeEvaluationMapper.countByOrderAndFromUser(100L, 5L)).thenReturn(1);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeEvaluation(e));
        Assertions.assertTrue(ex.getMessage().contains("重复评价"));
    }

    @Test
    void insertShouldDeriveToUserAndInsert()
    {
        TrTradeEvaluation e = baseEvaluation();
        e.setToUserId(123L); // 客户端伪造，应被服务端覆盖
        TrTradeOrder order = finishedOrder();
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(order);
        when(trTradeEvaluationMapper.countByOrderAndFromUser(anyLong(), anyLong())).thenReturn(0);

        ArgumentCaptor<TrTradeEvaluation> captor = ArgumentCaptor.forClass(TrTradeEvaluation.class);
        when(trTradeEvaluationMapper.insertTrTradeEvaluation(captor.capture())).thenReturn(1);

        Assertions.assertEquals(1, service.insertTrTradeEvaluation(e));
        TrTradeEvaluation saved = captor.getValue();
        // 评价人 = 买家 5 → 被评价人推导为卖家 6（而非客户端传的 123）
        Assertions.assertEquals(6L, saved.getToUserId());
        // 5★ 好评 → 被评价人（卖家6）+1 信用
        ArgumentCaptor<CreditChangeEvent> ev = ArgumentCaptor.forClass(CreditChangeEvent.class);
        verify(eventPublisher).publishEvent(ev.capture());
        Assertions.assertEquals(6L, ev.getValue().getUserId());
        Assertions.assertEquals(CreditConstants.DELTA_GOOD_REVIEW, ev.getValue().getChangeValue());
    }

    @Test
    void badReviewDeductsAndMidReviewDoesNot()
    {
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(finishedOrder());
        when(trTradeEvaluationMapper.countByOrderAndFromUser(anyLong(), anyLong())).thenReturn(0);
        when(trTradeEvaluationMapper.insertTrTradeEvaluation(any())).thenReturn(1);

        // 差评 2★ → -1
        TrTradeEvaluation bad = baseEvaluation();
        bad.setScore(2L);
        service.insertTrTradeEvaluation(bad);
        ArgumentCaptor<CreditChangeEvent> ev = ArgumentCaptor.forClass(CreditChangeEvent.class);
        verify(eventPublisher).publishEvent(ev.capture());
        Assertions.assertEquals(CreditConstants.DELTA_BAD_REVIEW, ev.getValue().getChangeValue());

        // 中评 3★ → 不发事件（累计仍只有上面 1 次）
        TrTradeEvaluation mid = baseEvaluation();
        mid.setScore(3L);
        service.insertTrTradeEvaluation(mid);
        verify(eventPublisher, org.mockito.Mockito.times(1)).publishEvent(any(CreditChangeEvent.class));
    }

    private TrTradeEvaluation baseEvaluation()
    {
        TrTradeEvaluation e = new TrTradeEvaluation();
        e.setOrderId(100L);
        e.setFromUserId(5L);
        e.setScore(5L);
        e.setContent("非常满意");
        return e;
    }

    private TrTradeOrder finishedOrder()
    {
        TrTradeOrder order = new TrTradeOrder();
        order.setOrderId(100L);
        order.setOrderStatus("3");
        order.setBuyerId(5L);
        order.setSellerId(6L);
        return order;
    }
}
