package com.ruoyi.common.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.ruoyi.common.storage.impl.AliyunOssStorageService;
import com.ruoyi.common.storage.impl.LocalStorageService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 文件存储自动装配。
 * <p>
 * 按 {@code ruoyi.storage.type} 选择实现：
 * <ul>
 *     <li>local（默认）—— {@link LocalStorageService}，写到 {@code ruoyi.profile} 本地目录</li>
 *     <li>aliyun-oss —— {@link AliyunOssStorageService}，需配置 {@code ruoyi.storage.aliyun.*} 参数</li>
 * </ul>
 *
 * @author trading
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration
{
    private static final Logger log = LoggerFactory.getLogger(StorageAutoConfiguration.class);

    @Bean
    public StorageService storageService(StorageProperties properties)
    {
        StorageType type = properties.resolveType();
        if (type == StorageType.ALIYUN_OSS)
        {
            StorageProperties.Aliyun aliyun = properties.getAliyun();
            assertOssConfig(aliyun);
            OSS client = new OSSClientBuilder().build(
                    aliyun.getEndpoint(),
                    aliyun.getAccessKeyId(),
                    aliyun.getAccessKeySecret());
            log.info("文件存储后端启用: 阿里云 OSS bucket={}, endpoint={}", aliyun.getBucketName(), aliyun.getEndpoint());
            return new AliyunOssStorageService(client, aliyun);
        }
        log.info("文件存储后端启用: 本地磁盘 (ruoyi.profile)");
        return new LocalStorageService();
    }

    private static void assertOssConfig(StorageProperties.Aliyun aliyun)
    {
        if (aliyun == null
                || StringUtils.isEmpty(aliyun.getEndpoint())
                || StringUtils.isEmpty(aliyun.getAccessKeyId())
                || StringUtils.isEmpty(aliyun.getAccessKeySecret())
                || StringUtils.isEmpty(aliyun.getBucketName()))
        {
            throw new IllegalStateException(
                    "ruoyi.storage.type=aliyun-oss 但 ruoyi.storage.aliyun.* 配置不完整，请检查 endpoint/accessKeyId/accessKeySecret/bucketName");
        }
    }
}
