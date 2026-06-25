package com.ruoyi.trade.event;

/**
 * 争议提交事件：在 {@code submitDispute} 所在事务提交之后由
 * {@code ApplicationEventPublisher} 派发，触发异步 AI 仲裁。
 *
 * <p>必须由事务内的 publisher 发出，且由 {@code @TransactionalEventListener(AFTER_COMMIT)}
 * 监听，确保异步线程读取争议记录时事务已 commit、不会出现"查不到刚插入记录"的可见性竞态。</p>
 *
 * @author daj
 * @date 2026-05-24
 */
public class DisputeSubmittedEvent
{
    private final Long disputeId;

    public DisputeSubmittedEvent(Long disputeId)
    {
        this.disputeId = disputeId;
    }

    public Long getDisputeId()
    {
        return disputeId;
    }
}
