package com.ruoyi.common.storage.impl;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.storage.StorageException;
import com.ruoyi.common.storage.StorageProperties;
import com.ruoyi.common.storage.StorageService;
import com.ruoyi.common.storage.StorageType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;
import com.ruoyi.common.utils.uuid.IdUtils;

/**
 * 阿里云 OSS 存储实现。
 * <p>
 * 上传后的 objectKey 形如 {@code <pathPrefix>/<yyyyMMdd>/<uuid>.<ext>}，
 * 返回的访问 URL 优先使用 {@link StorageProperties.Aliyun#getUrlPrefix()}，
 * 否则使用默认 {@code https://<bucket>.<endpoint>/<objectKey>}。
 *
 * @author trading
 */
public class AliyunOssStorageService implements StorageService
{
    private static final Logger log = LoggerFactory.getLogger(AliyunOssStorageService.class);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 单文件上传上限，与 FileUploadUtils 保持一致 50MB */
    private static final long MAX_SIZE = 50L * 1024 * 1024;

    private final OSS ossClient;

    private final StorageProperties.Aliyun config;

    private final String resolvedUrlPrefix;

    public AliyunOssStorageService(OSS ossClient, StorageProperties.Aliyun config)
    {
        this.ossClient = ossClient;
        this.config = config;
        this.resolvedUrlPrefix = resolveUrlPrefix(config);
    }

    @Override
    public String upload(MultipartFile file, String pathPrefix)
    {
        return doUpload(file, pathPrefix, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    }

    @Override
    public String uploadImage(MultipartFile file, String pathPrefix)
    {
        return doUpload(file, pathPrefix, MimeTypeUtils.IMAGE_EXTENSION);
    }

    private String doUpload(MultipartFile file, String pathPrefix, String[] allowedExtensions)
    {
        if (file == null || file.isEmpty())
        {
            throw new StorageException("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE)
        {
            throw new StorageException("文件大小超过 " + (MAX_SIZE / 1024 / 1024) + "MB 限制");
        }
        String extension = getExtension(file);
        if (!isAllowed(extension, allowedExtensions))
        {
            throw new StorageException("不允许的文件类型: ." + extension);
        }

        String objectKey = buildObjectKey(pathPrefix, extension);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (StringUtils.isNotEmpty(file.getContentType()))
        {
            metadata.setContentType(file.getContentType());
        }

        try (InputStream in = file.getInputStream())
        {
            PutObjectRequest request = new PutObjectRequest(config.getBucketName(), objectKey, in, metadata);
            ossClient.putObject(request);
        }
        catch (OSSException | IOException e)
        {
            throw new StorageException("上传到阿里云 OSS 失败: " + e.getMessage(), e);
        }

        return resolvedUrlPrefix + "/" + objectKey;
    }

    @Override
    public boolean delete(String url)
    {
        if (StringUtils.isEmpty(url))
        {
            return false;
        }
        // 仅处理属于本 bucket 的 URL
        if (!url.startsWith(resolvedUrlPrefix + "/"))
        {
            log.debug("URL [{}] 不属于 OSS bucket [{}]，跳过删除", url, config.getBucketName());
            return false;
        }
        String objectKey = url.substring(resolvedUrlPrefix.length() + 1);
        try
        {
            ossClient.deleteObject(config.getBucketName(), objectKey);
            return true;
        }
        catch (OSSException e)
        {
            log.warn("删除 OSS 对象失败 key={}", objectKey, e);
            return false;
        }
    }

    @Override
    public StorageType getType()
    {
        return StorageType.ALIYUN_OSS;
    }

    private String buildObjectKey(String pathPrefix, String extension)
    {
        String prefix = StringUtils.isEmpty(pathPrefix) ? "upload" : pathPrefix.replace('\\', '/').replaceAll("^/+|/+$", "");
        String date = LocalDate.now().format(DATE_FMT);
        return prefix + "/" + date + "/" + IdUtils.fastSimpleUUID() + "." + extension;
    }

    private static String getExtension(MultipartFile file)
    {
        String ext = FilenameUtils.getExtension(Objects.requireNonNull(file.getOriginalFilename()));
        if (StringUtils.isEmpty(ext) && StringUtils.isNotEmpty(file.getContentType()))
        {
            ext = MimeTypeUtils.getExtension(file.getContentType());
        }
        return ext == null ? "" : ext.toLowerCase();
    }

    private static boolean isAllowed(String extension, String[] allowed)
    {
        if (allowed == null || allowed.length == 0)
        {
            return true;
        }
        for (String a : allowed)
        {
            if (a.equalsIgnoreCase(extension))
            {
                return true;
            }
        }
        return false;
    }

    private static String resolveUrlPrefix(StorageProperties.Aliyun config)
    {
        String custom = config.getUrlPrefix();
        if (StringUtils.isNotEmpty(custom))
        {
            return custom.endsWith("/") ? custom.substring(0, custom.length() - 1) : custom;
        }
        String endpoint = Objects.requireNonNull(config.getEndpoint(), "OSS endpoint 未配置")
                .replaceFirst("^https?://", "");
        return Constants.HTTPS + config.getBucketName() + "." + endpoint;
    }
}
