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
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.vo.BatchHandleResult;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.mapper.TrTradeReportMapper;

/**
 * 举报信息 Service 单元测试，覆盖举报处理的状态校验与单条/批量路径。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeReportServiceImplTest
{
    private static final String FAKE_ADMIN = "admin";
    private static final Long FAKE_ADMIN_ID = 1L;

    @Mock private TrTradeReportMapper trTradeReportMapper;

    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TrTradeReportServiceImpl service;

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
                () -> service.handleReport(null, "1", "处理结果"));
    }

    @Test
    void handleShouldRejectIllegalTargetStatus()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.handleReport(1L, "0", "处理结果"));
        Assertions.assertThrows(ServiceException.class,
                () -> service.handleReport(1L, "9", "处理结果"));
    }

    @Test
    void handleShouldRequireHandleResult()
    {
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.handleReport(1L, "1", ""));
        Assertions.assertTrue(ex.getMessage().contains("处理结果"));
    }

    @Test
    void handleShouldRejectWhenReportMissing()
    {
        when(trTradeReportMapper.selectTrTradeReportByReportId(1L)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class,
                () -> service.handleReport(1L, "1", "处理结果"));
        verify(trTradeReportMapper, never()).updateReportHandle(
                any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void handleShouldRejectWhenNotPending()
    {
        TrTradeReport exist = new TrTradeReport();
        exist.setReportId(1L);
        exist.setHandleStatus("1"); // 已处理
        when(trTradeReportMapper.selectTrTradeReportByReportId(1L)).thenReturn(exist);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.handleReport(1L, "2", "处理结果"));
        Assertions.assertTrue(ex.getMessage().contains("待处理"));
    }

    @Test
    void handleShouldPassWithAdminContext()
    {
        TrTradeReport exist = new TrTradeReport();
        exist.setReportId(1L);
        exist.setHandleStatus("0"); // 待处理
        exist.setReportedUserId(99L);
        when(trTradeReportMapper.selectTrTradeReportByReportId(1L)).thenReturn(exist);
        when(trTradeReportMapper.updateReportHandle(eq(1L), eq("1"), eq(FAKE_ADMIN_ID),
                any(), eq("属实已下架"), eq(FAKE_ADMIN), any())).thenReturn(1);

        int rows = service.handleReport(1L, "1", "属实已下架");

        Assertions.assertEquals(1, rows);
        verify(trTradeReportMapper).updateReportHandle(eq(1L), eq("1"), eq(FAKE_ADMIN_ID),
                any(), eq("属实已下架"), eq(FAKE_ADMIN), any());
        // 举报成立 → 扣被举报人信用分
        ArgumentCaptor<CreditChangeEvent> captor = ArgumentCaptor.forClass(CreditChangeEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        Assertions.assertEquals(99L, captor.getValue().getUserId());
    }

    @Test
    void rejectingReportDoesNotDeductCredit()
    {
        TrTradeReport exist = new TrTradeReport();
        exist.setReportId(1L);
        exist.setHandleStatus("0");
        exist.setReportedUserId(99L);
        when(trTradeReportMapper.selectTrTradeReportByReportId(1L)).thenReturn(exist);
        when(trTradeReportMapper.updateReportHandle(eq(1L), eq("2"), any(),
                any(), anyString(), any(), any())).thenReturn(1);

        service.handleReport(1L, "2", "证据不足，驳回");

        verify(eventPublisher, never()).publishEvent(any(CreditChangeEvent.class));
    }

    // -------- handle batch --------

    @Test
    void batchHandleShouldRejectEmptyIds()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.batchHandleReport(new Long[0], "1", "处理结果"));
    }

    @Test
    void batchHandleShouldReturnSuccessAndErrorDetail()
    {
        TrTradeReport pending = new TrTradeReport();
        pending.setReportId(1L);
        pending.setHandleStatus("0");
        TrTradeReport done = new TrTradeReport();
        done.setReportId(2L);
        done.setHandleStatus("1");
        when(trTradeReportMapper.selectTrTradeReportByReportId(1L)).thenReturn(pending);
        when(trTradeReportMapper.selectTrTradeReportByReportId(2L)).thenReturn(done);
        when(trTradeReportMapper.selectTrTradeReportByReportId(3L)).thenReturn(null);
        when(trTradeReportMapper.updateReportHandle(eq(1L), anyString(), any(),
                any(), anyString(), anyString(), any())).thenReturn(1);

        BatchHandleResult result = service.batchHandleReport(
                new Long[]{1L, 2L, 3L}, "1", "处理结果");

        Assertions.assertEquals(1, result.getSuccess());
        Assertions.assertEquals(2, result.getFailure());
        Assertions.assertEquals(2, result.getErrors().size());
        Assertions.assertTrue(result.getErrors().stream().anyMatch(e -> e.getId().equals(2L)));
        Assertions.assertTrue(result.getErrors().stream().anyMatch(e -> e.getId().equals(3L)));
        verify(trTradeReportMapper, times(1)).updateReportHandle(eq(1L), anyString(),
                any(), any(), anyString(), anyString(), any());
    }
}
