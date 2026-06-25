package com.ruoyi.common.storage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link StorageType#from(String)} 解析单测，覆盖大小写、连字符、null 等输入。
 */
class StorageTypeTest
{
    @Test
    void from_recognizesLocalLiteral()
    {
        assertEquals(StorageType.LOCAL, StorageType.from("local"));
        assertEquals(StorageType.LOCAL, StorageType.from("LOCAL"));
        assertEquals(StorageType.LOCAL, StorageType.from(" Local "));
    }

    @Test
    void from_recognizesAliyunOssWithDashOrUnderscore()
    {
        assertEquals(StorageType.ALIYUN_OSS, StorageType.from("aliyun-oss"));
        assertEquals(StorageType.ALIYUN_OSS, StorageType.from("ALIYUN_OSS"));
        assertEquals(StorageType.ALIYUN_OSS, StorageType.from("Aliyun-Oss"));
    }

    @Test
    void from_defaultsToLocalForNullOrUnknown()
    {
        assertEquals(StorageType.LOCAL, StorageType.from(null));
        assertEquals(StorageType.LOCAL, StorageType.from(""));
        assertEquals(StorageType.LOCAL, StorageType.from("s3"));
    }
}
