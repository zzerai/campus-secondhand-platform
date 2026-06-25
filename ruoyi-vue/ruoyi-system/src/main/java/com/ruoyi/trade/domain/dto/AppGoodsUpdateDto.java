package com.ruoyi.trade.domain.dto;

import java.math.BigDecimal;

/**
 * 移动端商品修改入参。
 *
 * <p>与 {@link AppGoodsPublishDto} 同字段集 + 必填 goodsId。同样不接受审核字段、统计字段、
 * 状态字段、卖家ID 等，更新后服务端会强制把状态重置为待审核。</p>
 */
public class AppGoodsUpdateDto
{
    /** 商品ID（必填）。 */
    private Long goodsId;

    private String title;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String quality;
    private String description;
    private String tradePlace;
    private String contactWay;
    private Long categoryId;
    private String imageUrls;
    private String remark;

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }

    public BigDecimal getOriginalPrice()
    {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice)
    {
        this.originalPrice = originalPrice;
    }

    public String getQuality()
    {
        return quality;
    }

    public void setQuality(String quality)
    {
        this.quality = quality;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getTradePlace()
    {
        return tradePlace;
    }

    public void setTradePlace(String tradePlace)
    {
        this.tradePlace = tradePlace;
    }

    public String getContactWay()
    {
        return contactWay;
    }

    public void setContactWay(String contactWay)
    {
        this.contactWay = contactWay;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long categoryId)
    {
        this.categoryId = categoryId;
    }

    public String getImageUrls()
    {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls)
    {
        this.imageUrls = imageUrls;
    }

    public String getRemark()
    {
        return remark;
    }

    public void setRemark(String remark)
    {
        this.remark = remark;
    }
}
