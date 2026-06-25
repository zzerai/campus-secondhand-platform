package com.ruoyi.web.controller.app;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.AppAuthService;
import com.ruoyi.trade.domain.dto.AppReportSubmitDto;
import com.ruoyi.trade.domain.vo.AppReportVo;
import com.ruoyi.trade.service.IAppReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端举报控制器。
 *
 * <p>仅负责 token 解析与 DTO 字段非空校验，业务逻辑（商品存在 / 重复提交 / 学号回填）下沉到
 * {@link IAppReportService}。</p>
 */
@Tag(name = "移动端举报", description = "举报商品接口")
@RestController
@RequestMapping("/app/report")
public class AppReportController extends BaseController
{
    @Autowired
    private IAppReportService appReportService;

    @Autowired
    private AppAuthService appAuthService;

    @Log(title = "举报", businessType = BusinessType.INSERT)
    @Operation(summary = "提交举报", description = "对违规商品提交举报（选择类型 + 填写描述）")
    @PostMapping("/submit")
    public AjaxResult submitReport(HttpServletRequest request, @RequestBody AppReportSubmitDto submitDto)
    {
        Long userId = appAuthService.getUserIdFromRequest(request);
        if (userId == null)
        {
            return error("请先登录");
        }
        if (submitDto == null || submitDto.getGoodsId() == null || submitDto.getGoodsId() == 0)
        {
            return error("商品ID不能为空");
        }
        if (StringUtils.isEmpty(submitDto.getReportType()))
        {
            return error("举报类型不能为空");
        }
        if (StringUtils.isEmpty(submitDto.getReportContent()))
        {
            return error("举报描述不能为空");
        }

        Long reportId = appReportService.submitReport(userId, submitDto);
        AjaxResult ajax = AjaxResult.success("举报提交成功，我们会尽快处理");
        ajax.put("reportId", reportId);
        return ajax;
    }

    @Operation(summary = "我的举报记录", description = "查询当前登录用户的举报记录列表")
    @GetMapping("/myList")
    public AjaxResult myList(HttpServletRequest request)
    {
        Long userId = appAuthService.getUserIdFromRequest(request);
        if (userId == null)
        {
            return error("请先登录");
        }
        List<AppReportVo> list = appReportService.getMyReports(userId);
        return success(list);
    }
}
