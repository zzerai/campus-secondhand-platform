package com.ruoyi.trade.domain.dto;

/**
 * 移动端学生注册入参。
 *
 * <p>仅接收注册所需字段，避免客户端注入 creditScore / status / delFlag 等敏感字段。</p>
 */
public class AppRegisterDto
{
    /** 学号。 */
    private String studentNo;

    /** 手机号。 */
    private String phone;

    /** 登录密码（明文，由 Service 层加密入库）。 */
    private String password;

    /** 昵称（可选）。 */
    private String nickname;

    /** 短信验证码（必填，由 /app/sms/send?scene=register 下发）。 */
    private String smsCode;

    public String getStudentNo()
    {
        return studentNo;
    }

    public void setStudentNo(String studentNo)
    {
        this.studentNo = studentNo;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
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
