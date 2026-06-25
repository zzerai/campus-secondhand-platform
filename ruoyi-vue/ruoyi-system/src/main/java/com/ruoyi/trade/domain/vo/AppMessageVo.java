package com.ruoyi.trade.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 移动端私信消息详情项。
 *
 * <p>会话详情页消息流的单条记录。{@link #mine} 由服务端按当前登录用户计算，
 * 前端无需再比对 senderId 即可决定气泡方位。</p>
 */
public class AppMessageVo
{
    /** 消息ID。 */
    private Long messageId;

    /** 发送人ID。 */
    private Long senderId;

    /** 接收人ID。 */
    private Long receiverId;

    /** 消息内容。 */
    private String content;

    /** 阅读状态：0未读，1已读。供发送方查看对方是否读过。 */
    private String readStatus;

    /** 发送时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 是否当前登录用户发送（true=我发的，false=对方发的）。 */
    private Boolean mine;

    /** 对方是否在线（基于 Redis 心跳，最近活跃判定）。会话内每条记录值相同，前端取任意一条即可。 */
    private Boolean peerOnline;

    public Long getMessageId()
    {
        return messageId;
    }

    public void setMessageId(Long messageId)
    {
        this.messageId = messageId;
    }

    public Long getSenderId()
    {
        return senderId;
    }

    public void setSenderId(Long senderId)
    {
        this.senderId = senderId;
    }

    public Long getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(Long receiverId)
    {
        this.receiverId = receiverId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getReadStatus()
    {
        return readStatus;
    }

    public void setReadStatus(String readStatus)
    {
        this.readStatus = readStatus;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Boolean getMine()
    {
        return mine;
    }

    public void setMine(Boolean mine)
    {
        this.mine = mine;
    }

    public Boolean getPeerOnline()
    {
        return peerOnline;
    }

    public void setPeerOnline(Boolean peerOnline)
    {
        this.peerOnline = peerOnline;
    }
}
