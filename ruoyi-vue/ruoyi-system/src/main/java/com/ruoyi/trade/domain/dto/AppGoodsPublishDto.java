package com.ruoyi.trade.domain.dto;

import java.math.BigDecimal;

/**
 * 移动端商品发布入参。
 *
 * <p>仅接收用户可填写的字段，审核字段（auditUserId / auditTime / auditRemark）、统计字段
 * （viewCount / favoriteCount）、状态（goodsStatus）、卖家ID、删除标记等一律由服务端推导或固定，
 * 避免客户端注入伪造审核痕迹或刷量。</p>
 */
public class AppGoodsPublishDto
{
    /** 商品标题。 */
    private String title;

    /** 出售价格。 */
    private BigDecimal price;

    /** 原价。 */
    private BigDecimal originalPrice;

    /** 新旧程度。 */
    private String quality;

    /** 商品描述。 */
    private String description;

    /** 建议交易地点。 */
    private String tradePlace;

    /** 联系方式。 */
    private String contactWay;

    /** 商品分类 ID。 */
    private Long categoryId;

    /** 图片 URL，多个用逗号分隔；最多 9 张。 */
    private String imageUrls;

    /** 备注（可选）。 */
    private String remark;

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
