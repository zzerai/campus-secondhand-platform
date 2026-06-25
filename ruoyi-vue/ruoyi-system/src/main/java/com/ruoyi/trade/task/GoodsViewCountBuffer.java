package com.ruoyi.trade.task;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;

/**
 * 商品浏览量写缓冲。
 *
 * <p>商品详情是匿名高频读接口，原实现每次浏览都执行一次 {@code view_count = view_count + 1} 写库，
 * 热门商品会形成行级写热点并阻塞详情响应。本组件改为：浏览先在 Redis Hash 中累加增量，
 * 由定时任务按固定间隔批量回刷到数据库（每个商品一次带 delta 的 UPDATE）。</p>
 *
 * <p>代价：商品 {@code view_count}（含列表"热门"排序）会滞后至多一个回刷周期，
 * 与统计看板的秒级容忍一致，业务可接受。</p>
 *
 * @author thr
 */
@Component
public class GoodsViewCountBuffer
{
    private static final Logger log = LoggerFactory.getLogger(GoodsViewCountBuffer.class);

    /** 待回刷增量 Hash 键：field=goodsId，value=待回刷增量 */
    private static final String PENDING_KEY = CacheConstants.TRADE_GOODS_VIEW_KEY + "pending";

    @Autowired
    @SuppressWarnings("rawtypes")
    private RedisTemplate redisTemplate;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    /**
     * 记录一次浏览：在 Redis 中对该商品的待回刷增量 +1。
     * Redis 不可用时仅记日志降级，绝不影响详情读取主流程。
     */
    @SuppressWarnings("unchecked")
    public void increment(Long goodsId)
    {
        if (goodsId == null)
        {
            return;
        }
        try
        {
            redisTemplate.opsForHash().increment(PENDING_KEY, goodsId.toString(), 1L);
        }
        catch (Exception ex)
        {
            log.warn("浏览量累加失败 goodsId={}", goodsId, ex);
        }
    }

    /**
     * 定时把 Redis 中累计的浏览增量回刷到数据库。
     *
     * <p>逐商品回刷成功后，用 HINCRBY 减去本次已回刷的增量（而非整桶清空），
     * 保证回刷期间新到的浏览不丢失；字段归零后删除以控制 Hash 体积。
     * 单个商品回刷失败不影响其它商品，其增量留待下个周期重试。</p>
     */
    @Scheduled(fixedDelay = 60000L, initialDelay = 60000L)
    @SuppressWarnings("unchecked")
    public void flush()
    {
        Map<Object, Object> pending;
        try
        {
            pending = redisTemplate.opsForHash().entries(PENDING_KEY);
        }
        catch (Exception ex)
        {
            log.warn("读取浏览量缓冲失败，跳过本轮回刷", ex);
            return;
        }
        if (pending == null || pending.isEmpty())
        {
            return;
        }

        for (Map.Entry<Object, Object> entry : pending.entrySet())
        {
            String field = String.valueOf(entry.getKey());
            long delta = toLong(entry.getValue());
            try
            {
                if (delta <= 0)
                {
                    redisTemplate.opsForHash().delete(PENDING_KEY, field);
                    continue;
                }
                trTradeGoodsMapper.increaseViewCountBy(Long.parseLong(field), delta);
                Long remaining = redisTemplate.opsForHash().increment(PENDING_KEY, field, -delta);
                if (remaining != null && remaining <= 0)
                {
                    redisTemplate.opsForHash().delete(PENDING_KEY, field);
                }
            }
            catch (Exception ex)
            {
                // 留待下个周期重试，不清增量
                log.warn("浏览量回刷失败 goodsId={}, delta={}", field, delta, ex);
            }
        }
    }

    private long toLong(Object value)
    {
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        try
        {
            return value == null ? 0L : Long.parseLong(value.toString());
        }
        catch (NumberFormatException ex)
        {
            return 0L;
        }
    }
}
