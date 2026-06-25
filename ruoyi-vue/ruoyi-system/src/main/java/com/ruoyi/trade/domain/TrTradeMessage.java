package com.ruoyi.trade.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 私信消息对象 tr_trade_message
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public class TrTradeMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 关联商品ID */
    @Excel(name = "关联商品ID")
    private Long goodsId;

    /** 关联订单ID */
    @Excel(name = "关联订单ID")
    private Long orderId;

    /** 发送人ID */
    @Excel(name = "发送人ID")
    private Long senderId;

    /** 接收人ID */
    @Excel(name = "接收人ID")
    private Long receiverId;

    /** 消息内容 */
    @Excel(name = "消息内容")
    private String content;

    /** 阅读状态：0未读，1已读 */
    @Excel(name = "阅读状态：0未读，1已读")
    private String readStatus;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setMessageId(Long messageId) 
    {
        this.messageId = messageId;
    }

    public Long getMessageId() 
    {
        return messageId;
    }

    public void setGoodsId(Long goodsId) 
    {
        this.goodsId = goodsId;
    }

    public Long getGoodsId() 
    {
        return goodsId;
    }

    public void setOrderId(Long orderId) 
    {
        this.orderId = orderId;
    }

    public Long getOrderId() 
    {
        return orderId;
    }

    public void setSenderId(Long senderId) 
    {
        this.senderId = senderId;
    }

    public Long getSenderId() 
    {
        return senderId;
    }

    public void setReceiverId(Long receiverId) 
    {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() 
    {
        return receiverId;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setReadStatus(String readStatus) 
    {
        this.readStatus = readStatus;
    }

    public String getReadStatus() 
    {
        return readStatus;
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
            .append("messageId", getMessageId())
            .append("goodsId", getGoodsId())
            .append("orderId", getOrderId())
            .append("senderId", getSenderId())
            .append("receiverId", getReceiverId())
            .append("content", getContent())
            .append("readStatus", getReadStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
