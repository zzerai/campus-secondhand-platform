package com.ruoyi.common.storage.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.storage.StorageType;

/**
 * 本地磁盘存储实现单测，使用 JUnit5 临时目录避免污染真实 profile 路径。
 */
class LocalStorageServiceTest
{
    @TempDir
    Path tempProfile;

    private String originalProfile;

    private LocalStorageService service;

    @BeforeEach
    void setUp()
    {
        originalProfile = RuoYiConfig.getProfile();
        new RuoYiConfig().setProfile(tempProfile.toString());
        service = new LocalStorageService();
    }

    @AfterEach
    void restore()
    {
        new RuoYiConfig().setProfile(originalProfile);
    }

    @Test
    void getType_returnsLocal()
    {
        org.junit.jupiter.api.Assertions.assertEquals(StorageType.LOCAL, service.getType());
    }

    @Test
    void uploadImage_writesFileToProfileSubdirectoryAndReturnsResourcePath()
    {
        MockMultipartFile file = new MockMultipartFile(
                "file", "head.png", "image/png", new byte[] { 1, 2, 3, 4 });

        String stored = service.uploadImage(file, "avatar");

        assertNotNull(stored);
        assertTrue(stored.startsWith("/profile/avatar/"),
                "本地存储 URL 应以 /profile/<prefix>/ 开头，实际: " + stored);
        // 验证文件确实落盘
        String relativeAfterProfile = stored.substring("/profile/".length());
        File expected = new File(tempProfile.toFile(), relativeAfterProfile);
        assertTrue(expected.exists(), "上传文件应实际写入磁盘: " + expected);
    }

    @Test
    void delete_removesFileByResourceUrl() throws Exception
    {
        // 准备一个落到 tempProfile 下的文件
        Path target = tempProfile.resolve("avatar/20260519/test.png");
        Files.createDirectories(target.getParent());
        Files.write(target, new byte[] { 7, 8, 9 });

        boolean ok = service.delete("/profile/avatar/20260519/test.png");

        assertTrue(ok);
        assertFalse(target.toFile().exists(), "delete 后文件应已删除");
    }

    @Test
    void delete_skipsExternalHttpsUrl()
    {
        assertFalse(service.delete("https://cdn.example.com/foo.png"),
                "外部 URL 不应被本地存储处理");
    }

    @Test
    void delete_returnsFalseForMissingFile()
    {
        assertFalse(service.delete("/profile/avatar/not-exist.png"));
    }
}
