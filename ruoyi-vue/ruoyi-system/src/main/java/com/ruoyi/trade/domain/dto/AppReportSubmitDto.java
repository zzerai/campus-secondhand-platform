package com.ruoyi.trade.domain.dto;

/**
 * 移动端举报提交入参。
 *
 * <p>仅接收用户可填写的字段，举报人、被举报人、处理状态等由服务端推导，
 * 避免客户端注入 handleStatus / handleResult / handleUserId 等字段。</p>
 */
public class AppReportSubmitDto
{
    /** 被举报商品 ID。 */
    private Long goodsId;

    /** 关联订单 ID（可选）。 */
    private Long orderId;

    /** 举报类型：虚假信息/违禁品/价格欺诈/交易纠纷/其他。 */
    private String reportType;

    /** 举报内容。 */
    private String reportContent;

    /** 证据图片，多个用逗号分隔。 */
    private String evidenceImages;

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
}
