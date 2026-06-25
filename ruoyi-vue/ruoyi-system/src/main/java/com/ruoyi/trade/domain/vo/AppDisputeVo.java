package com.ruoyi.trade.domain.vo;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.trade.domain.TrTradeDispute;

/**
 * 移动端争议出参。
 *
 * <p>暴露争议双方需要看到的字段（申诉内容、AI分析、管理端仲裁结论、责任判定、退款信息），
 * 剔除 handleUserId、del_flag、审计字段等内部信息。</p>
 */
public class AppDisputeVo
{
    /** 争议 ID。 */
    private Long disputeId;

    /** 发起人 ID。 */
    private Long applicantId;

    /** 订单 ID。 */
    private Long orderId;

    /** 订单编号。 */
    private String orderNo;

    /** 订单创建时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date orderCreateTime;

    /** 商品 ID。 */
    private Long goodsId;

    /** 争议类型。 */
    private String disputeType;

    /** 争议描述。 */
    private String disputeContent;

    /** 证据图片，多个用逗号分隔。 */
    private String evidenceImages;

    /** AI 仲裁分析结果。 */
    private String aiAnalysis;

    /** 处理状态。 */
    private String handleStatus;

    /** 处理结果。 */
    private String handleResult;

    /** 责任判定：respondent/applicant/both/none。 */
    private String faultParty;

    /** 订单退款状态（来自 tr_trade_order）。 */
    private String refundStatus;

    /** 退款金额（来自 tr_trade_order）。 */
    private BigDecimal refundAmount;

    /** 处理时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date handleTime;

    /** 提交时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 由争议实体转换为出参对象。
     *
     * @param dispute 争议实体
     * @return 出参对象，入参为 {@code null} 时返回 {@code null}
     */
    public static AppDisputeVo from(TrTradeDispute dispute)
    {
        if (dispute == null)
        {
            return null;
        }
        AppDisputeVo vo = new AppDisputeVo();
        vo.disputeId = dispute.getDisputeId();
        vo.applicantId = dispute.getApplicantId();
        vo.orderId = dispute.getOrderId();
        vo.goodsId = dispute.getGoodsId();
        vo.disputeType = dispute.getDisputeType();
        vo.disputeContent = dispute.getDisputeContent();
        vo.evidenceImages = dispute.getEvidenceImages();
        vo.aiAnalysis = dispute.getAiAnalysis();
        vo.handleStatus = dispute.getHandleStatus();
        vo.handleResult = dispute.getHandleResult();
        vo.faultParty = dispute.getFaultParty();
        vo.handleTime = dispute.getHandleTime();
        vo.createTime = dispute.getCreateTime();
        return vo;
    }

    public Long getDisputeId()
    {
        return disputeId;
    }

    public void setDisputeId(Long disputeId)
    {
        this.disputeId = disputeId;
    }

    public Long getApplicantId()
    {
        return applicantId;
    }

    public void setApplicantId(Long applicantId)
    {
        this.applicantId = applicantId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Date getOrderCreateTime()
    {
        return orderCreateTime;
    }

    public void setOrderCreateTime(Date orderCreateTime)
    {
        this.orderCreateTime = orderCreateTime;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
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

    public String getAiAnalysis()
    {
        return aiAnalysis;
    }

    public void setAiAnalysis(String aiAnalysis)
    {
        this.aiAnalysis = aiAnalysis;
    }

    public String getHandleStatus()
    {
        return handleStatus;
    }

    public void setHandleStatus(String handleStatus)
    {
        this.handleStatus = handleStatus;
    }

    public String getHandleResult()
    {
        return handleResult;
    }

    public void setHandleResult(String handleResult)
    {
        this.handleResult = handleResult;
    }

    public String getFaultParty()
    {
        return faultParty;
    }

    public void setFaultParty(String faultParty)
    {
        this.faultParty = faultParty;
    }

    public String getRefundStatus()
    {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus)
    {
        this.refundStatus = refundStatus;
    }

    public BigDecimal getRefundAmount()
    {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount)
    {
        this.refundAmount = refundAmount;
    }

    public Date getHandleTime()
    {
        return handleTime;
    }

    public void setHandleTime(Date handleTime)
    {
        this.handleTime = handleTime;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }
}
