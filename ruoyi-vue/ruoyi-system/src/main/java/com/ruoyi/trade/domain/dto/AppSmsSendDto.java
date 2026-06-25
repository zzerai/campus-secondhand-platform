package com.ruoyi.trade.domain.dto;

/**
 * 移动端短信验证码发送入参。
 *
 * @author trading
 */
public class AppSmsSendDto
{
    /** 手机号 */
    private String phone;

    /** 业务场景：register / login */
    private String scene;

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getScene()
    {
        return scene;
    }

    public void setScene(String scene)
    {
        this.scene = scene;
    }
}
