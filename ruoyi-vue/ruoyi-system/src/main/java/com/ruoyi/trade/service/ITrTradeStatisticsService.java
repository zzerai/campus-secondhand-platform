package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.query.TrTradeStatisticsTrendQuery;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsCategoryVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsOrderTrendVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsOverviewVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsPaymentTrendVo;

/**
 * Trade statistics service.
 *
 * @author lyl
 */
public interface ITrTradeStatisticsService
{
    /**
     * Query platform overview data.
     *
     * @return overview statistics
     */
    TrTradeStatisticsOverviewVo selectOverview();

    /**
     * Query category statistics.
     *
     * @return category statistics list
     */
    List<TrTradeStatisticsCategoryVo> selectCategoryStatistics();

    /**
     * Query order trend statistics.
     *
     * @param query trend query
     * @return trend list
     */
    List<TrTradeStatisticsOrderTrendVo> selectOrderTrend(TrTradeStatisticsTrendQuery query);

    /**
     * Query payment trend statistics.
     *
     * @param query trend query
     * @return trend list
     */
    List<TrTradeStatisticsPaymentTrendVo> selectPaymentTrend(TrTradeStatisticsTrendQuery query);
}
