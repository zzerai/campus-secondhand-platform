package com.ruoyi.web.controller.app;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.ruoyi.common.core.controller.BaseController;

/**
 * Base controller for mobile APIs.
 * Swagger may send empty query values for {@code params}; block binding to avoid type errors.
 */
public abstract class AppApiController extends BaseController
{
    @InitBinder
    public void appQueryBinder(WebDataBinder binder)
    {
        binder.setDisallowedFields("params");
    }

    /**
     * 获取当前登录用户ID；匿名访问（如 {@code @Anonymous} 接口）时返回 {@code null} 而不抛异常。
     */
    protected Long currentUserIdOrNull()
    {
        try
        {
            return getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
