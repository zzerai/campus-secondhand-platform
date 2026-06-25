package com.ruoyi.trade.domain.query;

import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Trade statistics trend query.
 *
 * @author lyl
 */
public class TrTradeStatisticsTrendQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Start date, inclusive. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    /** End date, inclusive. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    /** Number of days when explicit dates are not provided. */
    private Integer days;

    public Date getStartDate()
    {
        return startDate;
    }

    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }

    public Date getEndDate()
    {
        return endDate;
    }

    public void setEndDate(Date endDate)
    {
        this.endDate = endDate;
    }

    public Integer getDays()
    {
        return days;
    }

    public void setDays(Integer days)
    {
        this.days = days;
    }
}
