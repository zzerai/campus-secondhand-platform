package com.ruoyi.trade.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.dto.AppReportSubmitDto;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeReportMapper;

/**
 * 移动端举报业务单测。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class AppReportServiceImplTest
{
    @Mock private TrTradeReportMapper trTradeReportMapper;
    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;
    @Mock private TrStudentUserMapper trStudentUserMapper;

    @InjectMocks
    private AppReportServiceImpl service;

    @Test
    void submitShouldRejectMissingLogin()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.submitReport(null, validDto()));
    }

    @Test
    void submitShouldRejectMissingGoodsId()
    {
        AppReportSubmitDto dto = new AppReportSubmitDto();
        Assertions.assertThrows(ServiceException.class,
                () -> service.submitReport(1L, dto));
    }

    @Test
    void submitShouldRejectWhenGoodsMissing()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(null);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.submitReport(1L, validDto()));
        Assertions.assertTrue(ex.getMessage().contains("商品不存在"));
        verify(trTradeReportMapper, never()).insertTrTradeReport(any());
    }

    @Test
    void submitShouldRejectOwnGoods()
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(10L);
        goods.setSellerId(1L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(goods);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.submitReport(1L, validDto()));
        Assertions.assertTrue(ex.getMessage().contains("不能举报自己"));
    }

    @Test
    void submitShouldRejectDuplicatePending()
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(10L);
        goods.setSellerId(2L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(goods);
        when(trTradeReportMapper.countPendingByUserAndGoods(1L, 10L)).thenReturn(1);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.submitReport(1L, validDto()));
        Assertions.assertTrue(ex.getMessage().contains("已有待处理"));
        verify(trTradeReportMapper, never()).insertTrTradeReport(any());
    }

    @Test
    void submitShouldInsertAndBackfillReporterNo()
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(10L);
        goods.setSellerId(2L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(goods);
        when(trTradeReportMapper.countPendingByUserAndGoods(1L, 10L)).thenReturn(0);
        TrStudentUser reporter = new TrStudentUser();
        reporter.setUserId(1L);
        reporter.setStudentNo("S20260001");
        when(trStudentUserMapper.selectTrStudentUserByUserId(1L)).thenReturn(reporter);
        when(trTradeReportMapper.insertTrTradeReport(any(TrTradeReport.class)))
                .thenAnswer(inv -> {
                    TrTradeReport r = inv.getArgument(0);
                    r.setReportId(123L);
                    return 1;
                });

        Long reportId = service.submitReport(1L, validDto());
        Assertions.assertEquals(123L, reportId);
        verify(trTradeReportMapper).insertTrTradeReport(any(TrTradeReport.class));
    }

    @Test
    void submitShouldThrowWhenInsertReturnsZero()
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(10L);
        goods.setSellerId(2L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(goods);
        when(trTradeReportMapper.countPendingByUserAndGoods(anyLong(), anyLong())).thenReturn(0);
        when(trTradeReportMapper.insertTrTradeReport(any())).thenReturn(0);
        Assertions.assertThrows(ServiceException.class,
                () -> service.submitReport(1L, validDto()));
    }

    private AppReportSubmitDto validDto()
    {
        AppReportSubmitDto dto = new AppReportSubmitDto();
        dto.setGoodsId(10L);
        dto.setReportType("虚假信息");
        dto.setReportContent("商品描述与图片不符");
        return dto;
    }
}
