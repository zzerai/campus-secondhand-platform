package com.ruoyi.trade.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 闲置商品对象 tr_trade_goods
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeGoods extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long goodsId;

    /** 卖家学生用户ID */
    @Excel(name = "卖家学生用户ID")
    private Long sellerId;

    /** 商品分类ID */
    @Excel(name = "商品分类ID")
    private Long categoryId;

    /** 商品标题 */
    @Excel(name = "商品标题")
    private String title;

    /** 出售价格 */
    @Excel(name = "出售价格")
    private BigDecimal price;

    /** 原价 */
    @Excel(name = "原价")
    private BigDecimal originalPrice;

    /** 新旧程度 */
    @Excel(name = "新旧程度")
    private String quality;

    /** 商品描述 */
    @Excel(name = "商品描述")
    private String description;

    /** 建议交易地点 */
    @Excel(name = "建议交易地点")
    private String tradePlace;

    /** 联系方式 */
    @Excel(name = "联系方式")
    private String contactWay;

    /** 商品状态：0待审核，1已上架，2审核拒绝，3已下架，4已售出 */
    @Excel(name = "商品状态：0待审核，1已上架，2审核拒绝，3已下架，4已售出")
    private String goodsStatus;

    /** 审核管理员ID */
    @Excel(name = "审核管理员ID")
    private Long auditUserId;

    /** 审核时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "审核时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date auditTime;

    /** 审核意见 */
    @Excel(name = "审核意见")
    private String auditRemark;

    /** 浏览次数 */
    @Excel(name = "浏览次数")
    private Long viewCount;

    /** 收藏次数 */
    @Excel(name = "收藏次数")
    private Long favoriteCount;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    /** 分类名称（联表只读） */
    @Excel(name = "商品分类")
    private String categoryName;

    /** 卖家学号（联表只读） */
    @Excel(name = "卖家学号")
    private String sellerStudentNo;

    /** 卖家昵称（联表只读） */
    @Excel(name = "卖家昵称")
    private String sellerNickname;

    /** 卖家头像（联表只读，移动端列表用） */
    private String sellerAvatar;

    /** 审核管理员账号（联表只读） */
    private String auditUserName;

    /** 商品图片列表（详情接口返回） */
    private List<TrTradeGoodsImage> images;

    /**
     * 商品图片 URL，多张以英文逗号分隔（非持久化）。
     * 移动端 publishGoods/updateGoods 用此字段接收 CSV 入参；
     * 移动端列表查询通过 group_concat 子查询填充此字段。
     */
    private String imageUrls;

    /** 关键字（非持久化，移动端列表查询条件） */
    private String keyword;

    /** 排序方式：priceAsc / priceDesc / hot（非持久化，移动端列表查询） */
    private String sort;

    /** 最低价格筛选（非持久化，移动端列表查询） */
    private BigDecimal minPrice;

    /** 最高价格筛选（非持久化，移动端列表查询） */
    private BigDecimal maxPrice;

    /** 当前登录用户是否已收藏（非持久化，仅移动端商品详情返回；匿名/未登录为 null） */
    private Boolean isFavorite;

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setSellerId(Long sellerId) 
    {
        this.sellerId = sellerId;
    }

    public Long getSellerId() 
    {
        return sellerId;
    }

    public void setCategoryId(Long categoryId) 
    {
        this.categoryId = categoryId;
    }

    public Long getCategoryId() 
    {
        return categoryId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setPrice(BigDecimal price) 
    {
        this.price = price;
    }

    public BigDecimal getPrice() 
    {
        return price;
    }

    public void setOriginalPrice(BigDecimal originalPrice) 
    {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getOriginalPrice() 
    {
        return originalPrice;
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

    public void setTradePlace(String tradePlace) 
    {
        this.tradePlace = tradePlace;
    }

    public String getTradePlace() 
    {
        return tradePlace;
    }

    public void setContactWay(String contactWay) 
    {
        this.contactWay = contactWay;
    }

    public String getContactWay() 
    {
        return contactWay;
    }

    public void setGoodsStatus(String goodsStatus) 
    {
        this.goodsStatus = goodsStatus;
    }

    public String getGoodsStatus() 
    {
        return goodsStatus;
    }

    public void setAuditUserId(Long auditUserId) 
    {
        this.auditUserId = auditUserId;
    }

    public Long getAuditUserId() 
    {
        return auditUserId;
    }

    public void setAuditTime(Date auditTime) 
    {
        this.auditTime = auditTime;
    }

    public Date getAuditTime() 
    {
        return auditTime;
    }

    public void setAuditRemark(String auditRemark) 
    {
        this.auditRemark = auditRemark;
    }

    public String getAuditRemark() 
    {
        return auditRemark;
    }

    public void setViewCount(Long viewCount) 
    {
        this.viewCount = viewCount;
    }

    public Long getViewCount() 
    {
        return viewCount;
    }

    public void setFavoriteCount(Long favoriteCount) 
    {
        this.favoriteCount = favoriteCount;
    }

    public Long getFavoriteCount() 
    {
        return favoriteCount;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setCategoryName(String categoryName)
    {
        this.categoryName = categoryName;
    }

    public String getCategoryName()
    {
        return categoryName;
    }

    public void setSellerStudentNo(String sellerStudentNo)
    {
        this.sellerStudentNo = sellerStudentNo;
    }

    public String getSellerStudentNo()
    {
        return sellerStudentNo;
    }

    public void setSellerNickname(String sellerNickname)
    {
        this.sellerNickname = sellerNickname;
    }

    public String getSellerNickname()
    {
        return sellerNickname;
    }

    public void setSellerAvatar(String sellerAvatar)
    {
        this.sellerAvatar = sellerAvatar;
    }

    public String getSellerAvatar()
    {
        return sellerAvatar;
    }

    public void setAuditUserName(String auditUserName)
    {
        this.auditUserName = auditUserName;
    }

    public String getAuditUserName()
    {
        return auditUserName;
    }

    public void setImages(List<TrTradeGoodsImage> images)
    {
        this.images = images;
    }

    public List<TrTradeGoodsImage> getImages()
    {
        return images;
    }

    public String getImageUrls()
    {
        return imageUrls;
    }

    public void setImageUrls(String imageUrls)
    {
        this.imageUrls = imageUrls;
    }

    public String getKeyword()
    {
        return keyword;
    }

    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    public String getSort()
    {
        return sort;
    }

    public void setSort(String sort)
    {
        this.sort = sort;
    }

    public BigDecimal getMinPrice()
    {
        return minPrice;
    }

    public void setMinPrice(BigDecimal minPrice)
    {
        this.minPrice = minPrice;
    }

    public BigDecimal getMaxPrice()
    {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice)
    {
        this.maxPrice = maxPrice;
    }

    public Boolean getIsFavorite()
    {
        return isFavorite;
    }

    public void setIsFavorite(Boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("goodsId", getGoodsId())
            .append("sellerId", getSellerId())
            .append("categoryId", getCategoryId())
            .append("title", getTitle())
            .append("price", getPrice())
            .append("originalPrice", getOriginalPrice())
            .append("quality", getQuality())
            .append("description", getDescription())
            .append("tradePlace", getTradePlace())
            .append("contactWay", getContactWay())
            .append("goodsStatus", getGoodsStatus())
            .append("auditUserId", getAuditUserId())
            .append("auditTime", getAuditTime())
            .append("auditRemark", getAuditRemark())
            .append("viewCount", getViewCount())
            .append("favoriteCount", getFavoriteCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
