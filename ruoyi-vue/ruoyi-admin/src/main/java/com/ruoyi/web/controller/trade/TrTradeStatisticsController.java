package com.ruoyi.web.controller.trade;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.trade.domain.query.TrTradeStatisticsTrendQuery;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsCategoryVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsOrderTrendVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsOverviewVo;
import com.ruoyi.trade.domain.vo.TrTradeStatisticsPaymentTrendVo;
import com.ruoyi.trade.service.ITrTradeStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Trade statistics controller.
 *
 * @author lyl
 */
@Tag(name = "交易数据统计")
@RestController
@RequestMapping("/trade/statistics")
public class TrTradeStatisticsController extends BaseController
{
    @Autowired
    private ITrTradeStatisticsService trTradeStatisticsService;

    /**
     * Query platform overview statistics.
     */
    @Operation(summary = "平台概览数据", description = "返回商品、订单、交易、用户、举报、争议等核心汇总指标")
    @PreAuthorize("@ss.hasPermi('trade:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        TrTradeStatisticsOverviewVo overview = trTradeStatisticsService.selectOverview();
        return success(overview);
    }

    /**
     * Query category statistics.
     */
    @Operation(summary = "分类商品统计", description = "按商品分类统计商品总数和已成交订单数")
    @PreAuthorize("@ss.hasPermi('trade:statistics:list')")
    @GetMapping("/category")
    public AjaxResult category()
    {
        List<TrTradeStatisticsCategoryVo> categoryStatistics = trTradeStatisticsService.selectCategoryStatistics();
        return success(categoryStatistics);
    }

    /**
     * Query order trend statistics.
     */
    @Operation(summary = "订单趋势", description = "按日期范围统计每日新增、完成、取消订单数")
    @PreAuthorize("@ss.hasPermi('trade:statistics:list')")
    @GetMapping("/orderTrend")
    public AjaxResult orderTrend(TrTradeStatisticsTrendQuery query)
    {
        List<TrTradeStatisticsOrderTrendVo> orderTrend = trTradeStatisticsService.selectOrderTrend(query);
        return success(orderTrend);
    }

    /**
     * Query payment trend statistics.
     */
    @Operation(summary = "支付趋势", description = "按日期范围统计每日支付笔数和支付总金额")
    @PreAuthorize("@ss.hasPermi('trade:statistics:list')")
    @GetMapping("/paymentTrend")
    public AjaxResult paymentTrend(TrTradeStatisticsTrendQuery query)
    {
        List<TrTradeStatisticsPaymentTrendVo> paymentTrend = trTradeStatisticsService.selectPaymentTrend(query);
        return success(paymentTrend);
    }
}
