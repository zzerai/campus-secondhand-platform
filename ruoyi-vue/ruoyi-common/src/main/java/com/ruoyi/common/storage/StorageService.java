package com.ruoyi.common.storage;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储服务统一抽象。
 * 调用方无需关心底层是本地磁盘还是阿里云 OSS，按 {@code ruoyi.storage.type} 自动注入实现。
 *
 * @author trading
 */
public interface StorageService
{
    /**
     * 上传文件。
     *
     * @param file       Multipart 文件
     * @param pathPrefix 业务路径前缀（如 "avatar"、"goods"、"upload"），不要以 / 开头
     * @return 可直接展示的访问 URL
     */
    String upload(MultipartFile file, String pathPrefix);

    /**
     * 上传图片（限制图片扩展名）。
     *
     * @param file       Multipart 文件
     * @param pathPrefix 业务路径前缀
     * @return 可直接展示的访问 URL
     */
    String uploadImage(MultipartFile file, String pathPrefix);

    /**
     * 按访问 URL 删除文件。删除失败不抛异常，仅返回 false（用于覆盖旧头像等场景）。
     *
     * @param url upload 返回的 URL
     * @return 是否删除成功
     */
    boolean delete(String url);

    /**
     * 当前存储后端类型。
     */
    StorageType getType();
}
