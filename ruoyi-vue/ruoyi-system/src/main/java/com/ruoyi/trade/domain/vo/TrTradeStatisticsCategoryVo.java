package com.ruoyi.trade.domain.vo;

import java.io.Serializable;

/**
 * Trade category statistics.
 *
 * @author lyl
 */
public class TrTradeStatisticsCategoryVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long categoryId;

    private String categoryName;

    private Long goodsCount;

    private Long completedOrderCount;

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public Long getGoodsCount()
    {
        return goodsCount;
    }

    public void setGoodsCount(Long goodsCount)
    {
        this.goodsCount = goodsCount;
    }

    public Long getCompletedOrderCount()
    {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(Long completedOrderCount)
    {
        this.completedOrderCount = completedOrderCount;
    }
}
