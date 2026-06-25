package com.ruoyi.trade.service;

/**
 * 在线用户强制下线抽象。
 *
 * <p>实现位于 ruoyi-framework（依赖 TokenService / Redis），由 ruoyi-system 业务层（如信用分封禁）
 * 通过本接口反向调用，避免 system → framework 的非法依赖。</p>
 *
 * @author thr
 */
public interface IOnlineUserKicker
{
    /**
     * 踢掉指定用户的全部在线令牌（封禁后立即生效，不等令牌自然过期）。
     *
     * @param userId 用户ID
     * @return 清除的令牌数
     */
    int kickByUserId(Long userId);
}
