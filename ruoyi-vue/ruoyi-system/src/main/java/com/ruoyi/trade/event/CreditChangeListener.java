package com.ruoyi.trade.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import com.ruoyi.trade.service.ICreditScoreService;

/**
 * 信用分变动事件监听器。
 *
 * <p>{@code AFTER_COMMIT} 确保业务事务已提交后再落信用分，规避可见性竞态；
 * {@code fallbackExecution=true} 使无事务上下文（如单测直接发事件）时也能执行。
 * 信用分落地失败仅记日志，不回滚已提交的业务操作。</p>
 *
 * @author thr
 */
@Component
public class CreditChangeListener
{
    private static final Logger log = LoggerFactory.getLogger(CreditChangeListener.class);

    @Autowired
    private ICreditScoreService creditScoreService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCreditChange(CreditChangeEvent event)
    {
        try
        {
            creditScoreService.applyChange(event.getUserId(), event.getChangeType(), event.getChangeValue(),
                    event.getBizType(), event.getBizId(), event.getReason());
        }
        catch (Exception ex)
        {
            log.error("信用分变动落地失败: userId={}, type={}, bizId={}",
                    event.getUserId(), event.getChangeType(), event.getBizId(), ex);
        }
    }
}
