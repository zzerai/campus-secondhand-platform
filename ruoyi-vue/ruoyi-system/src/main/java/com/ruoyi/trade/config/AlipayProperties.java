package com.ruoyi.trade.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * Alipay sandbox payment configuration.
 */
@Component
@ConfigurationProperties(prefix = "ruoyi.alipay")
public class AlipayProperties
{
    private String appId;

    private String merchantPrivateKey;

    private String alipayPublicKey;

    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private String notifyUrl;

    private String returnUrl;

    private String charset = "UTF-8";

    private String signType = "RSA2";

    public boolean isConfigured()
    {
        return StringUtils.isNotEmpty(appId)
                && StringUtils.isNotEmpty(merchantPrivateKey)
                && StringUtils.isNotEmpty(alipayPublicKey)
                && StringUtils.isNotEmpty(gatewayUrl)
                && StringUtils.isNotEmpty(notifyUrl)
                && StringUtils.isNotEmpty(returnUrl);
    }

    public String getAppId()
    {
        return appId;
    }

    public void setAppId(String appId)
    {
        this.appId = StringUtils.trim(appId);
    }

    public String getMerchantPrivateKey()
    {
        return merchantPrivateKey;
    }

    public void setMerchantPrivateKey(String merchantPrivateKey)
    {
        this.merchantPrivateKey = StringUtils.trim(merchantPrivateKey);
    }

    public String getAlipayPublicKey()
    {
        return alipayPublicKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey)
    {
        this.alipayPublicKey = StringUtils.trim(alipayPublicKey);
    }

    public String getGatewayUrl()
    {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl)
    {
        this.gatewayUrl = StringUtils.trim(gatewayUrl);
    }

    public String getNotifyUrl()
    {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl)
    {
        this.notifyUrl = StringUtils.trim(notifyUrl);
    }

    public String getReturnUrl()
    {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl)
    {
        this.returnUrl = StringUtils.trim(returnUrl);
    }

    public String getCharset()
    {
        return charset;
    }

    public void setCharset(String charset)
    {
        this.charset = StringUtils.trim(charset);
    }

    public String getSignType()
    {
        return signType;
    }

    public void setSignType(String signType)
    {
        this.signType = StringUtils.trim(signType);
    }
}
