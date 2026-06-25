package com.ruoyi.trade.service.sms;

/**
 * 短信验证码场景。每个场景对应阿里云控制台不同的短信模板 CODE，便于审核合规与统计。
 *
 * @author trading
 */
public enum SmsScene
{
    /** 注册场景：用于新用户绑定手机号 */
    REGISTER,

    /** 登录场景：用于已注册用户短信验证码登录 */
    LOGIN,

    /** 忘记密码-重置场景：用于已注册手机号验证码重置密码 */
    RESET_PASSWORD,

    /** 换绑手机号场景：用于已登录用户向新手机号下发验证码 */
    CHANGE_PHONE
}
