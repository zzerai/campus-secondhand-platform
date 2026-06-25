package com.ruoyi.trade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.trade.service.ai.DisputeArbitrationAi;
import com.ruoyi.trade.service.ai.GoodsAuditAi;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;

/**
 * langchain4j AiServices Bean 注册。
 *
 * <p>把声明式 AI 接口（{@link GoodsAuditAi} / {@link DisputeArbitrationAi}）绑定到
 * 由 langchain4j-community-dashscope-spring-boot-starter 自动注入的 {@link ChatModel}
 * （模型与凭据由 application.yml 的 {@code langchain4j.community.dashscope.chat-model} 控制）。</p>
 *
 * <p>不在此处叠加重试 / 超时：DashScope starter 已配 {@code max-retries: 1}，
 * 单次调用硬超时由调用方 {@link com.ruoyi.trade.utils.AiCallExecutor} 统一施加，
 * 便于在 starter 暴露 timeout 字段前保持兜底一致。</p>
 */
@Configuration
public class AiServiceConfig
{
    @Bean
    public GoodsAuditAi goodsAuditAi(ChatModel chatModel)
    {
        return AiServices.builder(GoodsAuditAi.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public DisputeArbitrationAi disputeArbitrationAi(ChatModel chatModel)
    {
        return AiServices.builder(DisputeArbitrationAi.class)
                .chatModel(chatModel)
                .build();
    }
}
