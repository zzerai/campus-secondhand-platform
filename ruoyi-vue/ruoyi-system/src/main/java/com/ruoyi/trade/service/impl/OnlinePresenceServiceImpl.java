package com.ruoyi.trade.service.impl;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.trade.service.IOnlinePresenceService;

/**
 * 移动端在线状态服务实现（Redis 心跳 + TTL）。
 *
 * @author ruoyi
 */
@Service
public class OnlinePresenceServiceImpl implements IOnlinePresenceService
{
    /**
     * 心跳有效期（秒）。客户端 5s 拉消息、10s 拉未读，60s 内无任何 /app 请求才判离线，
     * 给网络抖动和切页留足容差。
     */
    private static final int HEARTBEAT_TTL_SECONDS = 60;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void touch(Long userId)
    {
        if (userId == null)
        {
            return;
        }
        redisCache.setCacheObject(key(userId), "1", HEARTBEAT_TTL_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public boolean isOnline(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        return Boolean.TRUE.equals(redisCache.hasKey(key(userId)));
    }

    private String key(Long userId)
    {
        return CacheConstants.APP_ONLINE_KEY + userId;
    }
}
