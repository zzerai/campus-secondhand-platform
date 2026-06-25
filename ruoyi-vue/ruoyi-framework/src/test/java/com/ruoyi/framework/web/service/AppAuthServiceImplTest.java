package com.ruoyi.framework.web.service;

import java.lang.reflect.Field;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.dto.AppChangePhoneDto;
import com.ruoyi.trade.domain.dto.AppChangePwdDto;
import com.ruoyi.trade.domain.dto.AppResetPwdDto;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.service.sms.SmsCodeService;

/**
 * AppAuthServiceImpl 三个账户安全接口（改密码 / 重置密码 / 换绑手机号）的核心路径单元测试。
 *
 * <p>不触发任何阿里云真实调用 —— SmsCodeService 全部走 Mockito mock；
 * BCryptPasswordEncoder 用真实实例，保证 matches/encode 的真实语义。</p>
 */
@ExtendWith(MockitoExtension.class)
class AppAuthServiceImplTest
{
    @Mock
    private TrStudentUserMapper trStudentUserMapper;

    @Mock
    private TokenService tokenService;

    @Mock
    private SmsCodeService smsCodeService;

    @Mock
    private HttpServletRequest request;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AppAuthServiceImpl service;

    @BeforeEach
    void setUp() throws Exception
    {
        service = new AppAuthServiceImpl();
        inject("trStudentUserMapper", trStudentUserMapper);
        inject("tokenService", tokenService);
        inject("smsCodeService", smsCodeService);
        inject("passwordEncoder", passwordEncoder);
    }

    private void inject(String fieldName, Object value) throws Exception
    {
        Field f = AppAuthServiceImpl.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(service, value);
    }

    private TrStudentUser buildUser(Long userId, String phone, String rawPassword)
    {
        TrStudentUser u = new TrStudentUser();
        u.setUserId(userId);
        u.setStudentNo("S" + userId);
        u.setPhone(phone);
        u.setPassword(passwordEncoder.encode(rawPassword));
        u.setStatus("0");
        u.setDelFlag("0");
        return u;
    }

    private LoginUser buildLoginUser(Long userId, String token)
    {
        LoginUser lu = new LoginUser();
        lu.setUserId(userId);
        lu.setToken(token);
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        lu.setUser(sysUser);
        return lu;
    }

    // ===== changePassword =====

    @Test
    void changePassword_notLoggedIn_returnsError()
    {
        when(tokenService.getLoginUser(request)).thenReturn(null);
        AppChangePwdDto dto = new AppChangePwdDto();
        dto.setOldPassword("old");
        dto.setNewPassword("new");

        String msg = service.changePassword(request, dto);

        Assertions.assertEquals("用户未登录或登录已过期", msg);
        verify(trStudentUserMapper, never()).updateTrStudentUser(any());
    }

    @Test
    void changePassword_emptyParams_returnsError()
    {
        String msg = service.changePassword(request, new AppChangePwdDto());
        Assertions.assertEquals("新旧密码不能为空", msg);
        verify(tokenService, never()).getLoginUser(any(HttpServletRequest.class));
    }

    @Test
    void changePassword_wrongOldPassword_returnsError()
    {
        LoginUser lu = buildLoginUser(10L, "tok");
        TrStudentUser user = buildUser(10L, "13800000000", "correctOld");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(10L)).thenReturn(user);

        AppChangePwdDto dto = new AppChangePwdDto();
        dto.setOldPassword("wrongOld");
        dto.setNewPassword("newPwd");

        String msg = service.changePassword(request, dto);

        Assertions.assertEquals("旧密码错误", msg);
        verify(trStudentUserMapper, never()).updateTrStudentUser(any());
        verify(tokenService, never()).delLoginUser(anyString());
    }

    @Test
    void changePassword_sameAsOld_returnsError()
    {
        LoginUser lu = buildLoginUser(10L, "tok");
        TrStudentUser user = buildUser(10L, "13800000000", "samePwd");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(10L)).thenReturn(user);

        AppChangePwdDto dto = new AppChangePwdDto();
        dto.setOldPassword("samePwd");
        dto.setNewPassword("samePwd");

        String msg = service.changePassword(request, dto);

        Assertions.assertEquals("新密码不能与旧密码相同", msg);
        verify(trStudentUserMapper, never()).updateTrStudentUser(any());
    }

    @Test
    void changePassword_disabledAccount_returnsError()
    {
        LoginUser lu = buildLoginUser(10L, "tok");
        TrStudentUser user = buildUser(10L, "13800000000", "old");
        user.setStatus("1");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(10L)).thenReturn(user);

        AppChangePwdDto dto = new AppChangePwdDto();
        dto.setOldPassword("old");
        dto.setNewPassword("newPwd");

        String msg = service.changePassword(request, dto);

        Assertions.assertEquals("账号已被禁用", msg);
    }

    @Test
    void changePassword_success_persistsEncryptedAndClearsCurrentToken()
    {
        LoginUser lu = buildLoginUser(10L, "currentTok");
        TrStudentUser user = buildUser(10L, "13800000000", "oldPwd");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(10L)).thenReturn(user);
        when(trStudentUserMapper.updateTrStudentUser(any(TrStudentUser.class))).thenReturn(1);

        AppChangePwdDto dto = new AppChangePwdDto();
        dto.setOldPassword("oldPwd");
        dto.setNewPassword("newPwd");

        String msg = service.changePassword(request, dto);

        Assertions.assertNull(msg);
        ArgumentCaptor<TrStudentUser> captor = ArgumentCaptor.forClass(TrStudentUser.class);
        verify(trStudentUserMapper).updateTrStudentUser(captor.capture());
        TrStudentUser updated = captor.getValue();
        Assertions.assertEquals(10L, updated.getUserId());
        Assertions.assertNotNull(updated.getPassword());
        Assertions.assertTrue(passwordEncoder.matches("newPwd", updated.getPassword()),
                "入库的密码必须是 newPwd 的 BCrypt 哈希");
        Assertions.assertFalse(updated.getPassword().equals("newPwd"),
                "明文不得直接入库");
        verify(tokenService, times(1)).delLoginUser("currentTok");
    }

    // ===== resetPassword =====

    @Test
    void resetPassword_phoneNotRegistered_returnsError()
    {
        when(trStudentUserMapper.selectTrStudentUserByPhone("13800000000")).thenReturn(null);

        AppResetPwdDto dto = new AppResetPwdDto();
        dto.setPhone("13800000000");
        dto.setSmsCode("123456");
        dto.setNewPassword("newPwd");

        String msg = service.resetPassword(dto);

        Assertions.assertEquals("手机号未注册", msg);
        verify(smsCodeService, never()).checkSmsCode(anyString(), anyString());
    }

    @Test
    void resetPassword_invalidPhoneFormat_returnsError()
    {
        AppResetPwdDto dto = new AppResetPwdDto();
        dto.setPhone("abc");
        dto.setSmsCode("123456");
        dto.setNewPassword("newPwd");

        String msg = service.resetPassword(dto);

        Assertions.assertEquals("手机号格式不正确", msg);
    }

    @Test
    void resetPassword_wrongSmsCode_returnsError()
    {
        TrStudentUser user = buildUser(20L, "13800000000", "old");
        when(trStudentUserMapper.selectTrStudentUserByPhone("13800000000")).thenReturn(user);
        when(smsCodeService.checkSmsCode("13800000000", "999999")).thenReturn(false);

        AppResetPwdDto dto = new AppResetPwdDto();
        dto.setPhone("13800000000");
        dto.setSmsCode("999999");
        dto.setNewPassword("newPwd");

        String msg = service.resetPassword(dto);

        Assertions.assertEquals("短信验证码错误或已过期", msg);
        verify(trStudentUserMapper, never()).updateTrStudentUser(any());
    }

    @Test
    void resetPassword_disabledAccount_returnsError()
    {
        TrStudentUser user = buildUser(20L, "13800000000", "old");
        user.setStatus("1");
        when(trStudentUserMapper.selectTrStudentUserByPhone("13800000000")).thenReturn(user);

        AppResetPwdDto dto = new AppResetPwdDto();
        dto.setPhone("13800000000");
        dto.setSmsCode("123456");
        dto.setNewPassword("newPwd");

        String msg = service.resetPassword(dto);

        Assertions.assertEquals("账号已被禁用", msg);
        verify(smsCodeService, never()).checkSmsCode(anyString(), anyString());
    }

    @Test
    void resetPassword_success_persistsEncrypted()
    {
        TrStudentUser user = buildUser(20L, "13800000000", "old");
        when(trStudentUserMapper.selectTrStudentUserByPhone("13800000000")).thenReturn(user);
        when(smsCodeService.checkSmsCode("13800000000", "123456")).thenReturn(true);
        when(trStudentUserMapper.updateTrStudentUser(any(TrStudentUser.class))).thenReturn(1);

        AppResetPwdDto dto = new AppResetPwdDto();
        dto.setPhone("13800000000");
        dto.setSmsCode("123456");
        dto.setNewPassword("brandNew");

        String msg = service.resetPassword(dto);

        Assertions.assertNull(msg);
        ArgumentCaptor<TrStudentUser> captor = ArgumentCaptor.forClass(TrStudentUser.class);
        verify(trStudentUserMapper).updateTrStudentUser(captor.capture());
        TrStudentUser updated = captor.getValue();
        Assertions.assertEquals(20L, updated.getUserId());
        Assertions.assertTrue(passwordEncoder.matches("brandNew", updated.getPassword()));
    }

    // ===== changePhone =====

    @Test
    void changePhone_invalidNewPhoneFormat_returnsError()
    {
        AppChangePhoneDto dto = new AppChangePhoneDto();
        dto.setNewPhone("abc");
        dto.setSmsCode("123456");

        String msg = service.changePhone(request, dto);

        Assertions.assertEquals("新手机号格式不正确", msg);
        verify(tokenService, never()).getLoginUser(any(HttpServletRequest.class));
    }

    @Test
    void changePhone_sameAsOld_returnsError()
    {
        LoginUser lu = buildLoginUser(30L, "tok");
        TrStudentUser user = buildUser(30L, "13800000000", "pwd");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(30L)).thenReturn(user);

        AppChangePhoneDto dto = new AppChangePhoneDto();
        dto.setNewPhone("13800000000");
        dto.setSmsCode("123456");

        String msg = service.changePhone(request, dto);

        Assertions.assertEquals("新手机号不能与旧手机号相同", msg);
        verify(smsCodeService, never()).checkSmsCode(anyString(), anyString());
    }

    @Test
    void changePhone_phoneTakenByOtherActiveUser_returnsError()
    {
        LoginUser lu = buildLoginUser(30L, "tok");
        TrStudentUser self = buildUser(30L, "13800000000", "pwd");
        TrStudentUser other = buildUser(31L, "13900000000", "pwd2");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(30L)).thenReturn(self);
        when(trStudentUserMapper.selectAnyByPhone("13900000000")).thenReturn(other);

        AppChangePhoneDto dto = new AppChangePhoneDto();
        dto.setNewPhone("13900000000");
        dto.setSmsCode("123456");

        String msg = service.changePhone(request, dto);

        Assertions.assertEquals("新手机号已被其他用户使用", msg);
        verify(smsCodeService, never()).checkSmsCode(anyString(), anyString());
    }

    @Test
    void changePhone_wrongSmsCode_returnsError()
    {
        LoginUser lu = buildLoginUser(30L, "tok");
        TrStudentUser self = buildUser(30L, "13800000000", "pwd");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(30L)).thenReturn(self);
        when(trStudentUserMapper.selectAnyByPhone("13900000000")).thenReturn(null);
        when(smsCodeService.checkSmsCode("13900000000", "999999")).thenReturn(false);

        AppChangePhoneDto dto = new AppChangePhoneDto();
        dto.setNewPhone("13900000000");
        dto.setSmsCode("999999");

        String msg = service.changePhone(request, dto);

        Assertions.assertEquals("短信验证码错误或已过期", msg);
        verify(trStudentUserMapper, never()).updateTrStudentUser(any());
    }

    @Test
    void changePhone_success_updatesPhoneAndRefreshesToken()
    {
        LoginUser lu = buildLoginUser(30L, "currentTok");
        TrStudentUser self = buildUser(30L, "13800000000", "pwd");
        when(tokenService.getLoginUser(request)).thenReturn(lu);
        when(trStudentUserMapper.selectTrStudentUserByUserId(30L)).thenReturn(self);
        when(trStudentUserMapper.selectAnyByPhone("13900000000")).thenReturn(null);
        when(smsCodeService.checkSmsCode("13900000000", "123456")).thenReturn(true);
        when(trStudentUserMapper.updateTrStudentUser(any(TrStudentUser.class))).thenReturn(1);

        AppChangePhoneDto dto = new AppChangePhoneDto();
        dto.setNewPhone("13900000000");
        dto.setSmsCode("123456");

        String msg = service.changePhone(request, dto);

        Assertions.assertNull(msg);
        ArgumentCaptor<TrStudentUser> updateCaptor = ArgumentCaptor.forClass(TrStudentUser.class);
        verify(trStudentUserMapper).updateTrStudentUser(updateCaptor.capture());
        Assertions.assertEquals("13900000000", updateCaptor.getValue().getPhone());

        ArgumentCaptor<LoginUser> refreshCaptor = ArgumentCaptor.forClass(LoginUser.class);
        verify(tokenService).refreshToken(refreshCaptor.capture());
        LoginUser refreshed = refreshCaptor.getValue();
        Assertions.assertEquals("currentTok", refreshed.getToken());
        Assertions.assertEquals("13900000000", refreshed.getUser().getPhonenumber(),
                "刷新后的 LoginUser 必须带上新手机号");
        Assertions.assertEquals("13900000000", refreshed.getUser().getUserName());
    }

    // ===== sendSmsCode 新 scene 前置校验 =====

    @Test
    void sendSmsCode_resetPassword_phoneNotRegistered_returnsError()
    {
        when(trStudentUserMapper.selectTrStudentUserByPhone("13800000000")).thenReturn(null);

        String msg = service.sendSmsCode("13800000000", "reset_password");

        Assertions.assertEquals("手机号未注册，请先注册！", msg);
        verify(smsCodeService, never()).sendSmsCode(anyString(), any());
    }

    @Test
    void sendSmsCode_changePhone_phoneAlreadyTaken_returnsError()
    {
        TrStudentUser any = buildUser(99L, "13900000000", "pwd");
        when(trStudentUserMapper.selectTrStudentUserByPhone("13900000000")).thenReturn(any);

        String msg = service.sendSmsCode("13900000000", "change_phone");

        Assertions.assertEquals("新手机号已被使用", msg);
        verify(smsCodeService, never()).sendSmsCode(anyString(), any());
    }
}
