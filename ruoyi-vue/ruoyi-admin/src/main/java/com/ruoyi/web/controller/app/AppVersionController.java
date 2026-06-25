package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.trade.domain.TrAppVersion;
import com.ruoyi.trade.service.ITrAppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端版本检测控制器。
 * <p>
 * 仅安卓 APK 侧载更新：返回当前启用的最新版本，由客户端用本地 versionCode 与之比对决定是否更新。
 * 该接口匿名放行（用户未登录也能检测）。
 *
 * @author trading
 */
@Tag(name = "移动端版本检测", description = "应用内更新检测（仅安卓）")
@RestController
@RequestMapping("/app/version")
public class AppVersionController extends BaseController
{
    @Autowired
    private ITrAppVersionService trAppVersionService;

    /**
     * 获取最新启用版本。data 为 null 表示后台未配置任何启用版本。
     */
    @Operation(summary = "获取最新版本", description = "返回当前启用的最新版本信息，客户端据此判断是否提示更新")
    @GetMapping("/latest")
    public AjaxResult latest()
    {
        TrAppVersion latest = trAppVersionService.selectLatestEnabledVersion();
        return success(latest);
    }
}
