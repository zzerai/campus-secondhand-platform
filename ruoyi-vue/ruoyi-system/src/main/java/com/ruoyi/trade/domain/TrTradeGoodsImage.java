package com.ruoyi.trade.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 商品图片对象 tr_trade_goods_image
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeGoodsImage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 图片ID */
    private Long imageId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private Long goodsId;

    /** 图片地址 */
    @Excel(name = "图片地址")
    private String imageUrl;

    /** 排序 */
    @Excel(name = "排序")
    private Long sort;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setImageId(Long imageId) 
    {
        this.imageId = imageId;
    }

    public Long getImageId() 
    {
        return imageId;
    }

    public void setGoodsId(Long goodsId) 
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setImageUrl(String imageUrl) 
    {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() 
    {
        return imageUrl;
    }

    public void setSort(Long sort) 
    {
        this.sort = sort;
    }

    public Long getSort() 
    {
        return sort;
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
            .append("imageId", getImageId())
            .append("goodsId", getGoodsId())
            .append("imageUrl", getImageUrl())
            .append("sort", getSort())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
