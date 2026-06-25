package com.ruoyi.trade.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 移动端私信会话列表项。
 *
 * <p>会话粒度为 (我, 对方, 商品)：同一对买卖双方在不同商品上的对话是不同会话。
 * 列表按 {@link #lastTime} 倒序，配合未读数供首页/消息中心展示。</p>
 */
public class AppConversationVo
{
    /** 对方学生用户ID。 */
    private Long peerId;

    /** 对方昵称（直接取 tr_student_user.nickname；可能为 null）。 */
    private String peerNickname;

    /** 对方头像（直接取 tr_student_user.avatar；可能为 null，由前端兜底）。 */
    private String peerAvatar;

    /** 关联商品ID。 */
    private Long goodsId;

    /** 商品标题。 */
    private String goodsTitle;

    /** 商品主图（取 tr_trade_goods_image 中按 sort/image_id 升序的第一张）。 */
    private String goodsCoverImage;

    /** 商品状态：0待审核 1已上架 2审核拒绝 3已下架 4已售出。前端可据此提示"已下架"等。 */
    private String goodsStatus;

    /** 最后一条消息内容（前端可截断显示）。 */
    private String lastContent;

    /** 最后一条消息的发送者ID。前端比对当前用户ID决定显示"我："还是"对方："。 */
    private Long lastSenderId;

    /** 最后一条消息时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastTime;

    /** 我在该会话的未读消息数（仅算 receiver=me 且 read_status='0' 的）。 */
    private Integer unreadCount;

    /** 关联订单ID（取最新一条关联订单，可能为 null）。 */
    private Long orderId;

    public Long getPeerId()
    {
        return peerId;
    }

    public void setPeerId(Long peerId)
    {
        this.peerId = peerId;
    }

    public String getPeerNickname()
    {
        return peerNickname;
    }

    public void setPeerNickname(String peerNickname)
    {
        this.peerNickname = peerNickname;
    }

    public String getPeerAvatar()
    {
        return peerAvatar;
    }

    public void setPeerAvatar(String peerAvatar)
    {
        this.peerAvatar = peerAvatar;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public String getGoodsTitle()
    {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle)
    {
        this.goodsTitle = goodsTitle;
    }

    public String getGoodsCoverImage()
    {
        return goodsCoverImage;
    }

    public void setGoodsCoverImage(String goodsCoverImage)
    {
        this.goodsCoverImage = goodsCoverImage;
    }

    public String getGoodsStatus()
    {
        return goodsStatus;
    }

    public void setGoodsStatus(String goodsStatus)
    {
        this.goodsStatus = goodsStatus;
    }

    public String getLastContent()
    {
        return lastContent;
    }

    public void setLastContent(String lastContent)
    {
        this.lastContent = lastContent;
    }

    public Long getLastSenderId()
    {
        return lastSenderId;
    }

    public void setLastSenderId(Long lastSenderId)
    {
        this.lastSenderId = lastSenderId;
    }

    public Date getLastTime()
    {
        return lastTime;
    }

    public void setLastTime(Date lastTime)
    {
        this.lastTime = lastTime;
    }

    public Integer getUnreadCount()
    {
        return unreadCount;
    }

    public void setUnreadCount(Integer unreadCount)
    {
        this.unreadCount = unreadCount;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }
}
