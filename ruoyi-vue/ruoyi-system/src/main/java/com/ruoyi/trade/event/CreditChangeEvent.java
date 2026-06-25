package com.ruoyi.trade.event;

/**
 * 信用分变动事件。
 *
 * <p>业务 Service（订单完成 / 举报成立 / 争议判责 / 评价等）在各自事务内发布本事件，
 * 由 {@code CreditChangeListener} 在事务提交后异步落地，解耦业务与信用逻辑。
 * PR1 仅提供事件与监听器骨架；具体发布点在后续 PR 接入。</p>
 *
 * @author thr
 */
public class CreditChangeEvent
{
    private final Long userId;
    private final String changeType;
    private final int changeValue;
    private final String bizType;
    private final Long bizId;
    private final String reason;

    public CreditChangeEvent(Long userId, String changeType, int changeValue,
                             String bizType, Long bizId, String reason)
    {
        this.userId = userId;
        this.changeType = changeType;
        this.changeValue = changeValue;
        this.bizType = bizType;
        this.bizId = bizId;
        this.reason = reason;
    }

    public Long getUserId() { return userId; }
    public String getChangeType() { return changeType; }
    public int getChangeValue() { return changeValue; }
    public String getBizType() { return bizType; }
    public Long getBizId() { return bizId; }
    public String getReason() { return reason; }
}
