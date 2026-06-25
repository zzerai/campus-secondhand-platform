package com.ruoyi.trade.domain.vo;

import java.io.Serializable;

/**
 * Trade order trend.
 *
 * @author lyl
 */
public class TrTradeStatisticsOrderTrendVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String statDate;

    private Long newOrderCount;

    private Long completedOrderCount;

    private Long cancelledOrderCount;

    public String getStatDate()
    {
        return statDate;
    }

    public void setStatDate(String statDate)
    {
        this.statDate = statDate;
    }

    public Long getNewOrderCount()
    {
        return newOrderCount;
    }

    public void setNewOrderCount(Long newOrderCount)
    {
        this.newOrderCount = newOrderCount;
    }

    public Long getCompletedOrderCount()
    {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Long completedOrderCount)
    {
        this.completedOrderCount = completedOrderCount;
    }

    public Long getCancelledOrderCount()
    {
        return cancelledOrderCount;
    }

    public void setCancelledOrderCount(Long cancelledOrderCount)
    {
        this.cancelledOrderCount = cancelledOrderCount;
    }
}
