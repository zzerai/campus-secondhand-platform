package com.ruoyi.trade.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * AI审核记录对象 tr_ai_audit_record
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrAiAuditRecord extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** AI审核记录ID */
    private Long recordId;

    /** 业务ID，如商品ID或争议ID */
    @Excel(name = "业务ID，如商品ID或争议ID")
    private Long businessId;

    /** 业务类型：goods商品/dispute争议/report举报 */
    @Excel(name = "业务类型：goods商品/dispute争议/report举报")
    private String businessType;

    /** AI输入内容 */
    @Excel(name = "AI输入内容")
    private String inputContent;

    /** AI返回完整结果 */
    @Excel(name = "AI返回完整结果")
    private String aiResult;

    /** 风险等级：low低，middle中，high高 */
    @Excel(name = "风险等级：low低，middle中，high高")
    private String riskLevel;

    /** 审核建议：通过/拒绝/人工复核 */
    @Excel(name = "审核建议：通过/拒绝/人工复核")
    private String suggestion;

    /** 风险原因 */
    @Excel(name = "风险原因")
    private String riskReason;

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

    public void setBusinessId(Long businessId) 
    {
        this.businessId = businessId;
    }

    public Long getBusinessId() 
    {
        return businessId;
    }

    public void setBusinessType(String businessType) 
    {
        this.businessType = businessType;
    }

    public String getBusinessType() 
    {
        return businessType;
    }

    public void setInputContent(String inputContent) 
    {
        this.inputContent = inputContent;
    }

    public String getInputContent() 
    {
        return inputContent;
    }

    public void setAiResult(String aiResult) 
    {
        this.aiResult = aiResult;
    }

    public String getAiResult() 
    {
        return aiResult;
    }

    public void setRiskLevel(String riskLevel) 
    {
        this.riskLevel = riskLevel;
    }

    public String getRiskLevel() 
    {
        return riskLevel;
    }

    public void setSuggestion(String suggestion) 
    {
        this.suggestion = suggestion;
    }

    public String getSuggestion() 
    {
        return suggestion;
    }

    public void setRiskReason(String riskReason) 
    {
        this.riskReason = riskReason;
    }

    public String getRiskReason() 
    {
        return riskReason;
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
            .append("businessId", getBusinessId())
            .append("businessType", getBusinessType())
            .append("inputContent", getInputContent())
            .append("aiResult", getAiResult())
            .append("riskLevel", getRiskLevel())
            .append("suggestion", getSuggestion())
            .append("riskReason", getRiskReason())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
