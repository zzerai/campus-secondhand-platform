package com.ruoyi.common.storage.impl;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.ruoyi.common.storage.StorageException;
import com.ruoyi.common.storage.StorageProperties;
import com.ruoyi.common.storage.StorageType;

/**
 * 阿里云 OSS 存储实现单测，OSS Client 用 Mock 替代，不打真实网络。
 */
@ExtendWith(MockitoExtension.class)
class AliyunOssStorageServiceTest
{
    @Mock
    private OSS ossClient;

    private StorageProperties.Aliyun config;

    private AliyunOssStorageService service;

    @BeforeEach
    void setUp()
    {
        config = new StorageProperties.Aliyun();
        config.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        config.setAccessKeyId("FAKE_AK");
        config.setAccessKeySecret("FAKE_SK");
        config.setBucketName("trading-test");
        service = new AliyunOssStorageService(ossClient, config);
    }

    @Test
    void getType_returnsAliyunOss()
    {
        assertEquals(StorageType.ALIYUN_OSS, service.getType());
    }

    @Test
    void uploadImage_putsObjectAndReturnsHttpsUrl() throws IOException
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "head.png", "image/png", new byte[] { 1, 2, 3 });
        when(ossClient.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());

        String url = service.uploadImage(file, "avatar");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(ossClient, times(1)).putObject(captor.capture());
        PutObjectRequest request = captor.getValue();
        assertEquals("trading-test", request.getBucketName());
        assertTrue(request.getKey().startsWith("avatar/"), "objectKey 应以 prefix/ 开头");
        assertTrue(request.getKey().endsWith(".png"), "objectKey 应保留扩展名");
        assertTrue(url.startsWith("https://trading-test.oss-cn-hangzhou.aliyuncs.com/avatar/"),
                "URL 应使用 https://bucket.endpoint/ 前缀，实际: " + url);
    }

    @Test
    void uploadImage_usesUrlPrefixWhenConfigured() throws IOException
    {
        config.setUrlPrefix("https://cdn.example.com/");
        service = new AliyunOssStorageService(ossClient, config);

        MockMultipartFile file = new MockMultipartFile(
                "file", "x.jpg", "image/jpeg", new byte[] { 9 });
        when(ossClient.putObject(any(PutObjectRequest.class))).thenReturn(new PutObjectResult());

        String url = service.uploadImage(file, "goods");

        assertTrue(url.startsWith("https://cdn.example.com/goods/"),
                "应使用自定义 urlPrefix 且去掉末尾斜杠，实际: " + url);
    }

    @Test
    void uploadImage_rejectsNonImageExtension()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "evil.exe", "application/octet-stream", new byte[] { 1 });

        StorageException ex = assertThrows(StorageException.class,
                () -> service.uploadImage(file, "avatar"));
        assertTrue(ex.getMessage().contains("不允许的文件类型"));
        verify(ossClient, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void upload_rejectsEmptyFile()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[0]);
        StorageException ex = assertThrows(StorageException.class,
                () -> service.upload(file, "goods"));
        assertTrue(ex.getMessage().contains("不能为空"));
        verify(ossClient, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    void upload_wrapsOssExceptionAsStorageException() throws IOException
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", new byte[] { 1, 2 });
        when(ossClient.putObject(any(PutObjectRequest.class)))
                .thenThrow(new OSSException("denied"));

        StorageException ex = assertThrows(StorageException.class,
                () -> service.upload(file, "upload"));
        assertTrue(ex.getMessage().contains("阿里云 OSS"));
    }

    @Test
    void delete_extractsObjectKeyAndCallsSdk()
    {
        String url = "https://trading-test.oss-cn-hangzhou.aliyuncs.com/avatar/20260519/abc.png";
        assertTrue(service.delete(url));
        verify(ossClient, times(1)).deleteObject("trading-test", "avatar/20260519/abc.png");
    }

    @Test
    void delete_skipsForeignUrl()
    {
        assertFalse(service.delete("https://other.cdn.com/x.png"));
        verify(ossClient, never()).deleteObject(anyString(), anyString());
    }

    @Test
    void delete_returnsFalseOnNullOrEmpty()
    {
        assertFalse(service.delete(null));
        assertFalse(service.delete(""));
    }
}
