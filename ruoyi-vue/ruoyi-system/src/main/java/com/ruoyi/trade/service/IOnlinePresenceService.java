package com.ruoyi.trade.service;

/**
 * 移动端学生用户在线状态服务。
 *
 * <p>无长连接，采用"最近活跃 + TTL"语义：每次带登录态的 {@code /app/**} 请求刷新一个 Redis 心跳键，
 * 键存在即视为在线，到期自动判离线。轮询架构下客户端本就每 5~10s 请求一次，等价自带心跳。</p>
 */
public interface IOnlinePresenceService
{
    /**
     * 刷新指定用户的在线心跳（重置 TTL）。
     *
     * @param userId 学生用户ID；为 null 时忽略
     */
    void touch(Long userId);

    /**
     * 判断指定用户当前是否在线。
     *
     * @param userId 学生用户ID
     * @return 心跳键存在返回 true
     */
    boolean isOnline(Long userId);
}
