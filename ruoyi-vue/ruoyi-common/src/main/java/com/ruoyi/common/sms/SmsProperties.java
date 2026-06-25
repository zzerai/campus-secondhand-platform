package com.ruoyi.common.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 阿里云号码认证服务（dypnsapi）短信验证码配置，前缀 {@code ruoyi.sms}。
 *
 * <pre>
 * ruoyi:
 *   sms:
 *     endpoint: dypnsapi.aliyuncs.com
 *     access-key-id: ${ALIYUN_SMS_ACCESS_KEY_ID}
 *     access-key-secret: ${ALIYUN_SMS_ACCESS_KEY_SECRET}
 *     sign-name: ${ALIYUN_SMS_SIGN_NAME}
 *     scheme-name: ${ALIYUN_SMS_SCHEME_NAME:}
 *     template-code-register: ${ALIYUN_SMS_TEMPLATE_CODE_REGISTER}
 *     template-code-login: ${ALIYUN_SMS_TEMPLATE_CODE_LOGIN}
 *     template-code-reset-password: ${ALIYUN_SMS_TEMPLATE_CODE_RESET_PASSWORD}
 *     template-code-change-phone: ${ALIYUN_SMS_TEMPLATE_CODE_CHANGE_PHONE}
 *     valid-time-seconds: 300
 *     interval-seconds: 60
 *     code-length: 6
 * </pre>
 *
 * @author trading
 */
@ConfigurationProperties(prefix = "ruoyi.sms")
public class SmsProperties
{
    /** 服务接入地址，默认 dypnsapi.aliyuncs.com */
    private String endpoint = "dypnsapi.aliyuncs.com";

    /** AccessKey ID */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** 短信签名名称（须在控制台申请通过） */
    private String signName;

    /** 方案名称（可选，号码认证方案，未启用方案时留空） */
    private String schemeName;

    /** 注册场景短信模板 CODE */
    private String templateCodeRegister;

    /** 登录场景短信模板 CODE */
    private String templateCodeLogin;

    /** 忘记密码-重置场景短信模板 CODE */
    private String templateCodeResetPassword;

    /** 换绑手机号场景短信模板 CODE */
    private String templateCodeChangePhone;

    /**
     * 注册场景 TemplateParam（JSON 字符串）。
     * {@code ${code}} 由阿里云 SendSmsVerifyCode 自动生成注入，仅需在此填模板里**除 code 之外**的其他变量；
     * 例如模板"您的验证码${code}，${min}分钟内有效"应配置 {@code {"min":"5"}}。
     * 默认空对象 {@code {}}，适用于模板只含 ${code} 的场景。
     */
    private String templateParamRegister = "{}";

    /** 登录场景 TemplateParam（JSON 字符串），同上 */
    private String templateParamLogin = "{}";

    /** 忘记密码-重置场景 TemplateParam（JSON 字符串），同上 */
    private String templateParamResetPassword = "{}";

    /** 换绑手机号场景 TemplateParam（JSON 字符串），同上 */
    private String templateParamChangePhone = "{}";

    /** 验证码有效期（秒），默认 300，阿里云允许 60-1800 */
    private Integer validTimeSeconds = 300;

    /** 同一手机号发送间隔（秒），默认 60，阿里云允许 60-86400 */
    private Integer intervalSeconds = 60;

    /** 验证码长度，默认 6，阿里云允许 4-8 */
    private Integer codeLength = 6;

    public String getEndpoint()
    {
        return endpoint;
    }

    public void setEndpoint(String endpoint)
    {
        this.endpoint = endpoint;
    }

    public String getAccessKeyId()
    {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId)
    {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret()
    {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret)
    {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSignName()
    {
        return signName;
    }

    public void setSignName(String signName)
    {
        this.signName = signName;
    }

    public String getSchemeName()
    {
        return schemeName;
    }

    public void setSchemeName(String schemeName)
    {
        this.schemeName = schemeName;
    }

    public String getTemplateCodeRegister()
    {
        return templateCodeRegister;
    }

    public void setTemplateCodeRegister(String templateCodeRegister)
    {
        this.templateCodeRegister = templateCodeRegister;
    }

    public String getTemplateCodeLogin()
    {
        return templateCodeLogin;
    }

    public void setTemplateCodeLogin(String templateCodeLogin)
    {
        this.templateCodeLogin = templateCodeLogin;
    }

    public String getTemplateCodeResetPassword()
    {
        return templateCodeResetPassword;
    }

    public void setTemplateCodeResetPassword(String templateCodeResetPassword)
    {
        this.templateCodeResetPassword = templateCodeResetPassword;
    }

    public String getTemplateCodeChangePhone()
    {
        return templateCodeChangePhone;
    }

    public void setTemplateCodeChangePhone(String templateCodeChangePhone)
    {
        this.templateCodeChangePhone = templateCodeChangePhone;
    }

    public String getTemplateParamRegister()
    {
        return templateParamRegister;
    }

    public void setTemplateParamRegister(String templateParamRegister)
    {
        this.templateParamRegister = templateParamRegister;
    }

    public String getTemplateParamLogin()
    {
        return templateParamLogin;
    }

    public void setTemplateParamLogin(String templateParamLogin)
    {
        this.templateParamLogin = templateParamLogin;
    }

    public String getTemplateParamResetPassword()
    {
        return templateParamResetPassword;
    }

    public void setTemplateParamResetPassword(String templateParamResetPassword)
    {
        this.templateParamResetPassword = templateParamResetPassword;
    }

    public String getTemplateParamChangePhone()
    {
        return templateParamChangePhone;
    }

    public void setTemplateParamChangePhone(String templateParamChangePhone)
    {
        this.templateParamChangePhone = templateParamChangePhone;
    }

    public Integer getValidTimeSeconds()
    {
        return validTimeSeconds;
    }

    public void setValidTimeSeconds(Integer validTimeSeconds)
    {
        this.validTimeSeconds = validTimeSeconds;
    }

    public Integer getIntervalSeconds()
    {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds)
    {
        this.intervalSeconds = intervalSeconds;
    }

    public Integer getCodeLength()
    {
        return codeLength;
    }

    public void setCodeLength(Integer codeLength)
    {
        this.codeLength = codeLength;
    }
}
