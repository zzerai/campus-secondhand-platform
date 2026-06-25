package com.ruoyi.trade.domain.vo;

import java.util.Date;

/**
 * 信用分变动结果。供管理端调整接口回显，并告知是否触发封禁。
 *
 * @author thr
 */
public class CreditApplyResult
{
    /** 封禁类型常量 */
    public static final String BAN_NONE = "NONE";
    public static final String BAN_TEMP = "TEMP";
    public static final String BAN_PERMANENT = "PERMANENT";

    private Long userId;

    /** 是否真正应用（false=幂等跳过：该业务事件已处理过） */
    private boolean applied;

    private Integer scoreBefore;

    private Integer scoreAfter;

    /** 本次是否触发封禁及类型：NONE / TEMP / PERMANENT */
    private String banType = BAN_NONE;

    /** 临时封禁到期时间（TEMP 时有值；PERMANENT / NONE 为 null） */
    private Date banUntil;

    public static CreditApplyResult skipped(Long userId)
    {
        CreditApplyResult r = new CreditApplyResult();
        r.userId = userId;
        r.applied = false;
        return r;
    }

    public static CreditApplyResult applied(Long userId, int scoreBefore, int scoreAfter)
    {
        CreditApplyResult r = new CreditApplyResult();
        r.userId = userId;
        r.applied = true;
        r.scoreBefore = scoreBefore;
        r.scoreAfter = scoreAfter;
        return r;
    }

    public void markBan(String banType, Date banUntil)
    {
        this.banType = banType;
        this.banUntil = banUntil;
    }

    public Long getUserId() { return userId; }
    public boolean isApplied() { return applied; }
    public Integer getScoreBefore() { return scoreBefore; }
    public Integer getScoreAfter() { return scoreAfter; }
    public String getBanType() { return banType; }
    public Date getBanUntil() { return banUntil; }
}
