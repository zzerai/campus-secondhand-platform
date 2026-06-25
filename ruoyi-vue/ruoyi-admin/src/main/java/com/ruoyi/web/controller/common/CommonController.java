package com.ruoyi.web.controller.common;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.storage.StorageService;
import com.ruoyi.common.storage.StorageType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUtils;
import com.ruoyi.framework.config.ServerConfig;

/**
 * 通用请求处理
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/common")
public class CommonController
{
    private static final Logger log = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private ServerConfig serverConfig;

    @Autowired
    private StorageService storageService;

    private static final String FILE_DELIMITER = ",";

    /** 通用上传业务路径前缀 */
    private static final String UPLOAD_PREFIX = "upload";

    /**
     * 通用下载请求
     * 
     * @param fileName 文件名称
     * @param delete 是否删除
     */
    @GetMapping("/download")
    public void fileDownload(String fileName, Boolean delete, HttpServletResponse response, HttpServletRequest request)
    {
        try
        {
            if (!FileUtils.checkAllowDownload(fileName))
            {
                throw new Exception(StringUtils.format("文件名称({})非法，不允许下载。 ", fileName));
            }
            String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
            String filePath = RuoYiConfig.getDownloadPath() + fileName;

            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, realFileName);
            FileUtils.writeBytes(filePath, response.getOutputStream());
            if (delete)
            {
                FileUtils.deleteFile(filePath);
            }
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }

    /**
     * 通用上传请求（单个）。底层按 {@code ruoyi.storage.type} 走本地或 OSS。
     */
    @PostMapping("/upload")
    public AjaxResult uploadFile(MultipartFile file) throws Exception
    {
        try
        {
            String storedUrl = storageService.upload(file, UPLOAD_PREFIX);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", toAbsoluteUrl(storedUrl));
            ajax.put("fileName", storedUrl);
            ajax.put("newFileName", FileUtils.getName(storedUrl));
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("通用上传失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 通用上传请求（多个）。底层按 {@code ruoyi.storage.type} 走本地或 OSS。
     */
    @PostMapping("/uploads")
    public AjaxResult uploadFiles(List<MultipartFile> files) throws Exception
    {
        try
        {
            List<String> urls = new ArrayList<String>();
            List<String> fileNames = new ArrayList<String>();
            List<String> newFileNames = new ArrayList<String>();
            List<String> originalFilenames = new ArrayList<String>();
            for (MultipartFile file : files)
            {
                String storedUrl = storageService.upload(file, UPLOAD_PREFIX);
                urls.add(toAbsoluteUrl(storedUrl));
                fileNames.add(storedUrl);
                newFileNames.add(FileUtils.getName(storedUrl));
                originalFilenames.add(file.getOriginalFilename());
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", StringUtils.join(urls, FILE_DELIMITER));
            ajax.put("fileNames", StringUtils.join(fileNames, FILE_DELIMITER));
            ajax.put("newFileNames", StringUtils.join(newFileNames, FILE_DELIMITER));
            ajax.put("originalFilenames", StringUtils.join(originalFilenames, FILE_DELIMITER));
            return ajax;
        }
        catch (Exception e)
        {
            log.error("通用批量上传失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 把存储层返回的 URL 适配为前端可直接访问的绝对 URL：
     * 本地模式返回 /profile/... 的相对路径，需要拼接 serverConfig；OSS 已经是绝对 URL，直接返回。
     */
    private String toAbsoluteUrl(String storedUrl)
    {
        if (storageService.getType() == StorageType.LOCAL)
        {
            return serverConfig.getUrl() + storedUrl;
        }
        return storedUrl;
    }

    /**
     * 本地资源通用下载
     */
    @GetMapping("/download/resource")
    public void resourceDownload(String resource, HttpServletRequest request, HttpServletResponse response)
            throws Exception
    {
        try
        {
            if (!FileUtils.checkAllowDownload(resource))
            {
                throw new Exception(StringUtils.format("资源文件({})非法，不允许下载。 ", resource));
            }
            // 本地资源路径
            String localPath = RuoYiConfig.getProfile();
            // 数据库资源地址
            String downloadPath = localPath + FileUtils.stripPrefix(resource);
            // 下载名称
            String downloadName = StringUtils.substringAfterLast(downloadPath, "/");
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            FileUtils.setAttachmentResponseHeader(response, downloadName);
            FileUtils.writeBytes(downloadPath, response.getOutputStream());
        }
        catch (Exception e)
        {
            log.error("下载文件失败", e);
        }
    }
}
