package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrCreditLog;
import com.ruoyi.trade.domain.vo.CreditApplyResult;

/**
 * 学生信用分 Service 接口。
 *
 * @author thr
 */
public interface ICreditScoreService
{
    /**
     * 应用一次信用分变动（加减分 + 下穿阈值自动封禁升级），全程一个事务、行锁串行化、按业务事件幂等。
     *
     * @param userId      目标用户
     * @param changeType  变动类型（见 {@code CreditConstants.TYPE_*}）
     * @param changeValue 增减分值（可负）
     * @param bizType     关联业务类型（见 {@code CreditConstants.BIZ_*}）
     * @param bizId       关联业务主键；非空时参与幂等去重，为空（如管理员调整）则允许重复
     * @param reason      原因 / 备注
     * @return 变动结果（含前后分值、是否触发封禁）
     */
    CreditApplyResult applyChange(Long userId, String changeType, int changeValue,
                                  String bizType, Long bizId, String reason);

    /**
     * 解除所有已到期的临时封禁：状态置正常、信用分重置、写解禁流水。
     * 供定时任务调用。
     *
     * @return 本次解禁的用户数
     */
    int releaseExpiredTempBans();

    /**
     * 查询信用流水列表（管理端）。
     *
     * @param query 过滤条件（userId / changeType / bizType）
     * @return 流水列表
     */
    List<TrCreditLog> selectCreditLogList(TrCreditLog query);
}
