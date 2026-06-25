package com.ruoyi.trade.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import com.ruoyi.common.exception.ServiceException;

/**
 * AI 调用的统一超时包装。
 *
 * <p>langchain4j community-dashscope-spring-boot-starter 暂未暴露 timeout 字段，
 * 实测某些情况下 DashScope 服务端会长时间 hang 住请求；以本工具用
 * {@link CompletableFuture#orTimeout} 在调用方一侧强制硬超时。
 * 超时或下游异常统一抛 {@link ServiceException}，调用方据此决定是抛给前端还是
 * 走异步任务的兜底（如 {@code TrAiDisputeArbitrationServiceImpl.arbitrateDisputeAsync}）。</p>
 */
public final class AiCallExecutor
{
    private AiCallExecutor() {}

    /**
     * 在指定秒数内执行 AI 调用，超时抛 ServiceException。
     *
     * @param call         实际 AI 调用（通常是一个 langchain4j AiService 方法引用）
     * @param seconds      超时秒数
     * @param errorPrefix  抛错时的业务前缀，例如 "AI审核" / "AI仲裁"
     * @param <T>          返回类型（任意结构化 POJO 或 String）
     */
    public static <T> T callWithTimeout(Supplier<T> call, long seconds, String errorPrefix)
    {
        try
        {
            return CompletableFuture.supplyAsync(call)
                    .orTimeout(seconds, TimeUnit.SECONDS)
                    .join();
        }
        catch (CompletionException ce)
        {
            Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
            if (cause instanceof TimeoutException)
            {
                throw new ServiceException(errorPrefix + "超时（" + seconds + "s）");
            }
            throw new ServiceException(errorPrefix + "调用失败: " + cause.getMessage());
        }
    }
}
