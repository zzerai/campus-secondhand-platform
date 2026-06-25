package com.ruoyi.trade.domain.dto;

/**
 * 移动端争议提交入参。
 *
 * <p>仅接收用户可填写的字段，发起人、被申诉人、处理状态等由服务端依据订单推导，
 * 避免客户端注入 handleStatus / handleResult / aiAnalysis / handleUserId 等字段。</p>
 */
public class AppDisputeSubmitDto
{
    /** 订单 ID。 */
    private Long orderId;

    /** 争议类型：未交货/商品不符/付款问题/其他。 */
    private String disputeType;

    /** 争议描述。 */
    private String disputeContent;

    /** 证据图片，多个用逗号分隔。 */
    private String evidenceImages;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getDisputeType()
    {
        return disputeType;
    }

    public void setDisputeType(String disputeType)
    {
        this.disputeType = disputeType;
    }

    public String getDisputeContent()
    {
        return disputeContent;
    }

    public void setDisputeContent(String disputeContent)
    {
        this.disputeContent = disputeContent;
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
