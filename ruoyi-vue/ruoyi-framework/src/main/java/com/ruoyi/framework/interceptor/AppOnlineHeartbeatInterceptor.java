package com.ruoyi.framework.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.service.IOnlinePresenceService;

/**
 * 移动端在线心跳拦截器。
 *
 * <p>对带登录态的 {@code /app/**} 请求刷新在线心跳键。匿名请求（未携带有效 Token）取不到
 * userId 时静默跳过，绝不影响主流程。</p>
 *
 * @author ruoyi
 */
@Component
public class AppOnlineHeartbeatInterceptor implements HandlerInterceptor
{
    @Autowired
    private IOnlinePresenceService onlinePresenceService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
    {
        Long userId = currentUserIdOrNull();
        if (userId != null)
        {
            onlinePresenceService.touch(userId);
        }
        return true;
    }

    private Long currentUserIdOrNull()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
