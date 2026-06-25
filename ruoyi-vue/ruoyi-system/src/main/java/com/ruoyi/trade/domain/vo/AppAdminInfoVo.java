package com.ruoyi.trade.domain.vo;

/**
 * 管理员信息 VO（移动端"联系管理员"功能用）。
 */
public class AppAdminInfoVo
{
    private Long userId;
    private String nickname;
    private String avatar;

    public Long getUserId() { return userId; }

    public void setUserId(Long userId) { this.userId = userId; }

    public String getNickname() { return nickname; }

    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }

    public void setAvatar(String avatar) { this.avatar = avatar; }
}
