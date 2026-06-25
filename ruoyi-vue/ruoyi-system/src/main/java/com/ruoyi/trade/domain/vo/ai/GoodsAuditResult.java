package com.ruoyi.trade.domain.vo.ai;

/**
 * AI 商品审核结构化返回。
 *
 * <p>由 langchain4j AiServices 按 prompt 中的 JSON schema 自动反序列化。
 * 字段名与 prompt 中要求 AI 返回的 JSON key 一一对应，禁止随意改名。</p>
 */
public class GoodsAuditResult
{
    /** 风险等级：low / middle / high */
    private String riskLevel;

    /** 审核建议：通过 / 拒绝 / 人工复核 */
    private String suggestion;

    /** 具体的风险原因或审核依据 */
    private String riskReason;

    public String getRiskLevel()
    {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel)
    {
        this.riskLevel = riskLevel;
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    public String getRiskReason()
    {
        return riskReason;
    }

    public void setRiskReason(String riskReason)
    {
        this.riskReason = riskReason;
    }
}
