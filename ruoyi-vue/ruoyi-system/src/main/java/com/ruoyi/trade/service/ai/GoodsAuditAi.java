package com.ruoyi.trade.service.ai;

import com.ruoyi.trade.domain.vo.ai.GoodsAuditResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI 商品审核服务（langchain4j AiServices 代理）。
 *
 * <p>提示词从 {@code resources/prompts/} 加载，运行时由 langchain4j 解析 {{var}} 占位符并调用
 * {@link dev.langchain4j.model.chat.ChatModel} 生成结构化结果。占位符值必须非 null，
 * 调用方对可选业务字段（如 originalPrice / description）需提前做"未提供"兜底。</p>
 *
 * <p>实现 Bean 由 {@link com.ruoyi.trade.config.AiServiceConfig} 注册；
 * 调用超时与异常兜底由 Service 层 {@link com.ruoyi.trade.utils.AiCallExecutor} 统一包装。</p>
 */
public interface GoodsAuditAi
{
    @SystemMessage(fromResource = "/prompts/goods-audit-system.md")
    @UserMessage(fromResource = "/prompts/goods-audit-user.md")
    GoodsAuditResult audit(@V("title") String title,
                           @V("category") String category,
                           @V("price") String price,
                           @V("originalPrice") String originalPrice,
                           @V("quality") String quality,
                           @V("description") String description,
                           @V("tradePlace") String tradePlace);
}
