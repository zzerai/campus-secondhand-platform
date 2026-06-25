package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.vo.AppUserHomepageVo;
import com.ruoyi.trade.mapper.TrTradeEvaluationMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.IAppUserService;
import com.ruoyi.trade.service.ITrStudentUserService;

/**
 * 移动端用户公开信息业务实现。
 *
 * <p>商品状态枚举（参见 CLAUDE.md / 01_ddl.sql）：1-已上架 4-已售出。</p>
 */
@Service
public class AppUserServiceImpl implements IAppUserService
{
    /** 已上架 */
    private static final String GOODS_ON_SALE = "1";
    /** 已售出 */
    private static final String GOODS_SOLD = "4";

    @Autowired
    private ITrStudentUserService studentUserService;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrTradeEvaluationMapper trTradeEvaluationMapper;

    @Override
    public AppUserHomepageVo getUserHomepage(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        TrStudentUser user = studentUserService.selectTrStudentUserByUserId(userId);
        if (user == null)
        {
            return null;
        }

        AppUserHomepageVo vo = new AppUserHomepageVo();
        vo.setUserId(user.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setCreditScore(user.getCreditScore());
        vo.setCreateTime(user.getCreateTime());

        vo.setOnSaleCount(trTradeGoodsMapper.countSellerGoodsByStatus(userId, GOODS_ON_SALE));
        vo.setSoldCount(trTradeGoodsMapper.countSellerGoodsByStatus(userId, GOODS_SOLD));

        // 评价均分
        BigDecimal avg = trTradeEvaluationMapper.avgScoreByUserId(userId);
        vo.setAverageScore(avg != null ? avg.doubleValue() : 0.0);

        // 好评率（4~5 星占比）与收到总数
        List<Map<String, Object>> dist = trTradeEvaluationMapper.scoreDistributionByUserId(userId);
        int total = 0;
        int good = 0;
        if (dist != null)
        {
            for (Map<String, Object> row : dist)
            {
                Object scoreObj = row.get("score");
                Object cntObj = row.get("cnt");
                if (scoreObj == null || cntObj == null)
                {
                    continue;
                }
                int star = ((Number) scoreObj).intValue();
                int cnt = ((Number) cntObj).intValue();
                total += cnt;
                if (star >= 4)
                {
                    good += cnt;
                }
            }
        }
        vo.setTotalReceived(total);
        vo.setGoodRate(total > 0 ? Math.round(good * 100f / total) : 0);

        return vo;
    }
}
