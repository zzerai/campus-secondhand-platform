package com.ruoyi.trade.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrCreditLog;

/**
 * 学生信用分变动流水 Mapper 接口
 *
 * @author thr
 */
public interface TrCreditLogMapper
{
    /**
     * 新增信用流水（uk_event 唯一键保障业务事件幂等）
     */
    public int insertTrCreditLog(TrCreditLog trCreditLog);

    /**
     * 查询信用流水列表（管理端，按 userId / changeType 过滤）
     */
    public List<TrCreditLog> selectTrCreditLogList(TrCreditLog trCreditLog);

    /**
     * 统计某用户的某业务事件是否已落库（幂等前置检查，bizId 非空时使用）。
     * 含 userId：同一业务事件可影响多个用户（如订单完成买卖双方各加分），按"用户+事件"去重。
     */
    public int countByEvent(@Param("userId") Long userId,
                            @Param("bizType") String bizType,
                            @Param("bizId") Long bizId,
                            @Param("changeType") String changeType);

    /**
     * 统计用户历史自动封禁次数（auto_ban 行数），决定下一次封禁等级
     */
    public int countAutoBanByUser(@Param("userId") Long userId);

    /**
     * 查询临时封禁已到期、应自动解禁的用户ID。
     *
     * <p>判定：status='1' 且其最近一次封禁生命周期事件（auto_ban / ban_release）是
     * auto_ban、ban_until 非空且已 &lt;= now —— 即处于"打开且过期"的临时封禁，
     * 避免误伤管理员手动禁用或永久封禁（status='2'）。</p>
     */
    public List<Long> selectUserIdsToRelease(@Param("now") Date now);
}
