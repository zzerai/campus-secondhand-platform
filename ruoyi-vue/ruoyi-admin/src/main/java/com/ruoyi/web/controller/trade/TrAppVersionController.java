package com.ruoyi.web.controller.trade;

import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.framework.config.ServerConfig;
import com.ruoyi.trade.domain.TrAppVersion;
import com.ruoyi.trade.service.ITrAppVersionService;

/**
 * 移动端APK版本Controller
 *
 * @author trading
 * @date 2026-06-05
 */
@RestController
@RequestMapping("/trade/appVersion")
public class TrAppVersionController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(TrAppVersionController.class);

    /** 允许上传的扩展名：仅 APK */
    private static final String[] APK_EXTENSION = { "apk" };

    @Autowired
    private ITrAppVersionService trAppVersionService;

    @Autowired
    private ServerConfig serverConfig;

    /**
     * 查询移动端APK版本列表
     */
    @PreAuthorize("@ss.hasPermi('trade:version:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrAppVersion trAppVersion)
    {
        startPage();
        List<TrAppVersion> list = trAppVersionService.selectTrAppVersionList(trAppVersion);
        return getDataTable(list);
    }

    /**
     * 获取移动端APK版本详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:version:query')")
    @GetMapping(value = "/{versionId}")
    public AjaxResult getInfo(@PathVariable("versionId") Long versionId)
    {
        return success(trAppVersionService.selectTrAppVersionByVersionId(versionId));
    }

    /**
     * 上传 APK 文件，返回完整下载 URL、文件大小、SHA-256，供新增版本时填充表单。
     */
    @PreAuthorize("@ss.hasPermi('trade:version:upload')")
    @Log(title = "APK上传", businessType = BusinessType.OTHER)
    @PostMapping("/uploadApk")
    public AjaxResult uploadApk(@RequestParam("file") MultipartFile file)
    {
        if (file == null || file.isEmpty())
        {
            return AjaxResult.error("未选择文件");
        }
        try
        {
            // 上传前先算 SHA-256（StandardMultipartFile 的 InputStream 可重复打开）
            String sha256 = DigestUtils.sha256Hex(file.getInputStream());
            long size = file.getSize();
            // 仅放行 apk 扩展名，落到 profile/apk 下；返回相对路径如 /profile/apk/xxx.apk
            String relativeUrl = FileUploadUtils.upload(RuoYiConfig.getProfile() + "/apk", file, APK_EXTENSION);
            AjaxResult ajax = AjaxResult.success();
            ajax.put("url", serverConfig.getUrl() + relativeUrl);
            ajax.put("fileName", relativeUrl);
            ajax.put("fileSize", size);
            ajax.put("fileSha256", sha256);
            ajax.put("originalFilename", file.getOriginalFilename());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("APK上传失败", e);
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 新增移动端APK版本
     */
    @PreAuthorize("@ss.hasPermi('trade:version:add')")
    @Log(title = "移动端APK版本", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrAppVersion trAppVersion)
    {
        return toAjax(trAppVersionService.insertTrAppVersion(trAppVersion));
    }

    /**
     * 修改移动端APK版本
     */
    @PreAuthorize("@ss.hasPermi('trade:version:edit')")
    @Log(title = "移动端APK版本", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrAppVersion trAppVersion)
    {
        return toAjax(trAppVersionService.updateTrAppVersion(trAppVersion));
    }

    /**
     * 删除移动端APK版本
     */
    @PreAuthorize("@ss.hasPermi('trade:version:remove')")
    @Log(title = "移动端APK版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{versionIds}")
    public AjaxResult remove(@PathVariable Long[] versionIds)
    {
        return toAjax(trAppVersionService.deleteTrAppVersionByVersionIds(versionIds));
    }
}
