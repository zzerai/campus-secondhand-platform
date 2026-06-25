package com.ruoyi.trade.service.impl;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.trade.domain.TrCreditLog;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.vo.CreditApplyResult;
import com.ruoyi.trade.mapper.TrCreditLogMapper;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.service.ICreditScoreService;
import com.ruoyi.trade.service.IOnlineUserKicker;
import com.ruoyi.trade.utils.CreditConstants;

/**
 * 学生信用分 Service 实现。
 *
 * <p>核心机制见 {@link #applyChange}：行锁串行化 → 落流水（按业务事件幂等）→ 物化当前分 →
 * 检测"下穿阈值"触发封禁升级（7天 / 30天 / 永久）→ 踢下线。
 * 临时封禁到期由 {@link #releaseExpiredTempBans} 还原并重置分值。</p>
 *
 * @author thr
 */
@Service
public class CreditScoreServiceImpl implements ICreditScoreService
{
    private static final Logger log = LoggerFactory.getLogger(CreditScoreServiceImpl.class);

    @Autowired
    private TrCreditLogMapper trCreditLogMapper;

    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Autowired
    private IOnlineUserKicker onlineUserKicker;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreditApplyResult applyChange(Long userId, String changeType, int changeValue,
                                         String bizType, Long bizId, String reason)
    {
        if (userId == null)
        {
            throw new ServiceException("信用分变动失败：用户ID不能为空");
        }
        // 幂等：业务事件（bizId 非空）若该用户已处理过直接跳过；管理员调整（bizId 为空）允许重复
        if (bizId != null && trCreditLogMapper.countByEvent(userId, bizType, bizId, changeType) > 0)
        {
            return CreditApplyResult.skipped(userId);
        }

        TrStudentUser locked = trStudentUserMapper.selectStudentForUpdate(userId);
        if (locked == null)
        {
            throw new ServiceException("信用分变动失败：用户不存在或已删除");
        }
        int before = locked.getCreditScore() == null
                ? CreditConstants.INITIAL_SCORE : locked.getCreditScore().intValue();
        int after = Math.max(CreditConstants.MIN_SCORE, before + changeValue);

        insertLog(userId, changeType, changeValue, before, after, bizType, bizId, null, reason);
        trStudentUserMapper.updateCreditScore(userId, after);

        CreditApplyResult result = CreditApplyResult.applied(userId, before, after);

        int threshold = configInt(CreditConstants.CFG_THRESHOLD, CreditConstants.DEFAULT_THRESHOLD);
        // 下穿：本次扣分使分值从“合格”跌破阈值，才触发封禁；已在阈值下的继续扣分不重复封禁
        if (before >= threshold && after < threshold)
        {
            applyBanEscalation(userId, after, result);
        }
        return result;
    }

    /** 按历史封禁次数升级：第1次 7天、第2次 30天、第3次及以后永久。 */
    private void applyBanEscalation(Long userId, int currentScore, CreditApplyResult result)
    {
        int tier = trCreditLogMapper.countAutoBanByUser(userId) + 1;
        String status;
        Date banUntil;
        String reason;
        if (tier == 1)
        {
            int days = configInt(CreditConstants.CFG_FIRST_DAYS, CreditConstants.DEFAULT_FIRST_DAYS);
            status = CreditConstants.STATUS_TEMP_BAN;
            banUntil = DateUtils.addDays(DateUtils.getNowDate(), days);
            reason = "信用分跌破阈值，首次临时封禁 " + days + " 天";
            result.markBan(CreditApplyResult.BAN_TEMP, banUntil);
        }
        else if (tier == 2)
        {
            int days = configInt(CreditConstants.CFG_SECOND_DAYS, CreditConstants.DEFAULT_SECOND_DAYS);
            status = CreditConstants.STATUS_TEMP_BAN;
            banUntil = DateUtils.addDays(DateUtils.getNowDate(), days);
            reason = "信用分再次跌破阈值，第二次临时封禁 " + days + " 天";
            result.markBan(CreditApplyResult.BAN_TEMP, banUntil);
        }
        else
        {
            status = CreditConstants.STATUS_PERMANENT_BAN;
            banUntil = null;
            reason = "信用分第三次跌破阈值，永久封禁";
            result.markBan(CreditApplyResult.BAN_PERMANENT, null);
        }
        trStudentUserMapper.updateStatus(userId, status);
        insertLog(userId, CreditConstants.TYPE_AUTO_BAN, 0, currentScore, currentScore,
                CreditConstants.BIZ_SYSTEM, null, banUntil, reason);
        onlineUserKicker.kickByUserId(userId);
        log.info("信用分封禁: userId={}, tier={}, status={}, banUntil={}", userId, tier, status, banUntil);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseExpiredTempBans()
    {
        List<Long> userIds = trCreditLogMapper.selectUserIdsToRelease(DateUtils.getNowDate());
        if (userIds == null || userIds.isEmpty())
        {
            return 0;
        }
        int released = 0;
        for (Long userId : userIds)
        {
            TrStudentUser locked = trStudentUserMapper.selectStudentForUpdate(userId);
            // 二次确认仍是临时封禁，避免与并发的管理员改状态竞态
            if (locked == null || !CreditConstants.STATUS_TEMP_BAN.equals(locked.getStatus()))
            {
                continue;
            }
            int before = locked.getCreditScore() == null ? 0 : locked.getCreditScore().intValue();
            trStudentUserMapper.updateCreditScore(userId, CreditConstants.RESET_SCORE);
            trStudentUserMapper.updateStatus(userId, CreditConstants.STATUS_NORMAL);
            insertLog(userId, CreditConstants.TYPE_BAN_RELEASE, CreditConstants.RESET_SCORE - before,
                    before, CreditConstants.RESET_SCORE, CreditConstants.BIZ_SYSTEM, null, null,
                    "临时封禁到期，自动解禁并重置信用分至 " + CreditConstants.RESET_SCORE);
            released++;
        }
        if (released > 0)
        {
            log.info("信用分临时封禁自动解禁完成，本次解禁 {} 人", released);
        }
        return released;
    }

    @Override
    public List<TrCreditLog> selectCreditLogList(TrCreditLog query)
    {
        return trCreditLogMapper.selectTrCreditLogList(query);
    }

    private void insertLog(Long userId, String changeType, int changeValue, int before, int after,
                           String bizType, Long bizId, Date banUntil, String reason)
    {
        TrCreditLog entry = new TrCreditLog();
        entry.setUserId(userId);
        entry.setChangeType(changeType);
        entry.setChangeValue(changeValue);
        entry.setScoreBefore(before);
        entry.setScoreAfter(after);
        entry.setBizType(bizType);
        entry.setBizId(bizId);
        entry.setBanUntil(banUntil);
        entry.setReason(reason);
        entry.setDelFlag("0");
        entry.setCreateBy("system");
        // createTime / updateTime 由 AuditTimeFillInterceptor 兜底
        trCreditLogMapper.insertTrCreditLog(entry);
    }

    private int configInt(String key, int defaultValue)
    {
        try
        {
            String value = sysConfigService.selectConfigByKey(key);
            if (StringUtils.isNotEmpty(value))
            {
                return Integer.parseInt(value.trim());
            }
        }
        catch (Exception ex)
        {
            log.warn("读取信用分配置 {} 失败，使用默认值 {}", key, defaultValue, ex);
        }
        return defaultValue;
    }
}
