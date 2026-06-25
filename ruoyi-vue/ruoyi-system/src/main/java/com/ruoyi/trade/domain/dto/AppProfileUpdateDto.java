package com.ruoyi.trade.domain.dto;

/**
 * 移动端学生个人信息修改入参。
 *
 * <p>仅允许修改头像、昵称、联系方式，其余字段（信用分、账号状态等）不可由客户端变更。</p>
 */
public class AppProfileUpdateDto
{
    /** 头像 URL。 */
    private String avatar;

    /** 昵称。 */
    private String nickname;

    /** 联系方式。 */
    private String contactWay;

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getContactWay()
    {
        return contactWay;
    }

    public void setContactWay(String contactWay)
    {
        this.contactWay = contactWay;
    }
}
