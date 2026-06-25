package com.ruoyi.trade.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 移动端举报出参。
 *
 * <p>在举报记录基础上联表回填被举报商品的标题、价格、封面图、状态，供"我的举报"列表
 * 渲染商品卡片；剔除 reportUserId、handleUserId、del_flag 等内部字段。</p>
 */
public class AppReportVo
{
    /** 举报 ID。 */
    private Long reportId;

    /** 被举报商品 ID。 */
    private Long goodsId;

    /** 关联订单 ID。 */
    private Long orderId;

    /** 举报类型。 */
    private String reportType;

    /** 举报描述。 */
    private String reportContent;

    /** 证据图片，多个用逗号分隔。 */
    private String evidenceImages;

    /** 处理状态：0待处理 1已处理 2已驳回。 */
    private String handleStatus;

    /** 处理时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 处理结果。 */
    private String handleResult;

    /** 提交时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 商品标题（商品已删除时为 null）。 */
    private String goodsTitle;

    /** 商品价格。 */
    private BigDecimal goodsPrice;

    /** 商品状态：0待审核 1已上架 2审核拒绝 3已下架 4已售出。 */
    private String goodsStatus;

    /** 商品封面图（第一张图片，相对或绝对路径）。 */
    private String goodsCoverImage;

    public Long getReportId()
    {
        return reportId;
    }

    public void setReportId(Long reportId)
    {
        this.reportId = reportId;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getReportType()
    {
        return reportType;
    }

    public void setReportType(String reportType)
    {
        this.reportType = reportType;
    }

    public String getReportContent()
    {
        return reportContent;
    }

    public void setReportContent(String reportContent)
    {
        this.reportContent = reportContent;
    }

    public String getEvidenceImages()
    {
        return evidenceImages;
    }

    public void setEvidenceImages(String evidenceImages)
    {
        this.evidenceImages = evidenceImages;
    }

    public String getHandleStatus()
    {
        return handleStatus;
    }

    public void setHandleStatus(String handleStatus)
    {
        this.handleStatus = handleStatus;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public String getHandleResult()
    {
        return handleResult;
    }

    public void setHandleResult(String handleResult)
    {
        this.handleResult = handleResult;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public String getGoodsTitle()
    {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle)
    {
        this.goodsTitle = goodsTitle;
    }

    public BigDecimal getGoodsPrice()
    {
        return goodsPrice;
    }

    public void setGoodsPrice(BigDecimal goodsPrice)
    {
        this.goodsPrice = goodsPrice;
    }

    public String getGoodsStatus()
    {
        return goodsStatus;
    }

    public void setGoodsStatus(String goodsStatus)
    {
        this.goodsStatus = goodsStatus;
    }

    public String getGoodsCoverImage()
    {
        return goodsCoverImage;
    }

    public void setGoodsCoverImage(String goodsCoverImage)
    {
        this.goodsCoverImage = goodsCoverImage;
    }
}
