package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.storage.StorageService;
import com.ruoyi.common.storage.StorageType;
import com.ruoyi.framework.config.ServerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端文件/图片上传接口。
 * <p>
 * 与管理端 {@code /common/upload} 等价，仅作为路由分组，方便移动端区分 baseUrl 调用、Swagger 文档分组。
 */
@RestController
@RequestMapping("/app/upload")
@Tag(name = "移动端文件上传接口")
public class AppUploadController extends AppApiController
{
    private static final Logger log = LoggerFactory.getLogger(AppUploadController.class);

    @Autowired
    private StorageService storageService;

    @Autowired
    private ServerConfig serverConfig;

    /**
     * 上传单张图片（限制为 jpg/png/gif/bmp/jpeg）。
     */
    @Operation(summary = "上传单张图片", description = "登录用户调用，常用于商品发布/争议凭证。返回完整 URL。")
    @PostMapping("/image")
    public AjaxResult uploadImage(
            @Parameter(description = "图片文件，表单字段名 file") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务路径前缀，例如 goods/dispute，默认 goods") @RequestParam(value = "prefix", defaultValue = "goods") String prefix)
    {
        try
        {
            String storedUrl = storageService.uploadImage(file, prefix);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", toAbsoluteUrl(storedUrl));
            ajax.put("fileName", storedUrl);
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("移动端上传图片失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 批量上传图片，最多 9 张。
     */
    @Operation(summary = "批量上传图片", description = "商品发布最多上传 9 张图片，返回 url 列表（逗号分隔）。")
    @PostMapping("/images")
    public AjaxResult uploadImages(
            @Parameter(description = "图片文件列表，表单字段名 files") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "业务路径前缀，默认 goods") @RequestParam(value = "prefix", defaultValue = "goods") String prefix)
    {
        if (files == null || files.isEmpty())
        {
            return AjaxResult.error("未选择文件");
        }
        if (files.size() > 9)
        {
            return AjaxResult.error("一次最多上传 9 张图片");
        }
        try
        {
            List<String> urls = new ArrayList<>();
            List<String> fileNames = new ArrayList<>();
            for (MultipartFile file : files)
            {
                String storedUrl = storageService.uploadImage(file, prefix);
                urls.add(toAbsoluteUrl(storedUrl));
                fileNames.add(storedUrl);
            }
            AjaxResult ajax = AjaxResult.success();
            ajax.put("urls", String.join(",", urls));
            ajax.put("fileNames", String.join(",", fileNames));
            return ajax;
        }
        catch (Exception e)
        {
            log.error("移动端批量上传图片失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    private String toAbsoluteUrl(String storedUrl)
    {
        if (storageService.getType() == StorageType.LOCAL)
        {
            return serverConfig.getUrl() + storedUrl;
        }
        return storedUrl;
    }
}
