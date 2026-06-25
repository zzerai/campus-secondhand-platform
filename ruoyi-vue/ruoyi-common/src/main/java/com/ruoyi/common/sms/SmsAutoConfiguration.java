package com.ruoyi.common.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import com.ruoyi.common.utils.StringUtils;

/**
 * 阿里云号码认证服务（dypnsapi）SDK 自动装配。
 *
 * <p>AK 未配置时仍会构造 Client（带空凭证），实际调用会被阿里云拒绝；
 * 这样可避免缺 SMS 配置导致整个应用启动失败，但 SMS 相关接口会在调用时显式报错。</p>
 *
 * @author trading
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsAutoConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(SmsAutoConfiguration.class);

    @Bean
    public Client dypnsapiClient(SmsProperties properties) throws Exception
    {
        Config config = new Config()
                .setAccessKeyId(properties.getAccessKeyId())
                .setAccessKeySecret(properties.getAccessKeySecret())
                .setEndpoint(StringUtils.isEmpty(properties.getEndpoint())
                        ? "dypnsapi.aliyuncs.com" : properties.getEndpoint());
        Client client = new Client(config);
        if (StringUtils.isEmpty(properties.getAccessKeyId())
                || StringUtils.isEmpty(properties.getAccessKeySecret()))
        {
            log.warn("阿里云号码认证 Client 已创建但 AccessKey 未配置；SMS 接口调用将失败，"
                    + "请在 .env 配置 ALIYUN_SMS_ACCESS_KEY_ID / ALIYUN_SMS_ACCESS_KEY_SECRET");
        }
        else
        {
            log.info("阿里云号码认证 Client 已启用: endpoint={}, signName={}",
                    config.endpoint, properties.getSignName());
        }
        return client;
    }
}
