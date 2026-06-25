package com.ruoyi.trade.task;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.IAppOrderService;

/**
 * 待支付订单超时自动取消任务（闲鱼模式）。
 *
 * <p>注册为若依 Quartz 任务，在管理端"系统监控 → 定时任务"维护触发频率（调用方法
 * {@code orderPaymentTimeoutTask.cancelTimeoutOrders()}）。每次执行扫描创建时间早于
 * {@code trade.order.pay-timeout-minutes}（默认 30 分钟）且仍处于"待支付"的订单，逐单自动取消
 * 并恢复商品上架。逐单调用 {@link IAppOrderService#cancelTimeoutOrder} 走独立事务，单单失败不影响
 * 其它；状态不符（如刚支付）由行锁内复检静默跳过。</p>
 *
 * @author thr
 */
@Component
public class OrderPaymentTimeoutTask
{
    private static final Logger log = LoggerFactory.getLogger(OrderPaymentTimeoutTask.class);

    @Value("${trade.order.pay-timeout-minutes:30}")
    private int payTimeoutMinutes;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private IAppOrderService appOrderService;

    public void cancelTimeoutOrders()
    {
        try
        {
            Date before = new Date(System.currentTimeMillis() - payTimeoutMinutes * 60_000L);
            List<Long> ids = trTradeOrderMapper.selectPayTimeoutOrderIds(before);
            if (ids == null || ids.isEmpty())
            {
                return;
            }
            int cancelled = 0;
            for (Long orderId : ids)
            {
                try
                {
                    if (appOrderService.cancelTimeoutOrder(orderId))
                    {
                        cancelled++;
                    }
                }
                catch (Exception ex)
                {
                    log.error("待支付超时取消订单 {} 失败", orderId, ex);
                }
            }
            if (cancelled > 0)
            {
                log.info("待支付超时自动取消完成，共取消 {} 单（超时阈值 {} 分钟）", cancelled, payTimeoutMinutes);
            }
        }
        catch (Exception ex)
        {
            log.error("待支付超时自动取消任务执行失败", ex);
        }
    }
}
