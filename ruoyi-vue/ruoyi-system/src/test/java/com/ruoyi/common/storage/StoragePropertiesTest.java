package com.ruoyi.common.storage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StoragePropertiesTest
{
    @Test
    void resolveType_defaultsToLocal()
    {
        StorageProperties props = new StorageProperties();
        assertEquals(StorageType.LOCAL, props.resolveType());
    }

    @Test
    void resolveType_acceptsAliyunOss()
    {
        StorageProperties props = new StorageProperties();
        props.setType("aliyun-oss");
        assertEquals(StorageType.ALIYUN_OSS, props.resolveType());
    }

    @Test
    void aliyunNestedConfig_isolatesPerInstance()
    {
        StorageProperties props = new StorageProperties();
        props.getAliyun().setBucketName("my-bucket");
        props.getAliyun().setEndpoint("oss-cn-hangzhou.aliyuncs.com");

        StorageProperties another = new StorageProperties();
        assertEquals(null, another.getAliyun().getBucketName(),
                "嵌套属性必须是实例级而非静态字段");
    }
}
