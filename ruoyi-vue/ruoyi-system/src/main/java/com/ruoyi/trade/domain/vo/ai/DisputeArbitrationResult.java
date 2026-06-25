package com.ruoyi.trade.domain.vo.ai;

/**
 * AI 争议仲裁结构化返回。
 *
 * <p>由 langchain4j AiServices 按 prompt 中的 JSON schema 自动反序列化。
 * 字段名与 prompt 中要求 AI 返回的 JSON key 一一对应，禁止随意改名。
 * 注意：落库时 arbitrateLevel 映射到 TrAiAuditRecord.riskLevel；reason 映射到 riskReason。</p>
 */
public class DisputeArbitrationResult
{
    /** 仲裁倾向：buyer / buyer_偏买家 / seller / seller_偏卖家 / 双方责任 */
    private String arbitrateLevel;

    /** 仲裁建议：退款给买家 / 买家确认收货 / 双方协商 / 人工介入 */
    private String suggestion;

    /** 仲裁理由 */
    private String reason;

    public String getArbitrateLevel()
    {
        return arbitrateLevel;
    }

    public void setArbitrateLevel(String arbitrateLevel)
    {
        this.arbitrateLevel = arbitrateLevel;
    }

    public String getSuggestion()
    {
        return suggestion;
    }

    public void setSuggestion(String suggestion)
    {
        this.suggestion = suggestion;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
