package com.ruoyi.trade.service;

import com.ruoyi.trade.domain.vo.AppUserHomepageVo;

/**
 * 移动端用户公开信息业务接口。
 */
public interface IAppUserService
{
    /**
     * 查询用户公开主页（脱敏聚合：基本信息 + 在售/已售数 + 评价均分/好评率）。
     *
     * @param userId 目标用户ID
     * @return 公开主页 VO；用户不存在或已删除时返回 {@code null}
     */
    public AppUserHomepageVo getUserHomepage(Long userId);
}
