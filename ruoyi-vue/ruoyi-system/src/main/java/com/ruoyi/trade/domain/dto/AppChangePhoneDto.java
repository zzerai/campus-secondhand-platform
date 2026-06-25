package com.ruoyi.trade.domain.dto;

/**
 * 移动端换绑手机号入参（需登录态）。
 *
 * <p>已登录即视为已证明账户所有权，故仅需新手机号 + 新手机号收到的验证码。</p>
 */
public class AppChangePhoneDto
{
    /** 新手机号。 */
    private String newPhone;

    /** 新手机号收到的短信验证码（scene=change_phone）。 */
    private String smsCode;

    public String getNewPhone()
    {
        return newPhone;
    }

    public void setNewPhone(String newPhone)
    {
        this.newPhone = newPhone;
    }

    public String getSmsCode()
    {
        return smsCode;
    }

    public void setSmsCode(String smsCode)
    {
        this.smsCode = smsCode;
    }
}
