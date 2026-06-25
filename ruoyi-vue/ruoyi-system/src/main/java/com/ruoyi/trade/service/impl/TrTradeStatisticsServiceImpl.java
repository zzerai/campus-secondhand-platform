package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.trade.domain.query.TrTradeStatisticsTrendQuery;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsCategoryVo;
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
import com.ruoyi.trade.service.ITrTradeStatisticsService;

/**
 * Trade statistics service implementation.
 *
 * @author lyl
 */
@Service
public class TrTradeStatisticsServiceImpl implements ITrTradeStatisticsService
{
    private static final int DEFAULT_DAYS = 7;

    private static final int MAX_DAYS = 366;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 统计概览缓存键 */
    private static final String OVERVIEW_CACHE_KEY = CacheConstants.TRADE_STATISTICS_KEY + "overview";

    /** 分类统计缓存键 */
    private static final String CATEGORY_CACHE_KEY = CacheConstants.TRADE_STATISTICS_KEY + "category";

    /** 统计类缓存有效期（秒）：后台看板容忍秒级延迟，过期自动重算，无需主动失效 */
    private static final int STATISTICS_CACHE_SECONDS = 60;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    @Autowired
    private TrTradeReportMapper trTradeReportMapper;

    @Autowired
    private TrTradeDisputeMapper trTradeDisputeMapper;

    @Override
    public TrTradeStatisticsOverviewVo selectOverview()
    {
        TrTradeStatisticsOverviewVo cached = redisCache.getCacheObject(OVERVIEW_CACHE_KEY);
        if (cached != null)
        {
            return cached;
        }
        TrTradeStatisticsOverviewVo overviewVo = new TrTradeStatisticsOverviewVo();
        overviewVo.setGoodsTotal(defaultLong(trTradeGoodsMapper.selectGoodsTotalCount()));
        overviewVo.setGoodsPendingAudit(defaultLong(trTradeGoodsMapper.selectPendingAuditGoodsCount()));
        overviewVo.setGoodsOnShelf(defaultLong(trTradeGoodsMapper.selectOnShelfGoodsCount()));
        overviewVo.setOrderTotal(defaultLong(trTradeOrderMapper.selectOrderTotalCount()));
        overviewVo.setTodayNewOrderCount(defaultLong(trTradeOrderMapper.selectTodayNewOrderCount()));
        overviewVo.setCompletedOrderCount(defaultLong(trTradeOrderMapper.selectCompletedOrderCount()));
        overviewVo.setTotalTradeAmount(defaultDecimal(trTradeOrderMapper.selectCompletedTradeAmount()));
        overviewVo.setStudentUserTotal(defaultLong(trStudentUserMapper.selectStudentUserTotalCount()));
        overviewVo.setTodayNewUserCount(defaultLong(trStudentUserMapper.selectTodayNewUserCount()));
        overviewVo.setPendingReportCount(defaultLong(trTradeReportMapper.selectPendingReportCount()));
        overviewVo.setPendingDisputeCount(defaultLong(trTradeDisputeMapper.selectPendingDisputeCount()));
        redisCache.setCacheObject(OVERVIEW_CACHE_KEY, overviewVo, STATISTICS_CACHE_SECONDS, TimeUnit.SECONDS);
        return overviewVo;
    }

    @Override
    public List<TrTradeStatisticsCategoryVo> selectCategoryStatistics()
    {
        JSONArray cached = redisCache.getCacheObject(CATEGORY_CACHE_KEY);
        if (cached != null)
        {
            // FastJson2 序列化器读回为 JSONArray，需显式转回实体（与 DictUtils 同一约定）
            return cached.toList(TrTradeStatisticsCategoryVo.class);
        }
        List<TrTradeStatisticsCategoryVo> list = trTradeGoodsMapper.selectCategoryStatistics();
        redisCache.setCacheObject(CATEGORY_CACHE_KEY, list, STATISTICS_CACHE_SECONDS, TimeUnit.SECONDS);
        return list;
    }

    @Override
    public List<TrTradeStatisticsOrderTrendVo> selectOrderTrend(TrTradeStatisticsTrendQuery query)
    {
        TrendRange trendRange = buildTrendRange(query);
        Map<String, Long> newOrderMap = toCountMap(trTradeOrderMapper.selectDailyNewOrderCount(trendRange.getStartDateTime(), trendRange.getEndDateTime()));
        Map<String, Long> completedOrderMap = toCountMap(trTradeOrderMapper.selectDailyCompletedOrderCount(trendRange.getStartDateTime(), trendRange.getEndDateTime()));
        Map<String, Long> cancelledOrderMap = toCountMap(trTradeOrderMapper.selectDailyCancelledOrderCount(trendRange.getStartDateTime(), trendRange.getEndDateTime()));

        List<TrTradeStatisticsOrderTrendVo> result = new ArrayList<>();
        for (LocalDate date : trendRange.getDates())
        {
            String statDate = formatDate(date);
            TrTradeStatisticsOrderTrendVo trendVo = new TrTradeStatisticsOrderTrendVo();
            trendVo.setStatDate(statDate);
            trendVo.setNewOrderCount(newOrderMap.getOrDefault(statDate, 0L));
            trendVo.setCompletedOrderCount(completedOrderMap.getOrDefault(statDate, 0L));
            trendVo.setCancelledOrderCount(cancelledOrderMap.getOrDefault(statDate, 0L));
            result.add(trendVo);
        }
        return result;
    }

    @Override
    public List<TrTradeStatisticsPaymentTrendVo> selectPaymentTrend(TrTradeStatisticsTrendQuery query)
    {
        TrendRange trendRange = buildTrendRange(query);
        Map<String, TrTradeStatisticsDailyPaymentVo> paymentMap = toPaymentMap(
                trTradeOrderMapper.selectDailyPaymentTrend(trendRange.getStartDateTime(), trendRange.getEndDateTime()));

        List<TrTradeStatisticsPaymentTrendVo> result = new ArrayList<>();
        for (LocalDate date : trendRange.getDates())
        {
            String statDate = formatDate(date);
            TrTradeStatisticsDailyPaymentVo dailyPayment = paymentMap.get(statDate);

            TrTradeStatisticsPaymentTrendVo trendVo = new TrTradeStatisticsPaymentTrendVo();
            trendVo.setStatDate(statDate);
            trendVo.setPaymentCount(dailyPayment == null ? 0L : defaultLong(dailyPayment.getPaymentCount()));
            trendVo.setPaymentAmount(dailyPayment == null ? BigDecimal.ZERO : defaultDecimal(dailyPayment.getPaymentAmount()));
            result.add(trendVo);
        }
        return result;
    }

    private TrendRange buildTrendRange(TrTradeStatisticsTrendQuery query)
    {
        TrTradeStatisticsTrendQuery safeQuery = query == null ? new TrTradeStatisticsTrendQuery() : query;
        LocalDate endDate = safeQuery.getEndDate() == null
                ? LocalDate.now()
                : toLocalDate(safeQuery.getEndDate());

        LocalDate startDate;
        if (safeQuery.getStartDate() != null)
        {
            startDate = toLocalDate(safeQuery.getStartDate());
        }
        else
        {
            int days = safeDays(safeQuery.getDays());
            startDate = endDate.minusDays(days - 1L);
        }

        if (startDate.isAfter(endDate))
        {
            throw new ServiceException("开始日期不能晚于结束日期");
        }

        long inclusiveDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
        if (inclusiveDays > MAX_DAYS)
        {
            startDate = endDate.minusDays(MAX_DAYS - 1L);
        }

        return new TrendRange(startDate, endDate);
    }

    private int safeDays(Integer days)
    {
        if (days == null || days <= 0)
        {
            return DEFAULT_DAYS;
        }
        return Math.min(days, MAX_DAYS);
    }

    private Map<String, Long> toCountMap(List<TrTradeStatisticsDailyCountVo> rows)
    {
        Map<String, Long> result = new HashMap<>();
        if (rows == null)
        {
            return result;
        }
        for (TrTradeStatisticsDailyCountVo row : rows)
        {
            if (row != null && row.getStatDate() != null)
            {
                result.put(row.getStatDate(), defaultLong(row.getTotalCount()));
            }
        }
        return result;
    }

    private Map<String, TrTradeStatisticsDailyPaymentVo> toPaymentMap(List<TrTradeStatisticsDailyPaymentVo> rows)
    {
        Map<String, TrTradeStatisticsDailyPaymentVo> result = new HashMap<>();
        if (rows == null)
        {
            return result;
        }
        for (TrTradeStatisticsDailyPaymentVo row : rows)
        {
            if (row != null && row.getStatDate() != null)
            {
                result.put(row.getStatDate(), row);
            }
        }
        return result;
    }

    private LocalDate toLocalDate(Date date)
    {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private String formatDate(LocalDate date)
    {
        return DATE_FORMATTER.format(date);
    }

    private Long defaultLong(Long value)
    {
        return Objects.requireNonNullElse(value, 0L);
    }

    private BigDecimal defaultDecimal(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static class TrendRange
    {
        private final LocalDate startDate;

        private final LocalDate endDate;

        private TrendRange(LocalDate startDate, LocalDate endDate)
        {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        private Date getStartDateTime()
        {
            return DateUtils.toDate(startDate);
        }

        private Date getEndDateTime()
        {
            return DateUtils.toDate(endDate.plusDays(1));
        }

        private List<LocalDate> getDates()
        {
            List<LocalDate> dates = new ArrayList<>();
            LocalDate current = startDate;
            while (!current.isAfter(endDate))
            {
                dates.add(current);
                current = current.plusDays(1);
            }
            return dates;
        }
    }
}
