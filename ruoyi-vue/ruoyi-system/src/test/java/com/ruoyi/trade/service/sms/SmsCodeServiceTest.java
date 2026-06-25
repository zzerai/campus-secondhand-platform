package com.ruoyi.trade.service.sms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.sms.SmsProperties;

/**
 * 短信验证码 Service 单元测试。
 *
 * <p>Mock 阿里云 Client，验证 {@link SmsCodeService} 对 SendSmsVerifyCode / CheckSmsVerifyCode 的
 * 参数装配、响应判定与异常处理逻辑。</p>
 *
 * @author trading
 */
@ExtendWith(MockitoExtension.class)
class SmsCodeServiceTest
{
    private static final String PHONE = "13800000001";

    @Mock
    private Client dypnsapiClient;

    private SmsProperties smsProperties;

    @InjectMocks
    private SmsCodeService smsCodeService;

    @BeforeEach
    void setUp() throws Exception
    {
        smsProperties = new SmsProperties();
        smsProperties.setAccessKeyId("test-ak");
        smsProperties.setAccessKeySecret("test-sk");
        smsProperties.setSignName("校园闲置");
        smsProperties.setTemplateCodeRegister("SMS_REG_001");
        smsProperties.setTemplateCodeLogin("SMS_LOGIN_001");
        smsProperties.setValidTimeSeconds(300);
        smsProperties.setIntervalSeconds(60);
        smsProperties.setCodeLength(6);
        // 由于 @InjectMocks 不会注入 @Spy/非 Mock 字段，手动反射设置
        java.lang.reflect.Field field = SmsCodeService.class.getDeclaredField("smsProperties");
        field.setAccessible(true);
        field.set(smsCodeService, smsProperties);
    }

    @Test
    void sendSmsCodeRegisterShouldUseRegisterTemplateAndSucceedOnOk() throws Exception
    {
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(okSendResponse("req-1"));

        smsCodeService.sendSmsCode(PHONE, SmsScene.REGISTER);

        ArgumentCaptor<SendSmsVerifyCodeRequest> captor = ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).sendSmsVerifyCode(captor.capture());
        SendSmsVerifyCodeRequest req = captor.getValue();
        Assertions.assertEquals(PHONE, req.getPhoneNumber());
        Assertions.assertEquals("校园闲置", req.getSignName());
        Assertions.assertEquals("SMS_REG_001", req.getTemplateCode());
        Assertions.assertEquals(Long.valueOf(300L), req.getValidTime());
        Assertions.assertEquals(Long.valueOf(60L), req.getInterval());
        Assertions.assertEquals(Long.valueOf(6L), req.getCodeLength());
    }

    @Test
    void sendSmsCodeLoginShouldUseLoginTemplate() throws Exception
    {
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(okSendResponse("req-2"));

        smsCodeService.sendSmsCode(PHONE, SmsScene.LOGIN);

        ArgumentCaptor<SendSmsVerifyCodeRequest> captor = ArgumentCaptor.forClass(SendSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).sendSmsVerifyCode(captor.capture());
        Assertions.assertEquals("SMS_LOGIN_001", captor.getValue().getTemplateCode());
    }

    @Test
    void sendSmsCodeShouldThrowOnNonOkResponse() throws Exception
    {
        SendSmsVerifyCodeResponse resp = new SendSmsVerifyCodeResponse()
                .setBody(new SendSmsVerifyCodeResponseBody()
                        .setCode("isv.OUT_OF_SERVICE")
                        .setMessage("业务停机"));
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenReturn(resp);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> smsCodeService.sendSmsCode(PHONE, SmsScene.REGISTER));
        Assertions.assertTrue(ex.getMessage().contains("业务停机"));
    }

    @Test
    void sendSmsCodeShouldThrowWhenClientRaises() throws Exception
    {
        when(dypnsapiClient.sendSmsVerifyCode(any())).thenThrow(new RuntimeException("network down"));

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> smsCodeService.sendSmsCode(PHONE, SmsScene.REGISTER));
        Assertions.assertTrue(ex.getMessage().contains("短信发送异常"));
    }

    @Test
    void sendSmsCodeShouldThrowWhenAccessKeyMissing()
    {
        smsProperties.setAccessKeyId("");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> smsCodeService.sendSmsCode(PHONE, SmsScene.REGISTER));
        Assertions.assertTrue(ex.getMessage().contains("AccessKey"));
    }

    @Test
    void sendSmsCodeShouldThrowWhenTemplateMissing()
    {
        smsProperties.setTemplateCodeRegister("");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> smsCodeService.sendSmsCode(PHONE, SmsScene.REGISTER));
        Assertions.assertTrue(ex.getMessage().contains("短信模板未配置"));
    }

    @Test
    void checkSmsCodeShouldReturnTrueOnPass() throws Exception
    {
        when(dypnsapiClient.checkSmsVerifyCode(any())).thenReturn(checkResponse("PASS"));

        Assertions.assertTrue(smsCodeService.checkSmsCode(PHONE, "123456"));

        ArgumentCaptor<CheckSmsVerifyCodeRequest> captor =
                ArgumentCaptor.forClass(CheckSmsVerifyCodeRequest.class);
        verify(dypnsapiClient).checkSmsVerifyCode(captor.capture());
        Assertions.assertEquals(PHONE, captor.getValue().getPhoneNumber());
        Assertions.assertEquals("123456", captor.getValue().getVerifyCode());
    }

    @Test
    void checkSmsCodeShouldReturnFalseOnUnknown() throws Exception
    {
        when(dypnsapiClient.checkSmsVerifyCode(any())).thenReturn(checkResponse("UNKNOWN"));

        Assertions.assertFalse(smsCodeService.checkSmsCode(PHONE, "000000"));
    }

    @Test
    void checkSmsCodeShouldReturnFalseOnNonOkResponse() throws Exception
    {
        CheckSmsVerifyCodeResponse resp = new CheckSmsVerifyCodeResponse()
                .setBody(new CheckSmsVerifyCodeResponseBody()
                        .setCode("isv.INVALID_PARAM")
                        .setMessage("参数错误"));
        when(dypnsapiClient.checkSmsVerifyCode(any())).thenReturn(resp);

        Assertions.assertFalse(smsCodeService.checkSmsCode(PHONE, "123456"));
    }

    @Test
    void checkSmsCodeShouldReturnFalseWhenClientThrows() throws Exception
    {
        when(dypnsapiClient.checkSmsVerifyCode(any())).thenThrow(new RuntimeException("boom"));

        Assertions.assertFalse(smsCodeService.checkSmsCode(PHONE, "123456"));
    }

    @Test
    void checkSmsCodeShouldReturnFalseOnEmptyInput()
    {
        Assertions.assertFalse(smsCodeService.checkSmsCode("", "123456"));
        Assertions.assertFalse(smsCodeService.checkSmsCode(PHONE, ""));
    }

    private static SendSmsVerifyCodeResponse okSendResponse(String requestId)
    {
        SendSmsVerifyCodeResponseBody.SendSmsVerifyCodeResponseBodyModel model =
                new SendSmsVerifyCodeResponseBody.SendSmsVerifyCodeResponseBodyModel()
                        .setRequestId(requestId);
        return new SendSmsVerifyCodeResponse()
                .setBody(new SendSmsVerifyCodeResponseBody()
                        .setCode("OK")
                        .setMessage("OK")
                        .setModel(model));
    }

    private static CheckSmsVerifyCodeResponse checkResponse(String verifyResult)
    {
        CheckSmsVerifyCodeResponseBody.CheckSmsVerifyCodeResponseBodyModel model =
                new CheckSmsVerifyCodeResponseBody.CheckSmsVerifyCodeResponseBodyModel()
                        .setVerifyResult(verifyResult);
        return new CheckSmsVerifyCodeResponse()
                .setBody(new CheckSmsVerifyCodeResponseBody()
                        .setCode("OK")
                        .setMessage("OK")
                        .setModel(model));
    }

    private static <T> T any()
    {
        return org.mockito.ArgumentMatchers.any();
    }
}
