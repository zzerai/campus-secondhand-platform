package com.ruoyi.trade.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 举报信息对象 tr_trade_report
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeReport extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 举报ID */
    private Long reportId;

    /** 被举报商品ID */
    @Excel(name = "被举报商品ID")
    private Long goodsId;

    /** 关联订单ID */
    @Excel(name = "关联订单ID")
    private Long orderId;

    /** 举报人ID */
    @Excel(name = "举报人ID")
    private Long reportUserId;

    /** 被举报人ID */
    @Excel(name = "被举报人ID")
    private Long reportedUserId;

    /** 举报类型：虚假信息/违禁品/价格欺诈/交易纠纷/其他 */
    @Excel(name = "举报类型：虚假信息/违禁品/价格欺诈/交易纠纷/其他")
    private String reportType;

    /** 举报内容 */
    @Excel(name = "举报内容")
    private String reportContent;

    /** 证据图片，多个用逗号分隔 */
    @Excel(name = "证据图片，多个用逗号分隔")
    private String evidenceImages;

    /** 处理状态：0待处理，1已处理，2已驳回 */
    @Excel(name = "处理状态：0待处理，1已处理，2已驳回")
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

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setReportId(Long reportId) 
    {
        this.reportId = reportId;
    }

    public Long getReportId() 
    {
        return reportId;
    }

    public void setGoodsId(Long goodsId) 
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setReportUserId(Long reportUserId) 
    {
        this.reportUserId = reportUserId;
    }

    public Long getReportUserId() 
    {
        return reportUserId;
    }

    public void setReportedUserId(Long reportedUserId) 
    {
        this.reportedUserId = reportedUserId;
    }

    public Long getReportedUserId() 
    {
        return reportedUserId;
    }

    public void setReportType(String reportType) 
    {
        this.reportType = reportType;
    }

    public String getReportType() 
    {
        return reportType;
    }

    public void setReportContent(String reportContent) 
    {
        this.reportContent = reportContent;
    }

    public String getReportContent() 
    {
        return reportContent;
    }

    public void setEvidenceImages(String evidenceImages) 
    {
        this.evidenceImages = evidenceImages;
    }

    public String getEvidenceImages() 
    {
        return evidenceImages;
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
            .append("reportId", getReportId())
            .append("goodsId", getGoodsId())
            .append("orderId", getOrderId())
            .append("reportUserId", getReportUserId())
            .append("reportedUserId", getReportedUserId())
            .append("reportType", getReportType())
            .append("reportContent", getReportContent())
            .append("evidenceImages", getEvidenceImages())
            .append("handleStatus", getHandleStatus())
            .append("handleUserId", getHandleUserId())
            .append("handleTime", getHandleTime())
            .append("handleResult", getHandleResult())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
