package com.ruoyi.web.controller.app;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.model.AppLoginBody;
import com.ruoyi.common.core.domain.model.AppTokenInfo;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.web.service.AppAuthService;
import com.ruoyi.trade.domain.dto.AppRegisterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端认证
 *
 * @author ruoyi
 */
@Tag(name = "移动端认证", description = "学生用户登录、注册、退出接口")
@RestController
@RequestMapping("/app")
public class AppAuthController extends BaseController
{
    @Autowired
    private AppAuthService appAuthService;

    /**
     * 学生用户登录
     */
    @Operation(summary = "学生登录", description = "通过学号/手机号+密码登录，返回token和学生信息")
    @PostMapping("/login")
    public AjaxResult login(@RequestBody AppLoginBody loginBody)
    {
        try
        {
            AppTokenInfo appTokenInfo = appAuthService.login(loginBody);
            if (appTokenInfo == null)
            {
                return error("登录失败，请检查账号和密码");
            }
            AjaxResult ajax = success();
            ajax.put("token", appTokenInfo.getToken());
            AppTokenInfo.AppStudentUser studentUser = appTokenInfo.getStudentUser();
            if (studentUser != null)
            {
                ajax.put("studentUser", studentUser);
            }
            return ajax;
        }
        catch (Exception e)
        {
            return error(e.getMessage());
        }
    }

    /**
     * 学生用户注册
     */
    @Operation(summary = "学生注册", description = "学生用户注册，需要提供学号、手机号、密码等信息")
    @PostMapping("/register")
    public AjaxResult register(@RequestBody AppRegisterDto registerDto)
    {
        String msg = appAuthService.register(registerDto);
        if (StringUtils.isEmpty(msg))
        {
            return success("注册成功");
        }
        return error(msg);
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录", description = "清除当前用户的token，需携带 Authorization 头")
    @PostMapping("/logout")
    public AjaxResult logout(HttpServletRequest request)
    {
        String token = resolveToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            appAuthService.logout(token);
        }
        return success("退出成功");
    }

    private String resolveToken(HttpServletRequest request)
    {
        String token = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX))
        {
            token = token.replaceFirst(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }
}
