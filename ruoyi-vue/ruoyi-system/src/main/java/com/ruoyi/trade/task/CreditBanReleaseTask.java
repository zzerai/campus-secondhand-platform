package com.ruoyi.trade.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.trade.service.ICreditScoreService;

/**
 * 信用分临时封禁自动解禁任务。
 *
 * <p>每 5 分钟扫描到期的临时封禁，置回正常并把信用分重置到起点（见 {@code ICreditScoreService.releaseExpiredTempBans}）。
 * 永久封禁（status='2'）不在范围内；登录时另有惰性兜底覆盖扫描间隔空窗。</p>
 *
 * @author thr
 */
@Component
public class CreditBanReleaseTask
{
    private static final Logger log = LoggerFactory.getLogger(CreditBanReleaseTask.class);

    @Autowired
    private ICreditScoreService creditScoreService;

    @Scheduled(fixedDelay = 300000L, initialDelay = 300000L)
    public void releaseExpiredBans()
    {
        try
        {
            creditScoreService.releaseExpiredTempBans();
        }
        catch (Exception ex)
        {
            log.error("信用分临时封禁自动解禁任务执行失败", ex);
        }
    }
}
