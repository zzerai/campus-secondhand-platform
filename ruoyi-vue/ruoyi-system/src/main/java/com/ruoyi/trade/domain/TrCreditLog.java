package com.ruoyi.trade.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生信用分变动流水对象 tr_credit_log
 *
 * <p>追加写、只增不改，是信用分的唯一事实源；{@code tr_student_user.credit_score} 为其物化当前值。
 * 封禁次数（升级依据）由本表 change_type='auto_ban' 行数推导，封禁到期时间存于 auto_ban 行的 banUntil。</p>
 *
 * @author thr
 */
public class TrCreditLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long logId;

    /** 学生用户ID */
    @Excel(name = "学生用户ID")
    private Long userId;

    /** 变动类型 */
    @Excel(name = "变动类型")
    private String changeType;

    /** 本次增减（可负） */
    @Excel(name = "增减分值")
    private Integer changeValue;

    /** 变动前分值 */
    @Excel(name = "变动前")
    private Integer scoreBefore;

    /** 变动后分值 */
    @Excel(name = "变动后")
    private Integer scoreAfter;

    /** 关联业务类型 */
    private String bizType;

    /** 关联业务主键 */
    private Long bizId;

    /** 封禁到期时间（仅 auto_ban 行有值；永久封禁为 null） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "封禁到期", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date banUntil;

    /** 原因 / 管理员备注 */
    @Excel(name = "原因")
    private String reason;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setLogId(Long logId) { this.logId = logId; }
    public Long getLogId() { return logId; }

    public void setUserId(Long userId) { this.userId = userId; }
    public Long getUserId() { return userId; }

    public void setChangeType(String changeType) { this.changeType = changeType; }
    public String getChangeType() { return changeType; }

    public void setChangeValue(Integer changeValue) { this.changeValue = changeValue; }
    public Integer getChangeValue() { return changeValue; }

    public void setScoreBefore(Integer scoreBefore) { this.scoreBefore = scoreBefore; }
    public Integer getScoreBefore() { return scoreBefore; }

    public void setScoreAfter(Integer scoreAfter) { this.scoreAfter = scoreAfter; }
    public Integer getScoreAfter() { return scoreAfter; }

    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizType() { return bizType; }

    public void setBizId(Long bizId) { this.bizId = bizId; }
    public Long getBizId() { return bizId; }

    public void setBanUntil(Date banUntil) { this.banUntil = banUntil; }
    public Date getBanUntil() { return banUntil; }

    public void setReason(String reason) { this.reason = reason; }
    public String getReason() { return reason; }

    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
    public String getDelFlag() { return delFlag; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("userId", getUserId())
            .append("changeType", getChangeType())
            .append("changeValue", getChangeValue())
            .append("scoreBefore", getScoreBefore())
            .append("scoreAfter", getScoreAfter())
            .append("bizType", getBizType())
            .append("bizId", getBizId())
            .append("banUntil", getBanUntil())
            .append("reason", getReason())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
