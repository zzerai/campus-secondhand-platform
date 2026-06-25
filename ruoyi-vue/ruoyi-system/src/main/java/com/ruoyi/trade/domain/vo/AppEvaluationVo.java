package com.ruoyi.trade.domain.vo;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 移动端评价列表VO（含用户昵称头像、订单商品信息）
 */
public class AppEvaluationVo
{
    private Long evaluationId;
    private Long orderId;
    private Long fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Long toUserId;
    private String toUserName;
    private String toUserAvatar;
    private Long score;
    private String content;
    private String goodsTitle;
    private String goodsImages;
    private BigDecimal goodsPrice;
    private Date createTime;

    public Long getEvaluationId() { return evaluationId; }
    public void setEvaluationId(Long evaluationId) { this.evaluationId = evaluationId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getFromUserAvatar() { return fromUserAvatar; }
    public void setFromUserAvatar(String fromUserAvatar) { this.fromUserAvatar = fromUserAvatar; }

    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }

    public String getToUserName() { return toUserName; }
    public void setToUserName(String toUserName) { this.toUserName = toUserName; }

    public String getToUserAvatar() { return toUserAvatar; }
    public void setToUserAvatar(String toUserAvatar) { this.toUserAvatar = toUserAvatar; }

    public Long getScore() { return score; }
    public void setScore(Long score) { this.score = score; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getGoodsTitle() { return goodsTitle; }
    public void setGoodsTitle(String goodsTitle) { this.goodsTitle = goodsTitle; }

    public String getGoodsImages() { return goodsImages; }
    public void setGoodsImages(String goodsImages) { this.goodsImages = goodsImages; }

    public BigDecimal getGoodsPrice() { return goodsPrice; }
    public void setGoodsPrice(BigDecimal goodsPrice) { this.goodsPrice = goodsPrice; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
