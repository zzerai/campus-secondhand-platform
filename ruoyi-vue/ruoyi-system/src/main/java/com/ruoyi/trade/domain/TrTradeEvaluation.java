package com.ruoyi.trade.domain;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import java.util.Date;

/**
 * 交易评价对象 tr_trade_evaluation
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeEvaluation extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    private Long evaluationId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 评价人ID */
    @Excel(name = "评价人ID")
    private Long fromUserId;

    /** 被评价人ID */
    @Excel(name = "被评价人ID")
    private Long toUserId;

    /** 评分：1-5 */
    @Excel(name = "评分：1-5")
    private Long score;

    /** 评价内容 */
    @Excel(name = "评价内容")
    private String content;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setEvaluationId(Long evaluationId) 
    {
        this.evaluationId = evaluationId;
    }

    public Long getEvaluationId() 
    {
        return evaluationId;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setFromUserId(Long fromUserId) 
    {
        this.fromUserId = fromUserId;
    }

    public Long getFromUserId() 
    {
        return fromUserId;
    }

    public void setToUserId(Long toUserId) 
    {
        this.toUserId = toUserId;
    }

    public Long getToUserId() 
    {
        return toUserId;
    }

    public void setScore(Long score) 
    {
        this.score = score;
    }

    public Long getScore() 
    {
        return score;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    @Override
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @JSONField(format = "yyyy-MM-dd HH:mm")
    public Date getCreateTime()
    {
        return super.getCreateTime();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("evaluationId", getEvaluationId())
            .append("orderId", getOrderId())
            .append("fromUserId", getFromUserId())
            .append("toUserId", getToUserId())
            .append("score", getScore())
            .append("content", getContent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
