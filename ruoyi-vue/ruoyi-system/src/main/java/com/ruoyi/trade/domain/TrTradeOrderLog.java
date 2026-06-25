package com.ruoyi.trade.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订单操作日志对象 tr_trade_order_log
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeOrderLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 操作人ID */
    @Excel(name = "操作人ID")
    private Long operatorId;

    /** 操作人类型：1买家，2卖家，3管理员 */
    @Excel(name = "操作人类型：1买家，2卖家，3管理员")
    private String operatorType;

    /** 操作前订单状态 */
    @Excel(name = "操作前订单状态")
    private String beforeStatus;

    /** 操作后订单状态 */
    @Excel(name = "操作后订单状态")
    private String afterStatus;

    /** 操作类型：create/confirm/cancel/pay/complete/dispute/handle */
    @Excel(name = "操作类型：create/confirm/cancel/pay/complete/dispute/handle")
    private String operationType;

    /** 操作说明 */
    @Excel(name = "操作说明")
    private String operationContent;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setOperatorId(Long operatorId) 
    {
        this.operatorId = operatorId;
    }

    public Long getOperatorId() 
    {
        return operatorId;
    }

    public void setOperatorType(String operatorType) 
    {
        this.operatorType = operatorType;
    }

    public String getOperatorType() 
    {
        return operatorType;
    }

    public void setBeforeStatus(String beforeStatus) 
    {
        this.beforeStatus = beforeStatus;
    }

    public String getBeforeStatus() 
    {
        return beforeStatus;
    }

    public void setAfterStatus(String afterStatus) 
    {
        this.afterStatus = afterStatus;
    }

    public String getAfterStatus() 
    {
        return afterStatus;
    }

    public void setOperationType(String operationType) 
    {
        this.operationType = operationType;
    }

    public String getOperationType() 
    {
        return operationType;
    }

    public void setOperationContent(String operationContent) 
    {
        this.operationContent = operationContent;
    }

    public String getOperationContent() 
    {
        return operationContent;
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
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("orderId", getOrderId())
            .append("operatorId", getOperatorId())
            .append("operatorType", getOperatorType())
            .append("beforeStatus", getBeforeStatus())
            .append("afterStatus", getAfterStatus())
            .append("operationType", getOperationType())
            .append("operationContent", getOperationContent())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
