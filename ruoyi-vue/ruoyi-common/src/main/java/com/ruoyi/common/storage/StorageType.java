package com.ruoyi.common.storage;

/**
 * 文件存储后端类型
 *
 * @author trading
 */
public enum StorageType
{
    /** 本地磁盘存储（开发环境默认） */
    LOCAL,

    /** 阿里云 OSS（生产环境推荐） */
    ALIYUN_OSS;

    public static StorageType from(String value)
    {
        if (value == null)
        {
            return LOCAL;
        }
        String normalized = value.trim().toUpperCase().replace('-', '_');
        for (StorageType type : values())
        {
            if (type.name().equals(normalized))
            {
                return type;
            }
        }
        return LOCAL;
    }
}
