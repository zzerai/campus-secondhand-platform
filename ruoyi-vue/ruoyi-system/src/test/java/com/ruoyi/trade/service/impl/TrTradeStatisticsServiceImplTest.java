package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsCategoryVo;
import com.ruoyi.trade.domain.query.TrTradeStatisticsTrendQuery;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsDailyCountVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsDailyPaymentVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsOrderTrendVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsOverviewVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsPaymentTrendVo;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.mapper.TrTradeReportMapper;

@ExtendWith(MockitoExtension.class)
class TrTradeStatisticsServiceImplTest
{
    private static final String OVERVIEW_CACHE_KEY = CacheConstants.TRADE_STATISTICS_KEY + "overview";

    private static final String CATEGORY_CACHE_KEY = CacheConstants.TRADE_STATISTICS_KEY + "category";

    @Mock
    private RedisCache redisCache;

    @Mock
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Mock
    private TrTradeOrderMapper trTradeOrderMapper;

    @Mock
    private TrStudentUserMapper trStudentUserMapper;

    @Mock
    private TrTradeReportMapper trTradeReportMapper;

    @Mock
    private TrTradeDisputeMapper trTradeDisputeMapper;

    @InjectMocks
    private TrTradeStatisticsServiceImpl trTradeStatisticsService;

    @Test
    void shouldAssembleOverviewStatistics()
    {
        when(trTradeGoodsMapper.selectGoodsTotalCount()).thenReturn(10L);
        when(trTradeGoodsMapper.selectPendingAuditGoodsCount()).thenReturn(2L);
        when(trTradeGoodsMapper.selectOnShelfGoodsCount()).thenReturn(6L);
        when(trTradeOrderMapper.selectOrderTotalCount()).thenReturn(9L);
        when(trTradeOrderMapper.selectTodayNewOrderCount()).thenReturn(3L);
        when(trTradeOrderMapper.selectCompletedOrderCount()).thenReturn(4L);
        when(trTradeOrderMapper.selectCompletedTradeAmount()).thenReturn(new BigDecimal("88.5"));
        when(trStudentUserMapper.selectStudentUserTotalCount()).thenReturn(20L);
        when(trStudentUserMapper.selectTodayNewUserCount()).thenReturn(5L);
        when(trTradeReportMapper.selectPendingReportCount()).thenReturn(1L);
        when(trTradeDisputeMapper.selectPendingDisputeCount()).thenReturn(2L);

        TrTradeStatisticsOverviewVo overview = trTradeStatisticsService.selectOverview();

        Assertions.assertEquals(10L, overview.getGoodsTotal());
        Assertions.assertEquals(2L, overview.getGoodsPendingAudit());
        Assertions.assertEquals(6L, overview.getGoodsOnShelf());
        Assertions.assertEquals(9L, overview.getOrderTotal());
        Assertions.assertEquals(3L, overview.getTodayNewOrderCount());
        Assertions.assertEquals(4L, overview.getCompletedOrderCount());
        Assertions.assertEquals(0, new BigDecimal("88.50").compareTo(overview.getTotalTradeAmount()));
        Assertions.assertEquals(20L, overview.getStudentUserTotal());
        Assertions.assertEquals(5L, overview.getTodayNewUserCount());
        Assertions.assertEquals(1L, overview.getPendingReportCount());
        Assertions.assertEquals(2L, overview.getPendingDisputeCount());
        // 缓存未命中应回源聚合并以 TTL 写回缓存
        verify(redisCache).setCacheObject(eq(OVERVIEW_CACHE_KEY), any(TrTradeStatisticsOverviewVo.class),
                eq(60), eq(TimeUnit.SECONDS));
    }

    @Test
    void selectOverviewShouldReturnCachedAndSkipQueriesOnHit()
    {
        TrTradeStatisticsOverviewVo cached = new TrTradeStatisticsOverviewVo();
        cached.setGoodsTotal(99L);
        when(redisCache.getCacheObject(OVERVIEW_CACHE_KEY)).thenReturn(cached);

        TrTradeStatisticsOverviewVo overview = trTradeStatisticsService.selectOverview();

        Assertions.assertEquals(99L, overview.getGoodsTotal());
        verify(trTradeGoodsMapper, never()).selectGoodsTotalCount();
        verify(trTradeOrderMapper, never()).selectOrderTotalCount();
    }

    @Test
    void shouldFillMissingDatesForOrderTrend()
    {
        TrTradeStatisticsTrendQuery query = new TrTradeStatisticsTrendQuery();
        query.setStartDate(toDate(LocalDate.of(2026, 5, 1)));
        query.setEndDate(toDate(LocalDate.of(2026, 5, 3)));

        when(trTradeOrderMapper.selectDailyNewOrderCount(any(Date.class), any(Date.class)))
                .thenReturn(Arrays.asList(dailyCount("2026-05-01", 2L), dailyCount("2026-05-03", 1L)));
        when(trTradeOrderMapper.selectDailyCompletedOrderCount(any(Date.class), any(Date.class)))
                .thenReturn(Collections.singletonList(dailyCount("2026-05-02", 1L)));
        when(trTradeOrderMapper.selectDailyCancelledOrderCount(any(Date.class), any(Date.class)))
                .thenReturn(Collections.singletonList(dailyCount("2026-05-03", 2L)));

        List<TrTradeStatisticsOrderTrendVo> trend = trTradeStatisticsService.selectOrderTrend(query);

        ArgumentCaptor<Date> startCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endCaptor = ArgumentCaptor.forClass(Date.class);
        verify(trTradeOrderMapper).selectDailyNewOrderCount(startCaptor.capture(), endCaptor.capture());

        Assertions.assertEquals(3, trend.size());
        Assertions.assertEquals(toDate(LocalDate.of(2026, 5, 1)), startCaptor.getValue());
        Assertions.assertEquals(toDate(LocalDate.of(2026, 5, 4)), endCaptor.getValue());
        Assertions.assertEquals("2026-05-01", trend.get(0).getStatDate());
        Assertions.assertEquals(2L, trend.get(0).getNewOrderCount());
        Assertions.assertEquals(0L, trend.get(0).getCompletedOrderCount());
        Assertions.assertEquals(0L, trend.get(0).getCancelledOrderCount());

        Assertions.assertEquals("2026-05-02", trend.get(1).getStatDate());
        Assertions.assertEquals(0L, trend.get(1).getNewOrderCount());
        Assertions.assertEquals(1L, trend.get(1).getCompletedOrderCount());
        Assertions.assertEquals(0L, trend.get(1).getCancelledOrderCount());

        Assertions.assertEquals("2026-05-03", trend.get(2).getStatDate());
        Assertions.assertEquals(1L, trend.get(2).getNewOrderCount());
        Assertions.assertEquals(0L, trend.get(2).getCompletedOrderCount());
        Assertions.assertEquals(2L, trend.get(2).getCancelledOrderCount());
    }

    @Test
    void shouldUseExplicitDaysForPaymentTrend()
    {
        TrTradeStatisticsTrendQuery query = new TrTradeStatisticsTrendQuery();
        query.setDays(3);

        when(trTradeOrderMapper.selectDailyPaymentTrend(any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        List<TrTradeStatisticsPaymentTrendVo> trend = trTradeStatisticsService.selectPaymentTrend(query);

        Assertions.assertEquals(3, trend.size());
    }

    @Test
    void shouldReturnCategoryStatisticsFromMapper()
    {
        TrTradeStatisticsCategoryVo categoryVo = new TrTradeStatisticsCategoryVo();
        categoryVo.setCategoryId(1L);
        categoryVo.setCategoryName("教材");
        categoryVo.setGoodsCount(2L);
        categoryVo.setCompletedOrderCount(1L);
        when(trTradeGoodsMapper.selectCategoryStatistics()).thenReturn(Collections.singletonList(categoryVo));

        List<TrTradeStatisticsCategoryVo> result = trTradeStatisticsService.selectCategoryStatistics();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("教材", result.get(0).getCategoryName());
        Assertions.assertEquals(2L, result.get(0).getGoodsCount());
        Assertions.assertEquals(1L, result.get(0).getCompletedOrderCount());
        verify(redisCache).setCacheObject(eq(CATEGORY_CACHE_KEY), any(), eq(60), eq(TimeUnit.SECONDS));
    }

    @Test
    void selectCategoryStatisticsShouldReturnCachedAndSkipMapperOnHit()
    {
        TrTradeStatisticsCategoryVo cached = new TrTradeStatisticsCategoryVo();
        cached.setCategoryId(1L);
        cached.setCategoryName("教材");
        cached.setGoodsCount(2L);
        JSONArray cachedArray = new JSONArray();
        cachedArray.add(cached);
        when(redisCache.getCacheObject(CATEGORY_CACHE_KEY)).thenReturn(cachedArray);

        List<TrTradeStatisticsCategoryVo> result = trTradeStatisticsService.selectCategoryStatistics();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("教材", result.get(0).getCategoryName());
        verify(trTradeGoodsMapper, never()).selectCategoryStatistics();
    }

    @Test
    void shouldThrowWhenStartDateAfterEndDate()
    {
        TrTradeStatisticsTrendQuery query = new TrTradeStatisticsTrendQuery();
        query.setStartDate(toDate(LocalDate.of(2026, 5, 5)));
        query.setEndDate(toDate(LocalDate.of(2026, 5, 1)));

        ServiceException exception = Assertions.assertThrows(ServiceException.class,
                () -> trTradeStatisticsService.selectOrderTrend(query));

        Assertions.assertEquals("开始日期不能晚于结束日期", exception.getMessage());
    }

    @Test
    void shouldCapDaysToMaxWhenInputTooLarge()
    {
        TrTradeStatisticsTrendQuery query = new TrTradeStatisticsTrendQuery();
        query.setDays(999);

        when(trTradeOrderMapper.selectDailyPaymentTrend(any(Date.class), any(Date.class)))
                .thenReturn(Collections.emptyList());

        List<TrTradeStatisticsPaymentTrendVo> trend = trTradeStatisticsService.selectPaymentTrend(query);

        Assertions.assertEquals(366, trend.size());
    }

    private TrTradeStatisticsDailyCountVo dailyCount(String statDate, Long totalCount)
    {
        TrTradeStatisticsDailyCountVo vo = new TrTradeStatisticsDailyCountVo();
        vo.setStatDate(statDate);
        vo.setTotalCount(totalCount);
        return vo;
    }

    private TrTradeStatisticsDailyPaymentVo dailyPayment(String statDate, Long paymentCount, BigDecimal paymentAmount)
    {
        TrTradeStatisticsDailyPaymentVo vo = new TrTradeStatisticsDailyPaymentVo();
        vo.setStatDate(statDate);
        vo.setPaymentCount(paymentCount);
        vo.setPaymentAmount(paymentAmount);
        return vo;
    }

    private Date toDate(LocalDate localDate)
    {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
