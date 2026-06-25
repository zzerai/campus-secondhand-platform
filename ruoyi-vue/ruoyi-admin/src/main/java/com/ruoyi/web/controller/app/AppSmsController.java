package com.ruoyi.web.controller.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.AppAuthService;
import com.ruoyi.trade.domain.dto.AppSmsSendDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端短信验证码下发接口。
 *
 * <p>对接阿里云号码认证服务（dypnsapi），验证码生成/存储/限频由阿里云托管。</p>
 *
 * @author trading
 */
@Tag(name = "移动端短信验证码", description = "注册 / 登录场景的短信验证码下发")
@RestController
@RequestMapping("/app/sms")
public class AppSmsController extends BaseController
{
    @Autowired
    private AppAuthService appAuthService;

    /**
     * 发送短信验证码
     */
    @Operation(summary = "发送短信验证码",
            description = "scene=register 要求手机号未注册；scene=login 要求手机号已注册。"
                    + "同一手机号 60s 内只能发送一次（由阿里云限频）")
    @PostMapping("/send")
    public AjaxResult send(@RequestBody AppSmsSendDto body)
    {
        if (body == null)
        {
            return error("请求体不能为空");
        }
        String msg = appAuthService.sendSmsCode(body.getPhone(), body.getScene());
        if (StringUtils.isEmpty(msg))
        {
            return success("验证码已发送");
        }
        return error(msg);
    }
}
