package com.ruoyi.trade.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 移动端用户公开主页VO（脱敏：仅含可对外展示字段，不含手机号/学号/密码/账号状态）。
 */
public class AppUserHomepageVo
{
    /** 用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 信用分 */
    private Long creditScore;

    /** 加入时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date createTime;

    /** 在售商品数 */
    private Long onSaleCount;

    /** 已售商品数 */
    private Long soldCount;

    /** 收到的评价均分 */
    private Double averageScore;

    /** 好评率（百分比，0-100；无评价时为 0） */
    private Integer goodRate;

    /** 收到的评价总数 */
    private Integer totalReceived;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Long getCreditScore() { return creditScore; }
    public void setCreditScore(Long creditScore) { this.creditScore = creditScore; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Long getOnSaleCount() { return onSaleCount; }
    public void setOnSaleCount(Long onSaleCount) { this.onSaleCount = onSaleCount; }

    public Long getSoldCount() { return soldCount; }
    public void setSoldCount(Long soldCount) { this.soldCount = soldCount; }

    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }

    public Integer getGoodRate() { return goodRate; }
    public void setGoodRate(Integer goodRate) { this.goodRate = goodRate; }

    public Integer getTotalReceived() { return totalReceived; }
    public void setTotalReceived(Integer totalReceived) { this.totalReceived = totalReceived; }
}
