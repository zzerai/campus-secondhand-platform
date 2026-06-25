package com.ruoyi.trade.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Trade statistics overview.
 *
 * @author lyl
 */
public class TrTradeStatisticsOverviewVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long goodsTotal;

    private Long goodsPendingAudit;

    private Long goodsOnShelf;

    private Long orderTotal;

    private Long todayNewOrderCount;

    private Long completedOrderCount;

    private BigDecimal totalTradeAmount;

    private Long studentUserTotal;

    private Long todayNewUserCount;

    private Long pendingReportCount;

    private Long pendingDisputeCount;

    public Long getGoodsTotal()
    {
        return goodsTotal;
    }

    public void setGoodsTotal(Long goodsTotal)
    {
        this.goodsTotal = goodsTotal;
    }

    public Long getGoodsPendingAudit()
    {
        return goodsPendingAudit;
    }

    public void setGoodsPendingAudit(Long goodsPendingAudit)
    {
        this.goodsPendingAudit = goodsPendingAudit;
    }

    public Long getGoodsOnShelf()
    {
        return goodsOnShelf;
    }

    public void setGoodsOnShelf(Long goodsOnShelf)
    {
        this.goodsOnShelf = goodsOnShelf;
    }

    public Long getOrderTotal()
    {
        return orderTotal;
    }

    public void setOrderTotal(Long orderTotal)
    {
        this.orderTotal = orderTotal;
    }

    public Long getTodayNewOrderCount()
    {
        return todayNewOrderCount;
    }

    public void setTodayNewOrderCount(Long todayNewOrderCount)
    {
        this.todayNewOrderCount = todayNewOrderCount;
    }

    public Long getCompletedOrderCount()
    {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Long completedOrderCount)
    {
        this.completedOrderCount = completedOrderCount;
    }

    public BigDecimal getTotalTradeAmount()
    {
        return totalTradeAmount;
    }

    public void setTotalTradeAmount(BigDecimal totalTradeAmount)
    {
        this.totalTradeAmount = totalTradeAmount;
    }

    public Long getStudentUserTotal()
    {
        return studentUserTotal;
    }

    public void setStudentUserTotal(Long studentUserTotal)
    {
        this.studentUserTotal = studentUserTotal;
    }

    public Long getTodayNewUserCount()
    {
        return todayNewUserCount;
    }

    public void setTodayNewUserCount(Long todayNewUserCount)
    {
        this.todayNewUserCount = todayNewUserCount;
    }

    public Long getPendingReportCount()
    {
        return pendingReportCount;
    }

    public void setPendingReportCount(Long pendingReportCount)
    {
        this.pendingReportCount = pendingReportCount;
    }

    public Long getPendingDisputeCount()
    {
        return pendingDisputeCount;
    }

    public void setPendingDisputeCount(Long pendingDisputeCount)
    {
        this.pendingDisputeCount = pendingDisputeCount;
    }
}
