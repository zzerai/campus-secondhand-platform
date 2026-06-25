package com.ruoyi.trade.service.ai;

import com.ruoyi.trade.domain.vo.ai.DisputeArbitrationResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI 争议仲裁服务（langchain4j AiServices 代理）。
 *
 * <p>提示词从 {@code resources/prompts/} 加载。{@code goodsInfo} 占位符由 Service 层动态拼装
 * （存在商品时为多行商品信息文本，不存在时为"无关联商品信息"），以兼容 dispute.goods_id 可空场景。</p>
 *
 * <p>实现 Bean 由 {@link com.ruoyi.trade.config.AiServiceConfig} 注册；
 * 调用超时与异常兜底由 Service 层 {@link com.ruoyi.trade.utils.AiCallExecutor} 统一包装。</p>
 */
public interface DisputeArbitrationAi
{
    @SystemMessage(fromResource = "/prompts/dispute-arbitration-system.md")
    @UserMessage(fromResource = "/prompts/dispute-arbitration-user.md")
    DisputeArbitrationResult arbitrate(@V("disputeType") String disputeType,
                                       @V("disputeContent") String disputeContent,
                                       @V("orderId") String orderId,
                                       @V("tradePrice") String tradePrice,
                                       @V("orderStatus") String orderStatus,
                                       @V("goodsInfo") String goodsInfo);
}
