package com.ruoyi.trade.domain.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Trade payment trend.
 *
 * @author lyl
 */
public class TrTradeStatisticsPaymentTrendVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String statDate;

    private Long paymentCount;

    private BigDecimal paymentAmount;

    public String getStatDate()
    {
        return statDate;
    }

    public void setStatDate(String statDate)
    {
        this.statDate = statDate;
    }

    public Long getPaymentCount()
    {
        return paymentCount;
    }

    public void setPaymentCount(Long paymentCount)
    {
        this.paymentCount = paymentCount;
    }

    public BigDecimal getPaymentAmount()
    {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount)
    {
        this.paymentAmount = paymentAmount;
    }
}
