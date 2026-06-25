package com.ruoyi.trade.domain.dto;

/**
 * 移动端登录态改密码入参。
 *
 * <p>需提供旧密码用于身份二次确认，新密码不能与旧密码相同。</p>
 */
public class AppChangePwdDto
{
    /** 旧密码（明文）。 */
    private String oldPassword;

    /** 新密码（明文，Service 层 BCrypt 加密）。 */
    private String newPassword;

    public String getOldPassword()
    {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword)
    {
        this.oldPassword = oldPassword;
    }

    public String getNewPassword()
    {
        return newPassword;
    }

    public void setNewPassword(String newPassword)
    {
        this.newPassword = newPassword;
    }
}
