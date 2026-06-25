package com.ruoyi.common.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置（前缀 {@code ruoyi.storage}）。
 *
 * <pre>
 * ruoyi:
 *   storage:
 *     type: local        # local | aliyun-oss
 *     aliyun:
 *       endpoint: ${OSS_ENDPOINT}
 *       access-key-id: ${OSS_ACCESS_KEY_ID}
 *       access-key-secret: ${OSS_ACCESS_KEY_SECRET}
 *       bucket-name: ${OSS_BUCKET}
 *       url-prefix: ${OSS_URL_PREFIX:}  # 可选，自定义 CDN/域名前缀
 * </pre>
 *
 * @author trading
 */
@ConfigurationProperties(prefix = "ruoyi.storage")
public class StorageProperties
{
    /** 存储后端类型：local 或 aliyun-oss，默认 local */
    private String type = "local";

    /** 阿里云 OSS 配置 */
    private Aliyun aliyun = new Aliyun();

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Aliyun getAliyun()
    {
        return aliyun;
    }

    public void setAliyun(Aliyun aliyun)
    {
        this.aliyun = aliyun;
    }

    public StorageType resolveType()
    {
        return StorageType.from(type);
    }

    public static class Aliyun
    {
        /** OSS 接入域名，如 oss-cn-hangzhou.aliyuncs.com */
        private String endpoint;

        /** AccessKey ID */
        private String accessKeyId;

        /** AccessKey Secret */
        private String accessKeySecret;

        /** Bucket 名称 */
        private String bucketName;

        /** 可选的访问域名前缀（CDN 或自定义域名），不填则使用默认 https://{bucket}.{endpoint} */
        private String urlPrefix;

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

        public String getBucketName()
        {
            return bucketName;
        }

        public void setBucketName(String bucketName)
        {
            this.bucketName = bucketName;
        }

        public String getUrlPrefix()
        {
            return urlPrefix;
        }

        public void setUrlPrefix(String urlPrefix)
        {
            this.urlPrefix = urlPrefix;
        }
    }
}
