package com.ruoyi.trade.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.ruoyi.trade.service.ITrAiDisputeArbitrationService;

/**
 * 监听 {@link DisputeSubmittedEvent}：在 submitDispute 事务 commit 之后触发异步 AI 仲裁。
 *
 * <p>抽到独立 Bean 而非写在 {@code TrAiDisputeArbitrationServiceImpl} 实现类上，原因是
 * Spring 默认基于接口做 JDK 动态代理，接口中没有的方法（如 onDisputeSubmitted）无法通过
 * 代理被 Spring 事件机制反射调用，会启动时报
 * {@code "not found in any interface(s) of the exposed proxy type"}。强制 CGLIB 又会影响全局。</p>
 *
 * <p>本类自身不加 {@code @Async}：所调用的 {@code arbitrateDisputeAsync} 本身就带 @Async，
 * 跨 Bean 调用代理生效，立即入队列返回，主线程不会被 AI 调用阻塞。</p>
 *
 * @author daj
 * @date 2026-05-24
 */
@Component
public class DisputeArbitrationEventListener
{
    @Autowired
    private ITrAiDisputeArbitrationService trAiDisputeArbitrationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDisputeSubmitted(DisputeSubmittedEvent event)
    {
        trAiDisputeArbitrationService.arbitrateDisputeAsync(event.getDisputeId());
    }
}
