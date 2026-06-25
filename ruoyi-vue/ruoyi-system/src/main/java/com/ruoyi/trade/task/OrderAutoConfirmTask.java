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
 * 待收货订单超时自动确认收货任务（闲鱼模式）。
 *
 * <p>注册为若依 Quartz 任务，在管理端"系统监控 → 定时任务"维护触发频率（调用方法
 * {@code orderAutoConfirmTask.autoConfirmOrders()}）。每次执行扫描支付成功时间早于
 * {@code trade.order.auto-confirm-days}（默认 7 天）且仍处于"待收货"、无退款的订单，逐单自动确认完成
 * （等价于买家确认收货，买卖双方各 +1 信用分）。逐单调用 {@link IAppOrderService#autoConfirmOrder}
 * 走独立事务，单单失败不影响其它。</p>
 *
 * @author thr
 */
@Component
public class OrderAutoConfirmTask
{
    private static final Logger log = LoggerFactory.getLogger(OrderAutoConfirmTask.class);

    @Value("${trade.order.auto-confirm-days:7}")
    private int autoConfirmDays;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private IAppOrderService appOrderService;

    public void autoConfirmOrders()
    {
        try
        {
            Date before = new Date(System.currentTimeMillis() - autoConfirmDays * 86_400_000L);
            List<Long> ids = trTradeOrderMapper.selectAutoConfirmOrderIds(before);
            if (ids == null || ids.isEmpty())
            {
                return;
            }
            int confirmed = 0;
            for (Long orderId : ids)
            {
                try
                {
                    if (appOrderService.autoConfirmOrder(orderId))
                    {
                        confirmed++;
                    }
                }
                catch (Exception ex)
                {
                    log.error("待收货超时自动确认订单 {} 失败", orderId, ex);
                }
            }
            if (confirmed > 0)
            {
                log.info("待收货超时自动确认完成，共确认 {} 单（超时阈值 {} 天）", confirmed, autoConfirmDays);
            }
        }
        catch (Exception ex)
        {
            log.error("待收货超时自动确认任务执行失败", ex);
        }
    }
}
