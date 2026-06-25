package com.ruoyi.trade.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 交易争议处理对象 tr_trade_dispute
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeDispute extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 争议ID */
    private Long disputeId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 商品ID */
    @Excel(name = "商品ID")
    private Long goodsId;

    /** 发起人ID */
    @Excel(name = "发起人ID")
    private Long applicantId;

    /** 被申诉人ID */
    @Excel(name = "被申诉人ID")
    private Long respondentId;

    /** 争议类型：未交货/商品不符/付款问题/其他 */
    @Excel(name = "争议类型：未交货/商品不符/付款问题/其他")
    private String disputeType;

    /** 争议描述 */
    @Excel(name = "争议描述")
    private String disputeContent;

    /** 证据图片，多个用逗号分隔 */
    @Excel(name = "证据图片，多个用逗号分隔")
    private String evidenceImages;

    /** AI仲裁分析结果 */
    @Excel(name = "AI仲裁分析结果")
    private String aiAnalysis;

    /** 处理状态：0待AI分析，1AI分析中，2等待人工仲裁，3已处理 */
    @Excel(name = "处理状态", readConverterExp = "0=待AI分析,1=AI分析中,2=等待人工仲裁,3=已处理")
    private String handleStatus;

    /** 处理管理员ID */
    @Excel(name = "处理管理员ID")
    private Long handleUserId;

    /** 处理时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "处理时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date handleTime;

    /** 处理结果 */
    @Excel(name = "处理结果")
    private String handleResult;

    /** 责任判定：respondent/applicant/both/none */
    @Excel(name = "责任判定", readConverterExp = "respondent=被申诉人,applicant=发起人,both=双方,none=不判责")
    private String faultParty;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setDisputeId(Long disputeId) 
    {
        this.disputeId = disputeId;
    }

    public Long getDisputeId() 
    {
        return disputeId;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setGoodsId(Long goodsId) 
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setApplicantId(Long applicantId) 
    {
        this.applicantId = applicantId;
    }

    public Long getApplicantId() 
    {
        return applicantId;
    }

    public void setRespondentId(Long respondentId) 
    {
        this.respondentId = respondentId;
    }

    public Long getRespondentId() 
    {
        return respondentId;
    }

    public void setDisputeType(String disputeType) 
    {
        this.disputeType = disputeType;
    }

    public String getDisputeType() 
    {
        return disputeType;
    }

    public void setDisputeContent(String disputeContent) 
    {
        this.disputeContent = disputeContent;
    }

    public String getDisputeContent() 
    {
        return disputeContent;
    }

    public void setEvidenceImages(String evidenceImages) 
    {
        this.evidenceImages = evidenceImages;
    }

    public String getEvidenceImages() 
    {
        return evidenceImages;
    }

    public void setAiAnalysis(String aiAnalysis) 
    {
        this.aiAnalysis = aiAnalysis;
    }

    public String getAiAnalysis() 
    {
        return aiAnalysis;
    }

    public void setHandleStatus(String handleStatus) 
    {
        this.handleStatus = handleStatus;
    }

    public String getHandleStatus() 
    {
        return handleStatus;
    }

    public void setHandleUserId(Long handleUserId) 
    {
        this.handleUserId = handleUserId;
    }

    public Long getHandleUserId() 
    {
        return handleUserId;
    }

    public void setHandleTime(Date handleTime) 
    {
        this.handleTime = handleTime;
    }

    public Date getHandleTime() 
    {
        return handleTime;
    }

    public void setHandleResult(String handleResult) 
    {
        this.handleResult = handleResult;
    }

    public String getHandleResult()
    {
        return handleResult;
    }

    public void setFaultParty(String faultParty)
    {
        this.faultParty = faultParty;
    }

    public String getFaultParty()
    {
        return faultParty;
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
            .append("disputeId", getDisputeId())
            .append("orderId", getOrderId())
            .append("goodsId", getGoodsId())
            .append("applicantId", getApplicantId())
            .append("respondentId", getRespondentId())
            .append("disputeType", getDisputeType())
            .append("disputeContent", getDisputeContent())
            .append("evidenceImages", getEvidenceImages())
            .append("aiAnalysis", getAiAnalysis())
            .append("handleStatus", getHandleStatus())
            .append("handleUserId", getHandleUserId())
            .append("handleTime", getHandleTime())
            .append("handleResult", getHandleResult())
            .append("faultParty", getFaultParty())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
