package com.ruoyi.common.storage.impl;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.storage.StorageException;
import com.ruoyi.common.storage.StorageService;
import com.ruoyi.common.storage.StorageType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;

/**
 * 本地磁盘存储实现，文件落到 {@link RuoYiConfig#getProfile()} 下，
 * 通过 RuoYi 资源映射（/profile/**）对外暴露。
 *
 * @author trading
 */
public class LocalStorageService implements StorageService
{
    private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);

    @Override
    public String upload(MultipartFile file, String pathPrefix)
    {
        try
        {
            return FileUploadUtils.upload(buildBaseDir(pathPrefix), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        }
        catch (Exception e)
        {
            throw new StorageException("本地文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadImage(MultipartFile file, String pathPrefix)
    {
        try
        {
            return FileUploadUtils.upload(buildBaseDir(pathPrefix), file, MimeTypeUtils.IMAGE_EXTENSION, true);
        }
        catch (Exception e)
        {
            throw new StorageException("本地图片上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String url)
    {
        if (StringUtils.isEmpty(url))
        {
            return false;
        }
        // 外部 URL（OSS / CDN）不删除
        if (url.startsWith(Constants.HTTP) || url.startsWith(Constants.HTTPS))
        {
            log.debug("URL [{}] 不属于本地存储，跳过删除", url);
            return false;
        }
        try
        {
            String absPath = RuoYiConfig.getProfile() + FileUtils.stripPrefix(url);
            return FileUtils.deleteFile(absPath);
        }
        catch (Exception e)
        {
            log.warn("删除本地文件失败 url={}", url, e);
            return false;
        }
    }

    @Override
    public StorageType getType()
    {
        return StorageType.LOCAL;
    }

    /** 仅允许字母数字、下划线、短横线作为路径段，多层用 '/' 分隔。 */
    private static final Pattern SAFE_SEGMENT = Pattern.compile("^[A-Za-z0-9_\\-]+$");

    private String buildBaseDir(String pathPrefix)
    {
        if (StringUtils.isEmpty(pathPrefix))
        {
            return RuoYiConfig.getProfile() + "/upload";
        }
        String normalized = pathPrefix.replace('\\', '/').replaceAll("^/+", "");
        // Windows 盘符如 C:、绝对路径都不允许
        if (normalized.contains(":"))
        {
            throw new StorageException("非法的上传路径前缀：禁止绝对路径");
        }
        String[] segments = normalized.split("/");
        StringBuilder safe = new StringBuilder();
        for (String segment : segments)
        {
            if (StringUtils.isEmpty(segment))
            {
                continue;
            }
            // 任何 . / .. 段都属于目录穿越尝试
            if (".".equals(segment) || "..".equals(segment) || !SAFE_SEGMENT.matcher(segment).matches())
            {
                throw new StorageException("非法的上传路径前缀：" + pathPrefix);
            }
            if (safe.length() > 0)
            {
                safe.append('/');
            }
            safe.append(segment);
        }
        if (safe.length() == 0)
        {
            return RuoYiConfig.getProfile() + "/upload";
        }
        return RuoYiConfig.getProfile() + "/" + safe;
    }
}
