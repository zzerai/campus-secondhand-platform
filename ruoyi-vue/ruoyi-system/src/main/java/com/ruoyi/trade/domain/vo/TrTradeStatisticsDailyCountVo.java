package com.ruoyi.trade.domain.vo;

import java.io.Serializable;

/**
 * Daily count statistics row.
 *
 * @author lyl
 */
public class TrTradeStatisticsDailyCountVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String statDate;

    private Long totalCount;

    public String getStatDate()
    {
        return statDate;
    }

    public void setStatDate(String statDate)
    {
        this.statDate = statDate;
    }

    public Long getTotalCount()
    {
        return totalCount;
    }

    public void setTotalCount(Long totalCount)
    {
        this.totalCount = totalCount;
    }
}
