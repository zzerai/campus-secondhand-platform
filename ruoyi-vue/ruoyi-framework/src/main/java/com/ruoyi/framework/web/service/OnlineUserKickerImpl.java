package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.service.IOnlineUserKicker;

/**
 * 在线用户强制下线实现（ruoyi-framework 侧，桥接 {@link TokenService}）。
 *
 * <p>接口定义在 ruoyi-system，本实现放在 framework 内，使 system 业务层可反向调用而不产生非法依赖。</p>
 *
 * @author thr
 */
@Service
public class OnlineUserKickerImpl implements IOnlineUserKicker
{
    @Autowired
    private TokenService tokenService;

    @Override
    public int kickByUserId(Long userId)
    {
        return tokenService.deleteByUserId(userId);
    }
}
