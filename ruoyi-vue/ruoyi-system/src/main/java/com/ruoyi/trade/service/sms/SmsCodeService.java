package com.ruoyi.trade.service.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.aliyun.tea.TeaException;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.sms.SmsProperties;
import com.ruoyi.common.utils.StringUtils;

/**
 * 阿里云号码认证服务短信验证码 Service。
 *
 * <p>验证码的生成、存储、限频、有效期全部由阿里云托管：</p>
 * <ul>
 *   <li>{@code SendSmsVerifyCode} 内置 {@code Interval} 秒级限频与 {@code ValidTime} 有效期</li>
 *   <li>{@code CheckSmsVerifyCode} 服务端核验，{@code Model.VerifyResult == "PASS"} 为通过</li>
 * </ul>
 *
 * @author trading
 */
@Service
public class SmsCodeService
{
    private static final Logger log = LoggerFactory.getLogger(SmsCodeService.class);

    /** 阿里云核验通过标识 */
    private static final String VERIFY_RESULT_PASS = "PASS";

    /** 阿里云接口调用成功标识 */
    private static final String RESPONSE_CODE_OK = "OK";

    /** TemplateParam 缺省值：仅适用于模板里只含 ${code} 的场景 */
    private static final String DEFAULT_TEMPLATE_PARAM = "{}";

    @Autowired
    private Client dypnsapiClient;

    @Autowired
    private SmsProperties smsProperties;

    /**
     * 发送短信验证码。失败时抛 {@link ServiceException}。
     *
     * @param phone 手机号
     * @param scene 业务场景，按场景选取不同的短信模板
     */
    public void sendSmsCode(String phone, SmsScene scene)
    {
        ensureCredentialsConfigured();
        String templateCode = resolveTemplateCode(scene);
        if (StringUtils.isEmpty(templateCode))
        {
            throw new ServiceException("短信模板未配置（场景：" + scene + "），请检查 ruoyi.sms 配置");
        }
        if (StringUtils.isEmpty(smsProperties.getSignName()))
        {
            throw new ServiceException("短信签名未配置，请检查 ruoyi.sms.sign-name");
        }

        String templateParam = resolveTemplateParam(scene);
        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setSignName(smsProperties.getSignName())
                .setTemplateCode(templateCode)
                .setTemplateParam(templateParam)
                .setValidTime(smsProperties.getValidTimeSeconds().longValue())
                .setInterval(smsProperties.getIntervalSeconds().longValue())
                .setCodeLength(smsProperties.getCodeLength().longValue());
        if (StringUtils.isNotEmpty(smsProperties.getSchemeName()))
        {
            request.setSchemeName(smsProperties.getSchemeName());
        }

        log.info("准备调用阿里云 SendSmsVerifyCode phone={} signName=[{}] templateCode=[{}] templateParam={} validTime={} interval={} codeLength={} schemeName=[{}] endpoint={}",
                desensitize(phone), smsProperties.getSignName(), templateCode,
                templateParam, smsProperties.getValidTimeSeconds(),
                smsProperties.getIntervalSeconds(), smsProperties.getCodeLength(),
                smsProperties.getSchemeName(), smsProperties.getEndpoint());

        try
        {
            SendSmsVerifyCodeResponse response = dypnsapiClient.sendSmsVerifyCode(request);
            SendSmsVerifyCodeResponseBody body = response == null ? null : response.getBody();
            String acsRequestId = response == null || response.getHeaders() == null
                    ? null : response.getHeaders().get("x-acs-request-id");
            if (body == null || !RESPONSE_CODE_OK.equals(body.getCode()))
            {
                String code = body == null ? "null" : body.getCode();
                String message = body == null ? "无响应体" : body.getMessage();
                Boolean success = body == null ? null : body.getSuccess();
                String accessDenied = body == null ? null : body.getAccessDeniedDetail();
                log.warn("阿里云短信下发失败 phone={} scene={} code={} message={} success={} accessDenied={} acsRequestId={}",
                        desensitize(phone), scene, code, message, success, accessDenied, acsRequestId);
                throw new ServiceException("短信发送失败：" + message);
            }
            String requestId = body.getModel() == null ? null : body.getModel().getRequestId();
            log.info("阿里云短信下发成功 phone={} scene={} requestId={} acsRequestId={}",
                    desensitize(phone), scene, requestId, acsRequestId);
        }
        catch (ServiceException se)
        {
            throw se;
        }
        catch (TeaException te)
        {
            Object recommend = te.getData() == null ? null : te.getData().get("Recommend");
            log.error("调用阿里云 SendSmsVerifyCode TeaException phone={} scene={} code={} message={} recommend={}",
                    desensitize(phone), scene, te.getCode(), te.getMessage(), recommend);
            throw new ServiceException("短信发送异常：" + te.getMessage()
                    + (recommend == null ? "" : " | 诊断: " + recommend));
        }
        catch (Exception e)
        {
            log.error("调用阿里云 SendSmsVerifyCode 异常 phone={} scene={}", desensitize(phone), scene, e);
            throw new ServiceException("短信发送异常，请稍后重试");
        }
    }

    /**
     * 校验短信验证码。仅当阿里云返回 {@code Model.VerifyResult == "PASS"} 视为通过。
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true 通过；false 未通过（含验证码错误、过期、未发送等所有失败原因）
     */
    public boolean checkSmsCode(String phone, String code)
    {
        ensureCredentialsConfigured();
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(code))
        {
            return false;
        }

        CheckSmsVerifyCodeRequest request = new CheckSmsVerifyCodeRequest()
                .setPhoneNumber(phone)
                .setVerifyCode(code);
        if (StringUtils.isNotEmpty(smsProperties.getSchemeName()))
        {
            request.setSchemeName(smsProperties.getSchemeName());
        }

        try
        {
            CheckSmsVerifyCodeResponse response = dypnsapiClient.checkSmsVerifyCode(request);
            CheckSmsVerifyCodeResponseBody body = response == null ? null : response.getBody();
            if (body == null || !RESPONSE_CODE_OK.equals(body.getCode()))
            {
                String respCode = body == null ? "null" : body.getCode();
                String message = body == null ? "无响应体" : body.getMessage();
                log.warn("阿里云短信核验调用失败 phone={} code={} message={}",
                        desensitize(phone), respCode, message);
                return false;
            }
            CheckSmsVerifyCodeResponseBody.CheckSmsVerifyCodeResponseBodyModel model = body.getModel();
            return model != null && VERIFY_RESULT_PASS.equals(model.getVerifyResult());
        }
        catch (Exception e)
        {
            log.error("调用阿里云 CheckSmsVerifyCode 异常 phone={}", desensitize(phone), e);
            return false;
        }
    }

    private String resolveTemplateCode(SmsScene scene)
    {
        switch (scene)
        {
            case REGISTER:
                return smsProperties.getTemplateCodeRegister();
            case LOGIN:
                return smsProperties.getTemplateCodeLogin();
            case RESET_PASSWORD:
                return smsProperties.getTemplateCodeResetPassword();
            case CHANGE_PHONE:
                return smsProperties.getTemplateCodeChangePhone();
            default:
                throw new ServiceException("未支持的短信场景：" + scene);
        }
    }

    private String resolveTemplateParam(SmsScene scene)
    {
        String param;
        switch (scene)
        {
            case REGISTER:
                param = smsProperties.getTemplateParamRegister();
                break;
            case LOGIN:
                param = smsProperties.getTemplateParamLogin();
                break;
            case RESET_PASSWORD:
                param = smsProperties.getTemplateParamResetPassword();
                break;
            case CHANGE_PHONE:
                param = smsProperties.getTemplateParamChangePhone();
                break;
            default:
                throw new ServiceException("未支持的短信场景：" + scene);
        }
        param = stripOuterQuotes(param);
        return StringUtils.isEmpty(param) ? DEFAULT_TEMPLATE_PARAM : param;
    }

    /**
     * 去除字符串外层成对的单/双引号。
     * dotenv-java 在 {@code KEY='{"k":"v"}'} 形式下不会自动剥单引号，
     * 这里兜底处理，让 .env 用单引号、双引号或不加引号都能正确传给阿里云。
     */
    private static String stripOuterQuotes(String s)
    {
        if (s == null || s.length() < 2)
        {
            return s;
        }
        char first = s.charAt(0);
        char last = s.charAt(s.length() - 1);
        if ((first == '\'' || first == '"') && first == last)
        {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private void ensureCredentialsConfigured()
    {
        if (StringUtils.isEmpty(smsProperties.getAccessKeyId())
                || StringUtils.isEmpty(smsProperties.getAccessKeySecret()))
        {
            throw new ServiceException("短信服务未配置 AccessKey，请联系管理员");
        }
    }

    private static String desensitize(String phone)
    {
        if (phone == null || phone.length() < 11)
        {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
