package com.ruoyi.trade.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI估价记录对象 tr_ai_price_record
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrAiPriceRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** AI估价记录ID */
    private Long recordId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private Long goodsId;

    /** 商品标题 */
    @Excel(name = "商品标题")
    private String title;

    /** 分类名称 */
    @Excel(name = "分类名称")
    private String categoryName;

    /** 新旧程度 */
    @Excel(name = "新旧程度")
    private String quality;

    /** 商品描述 */
    @Excel(name = "商品描述")
    private String description;

    /** AI建议价格 */
    @Excel(name = "AI建议价格")
    private BigDecimal suggestPrice;

    /** 估价理由 */
    @Excel(name = "估价理由")
    private String priceReason;

    /** AI返回完整结果 */
    @Excel(name = "AI返回完整结果")
    private String aiResult;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setRecordId(Long recordId) 
    {
        this.recordId = recordId;
    }

    public Long getRecordId() 
    {
        return recordId;
    }

    public void setGoodsId(Long goodsId) 
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setCategoryName(String categoryName) 
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName() 
    {
        return categoryName;
    }

    public void setQuality(String quality) 
    {
        this.quality = quality;
    }

    public String getQuality() 
    {
        return quality;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setSuggestPrice(BigDecimal suggestPrice) 
    {
        this.suggestPrice = suggestPrice;
    }

    public BigDecimal getSuggestPrice() 
    {
        return suggestPrice;
    }

    public void setPriceReason(String priceReason) 
    {
        this.priceReason = priceReason;
    }

    public String getPriceReason() 
    {
        return priceReason;
    }

    public void setAiResult(String aiResult) 
    {
        this.aiResult = aiResult;
    }

    public String getAiResult() 
    {
        return aiResult;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("recordId", getRecordId())
            .append("goodsId", getGoodsId())
            .append("title", getTitle())
            .append("categoryName", getCategoryName())
            .append("quality", getQuality())
            .append("description", getDescription())
            .append("suggestPrice", getSuggestPrice())
            .append("priceReason", getPriceReason())
            .append("aiResult", getAiResult())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
