package com.ruoyi.framework.web.service;

import com.ruoyi.common.core.domain.model.AppLoginBody;
import com.ruoyi.common.core.domain.model.AppTokenInfo;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.dto.AppChangePhoneDto;
import com.ruoyi.trade.domain.dto.AppChangePwdDto;
import com.ruoyi.trade.domain.dto.AppProfileUpdateDto;
import com.ruoyi.trade.domain.dto.AppRegisterDto;
import com.ruoyi.trade.domain.dto.AppResetPwdDto;

/**
 * 移动端认证Service接口
 * 
 * @author ruoyi
 */
public interface AppAuthService
{
    /**
     * 学生用户登录
     * 
     * @param loginBody 登录信息
     * @return 登录结果（包含token和学生信息）
     */
    AppTokenInfo login(AppLoginBody loginBody);

    /**
     * 学生用户注册
     *
     * @param registerDto 注册入参（仅含学号、手机号、密码、昵称）
     * @return 注册结果消息，成功返回null
     */
    String register(AppRegisterDto registerDto);

    /**
     * 发送移动端短信验证码（前置校验 + 调用阿里云下发）。
     *
     * @param phone 手机号
     * @param scene 场景，{@code register} / {@code reset_password} 要求已注册逻辑，
     *              {@code login} 要求已注册，{@code change_phone} 要求新号未被使用
     * @return 错误消息；成功返回 null
     */
    String sendSmsCode(String phone, String scene);

    /**
     * 退出登录
     * 
     * @param token token
     */
    void logout(String token);

    /**
     * 根据手机号/学号查询学生用户
     * 
     * @param username 手机号或学号
     * @return 学生用户
     */
    TrStudentUser selectByPhone(String username);

    /**
     * 获取当前登录用户的个人信息
     * 
     * @param request HTTP请求
     * @return 脱敏后的学生用户信息
     */
    AppTokenInfo.AppStudentUser getUserProfile(jakarta.servlet.http.HttpServletRequest request);

    /**
     * 更新当前登录用户的信息
     *
     * @param request HTTP请求
     * @param profileDto 要更新的用户信息（仅允许avatar、nickname、contactWay）
     * @return 结果
     */
    String updateUserProfile(jakarta.servlet.http.HttpServletRequest request, AppProfileUpdateDto profileDto);

    /**
     * 从请求中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID，未登录返回null
     */
    Long getUserIdFromRequest(jakarta.servlet.http.HttpServletRequest request);

    /**
     * 登录态修改密码。成功后清除当前 token，前端需引导用户用新密码重新登录。
     *
     * @param request HTTP请求
     * @param changePwdDto 旧密码 + 新密码
     * @return 错误消息；成功返回 null
     */
    String changePassword(jakarta.servlet.http.HttpServletRequest request, AppChangePwdDto changePwdDto);

    /**
     * 忘记密码-通过短信验证码重置密码（无需登录态）。
     *
     * @param resetPwdDto 手机号 + 验证码 + 新密码
     * @return 错误消息；成功返回 null
     */
    String resetPassword(AppResetPwdDto resetPwdDto);

    /**
     * 登录态换绑手机号。仅验证新手机号收到的验证码（已登录即已证明账户所有权）。
     *
     * @param request HTTP请求
     * @param changePhoneDto 新手机号 + 验证码
     * @return 错误消息；成功返回 null
     */
    String changePhone(jakarta.servlet.http.HttpServletRequest request, AppChangePhoneDto changePhoneDto);
}
