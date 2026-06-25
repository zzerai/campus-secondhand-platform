package com.ruoyi.trade.domain.dto;

/**
 * 移动端忘记密码-重置密码入参（无需登录态）。
 *
 * <p>通过 /app/sms/send?scene=reset_password 下发的短信验证码完成身份校验。</p>
 */
public class AppResetPwdDto
{
    /** 已注册的手机号。 */
    private String phone;

    /** 短信验证码（阿里云下发，6 位）。 */
    private String smsCode;

    /** 新密码（明文，Service 层 BCrypt 加密）。 */
    private String newPassword;

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getSmsCode()
    {
        return smsCode;
    }

    public void setSmsCode(String smsCode)
    {
        this.smsCode = smsCode;
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
