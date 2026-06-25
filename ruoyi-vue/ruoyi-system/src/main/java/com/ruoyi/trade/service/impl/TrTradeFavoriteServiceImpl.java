package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.trade.domain.TrTradeFavorite;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.mapper.TrTradeFavoriteMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.ITrTradeFavoriteService;

/**
 * 商品收藏Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeFavoriteServiceImpl implements ITrTradeFavoriteService
{
    /** 商品状态：已上架 */
    private static final String GOODS_ON_SALE = "1";

    @Autowired
    private TrTradeFavoriteMapper trTradeFavoriteMapper;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    /**
     * 查询商品收藏
     *
     * @param favoriteId 商品收藏主键
     * @return 商品收藏
     */
    @Override
    public TrTradeFavorite selectTrTradeFavoriteByFavoriteId(Long favoriteId)
    {
        return trTradeFavoriteMapper.selectTrTradeFavoriteByFavoriteId(favoriteId);
    }

    /**
     * 查询商品收藏列表
     *
     * @param trTradeFavorite 商品收藏
     * @return 商品收藏
     */
    @Override
    public List<TrTradeFavorite> selectTrTradeFavoriteList(TrTradeFavorite trTradeFavorite)
    {
        return trTradeFavoriteMapper.selectTrTradeFavoriteList(trTradeFavorite);
    }

    /**
     * 新增商品收藏（管理端入口）
     *
     * 走 {@link #addFavorite(Long, Long, String)} 业务方法以复用商品校验与 ON DUPLICATE KEY UPDATE，
     * 避免管理端代录时撞 uk_user_goods 唯一键。
     */
    @Override
    public int insertTrTradeFavorite(TrTradeFavorite trTradeFavorite)
    {
        return addFavorite(trTradeFavorite.getUserId(), trTradeFavorite.getGoodsId(),
                SecurityUtils.getUsername());
    }

    @Override
    @Transactional
    public int addFavorite(Long userId, Long goodsId, String createBy)
    {
        if (userId == null)
        {
            throw new ServiceException("收藏失败，用户ID不能为空");
        }
        if (goodsId == null)
        {
            throw new ServiceException("收藏失败，商品ID不能为空");
        }
        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (goods == null)
        {
            throw new ServiceException("收藏失败，商品不存在或已删除");
        }
        if (userId.equals(goods.getSellerId()))
        {
            throw new ServiceException("不能收藏自己发布的商品");
        }
        if (!GOODS_ON_SALE.equals(goods.getGoodsStatus()))
        {
            throw new ServiceException("只能收藏已上架的商品");
        }
        // 先判定原始状态，再 upsert：仅在"未收藏 → 已收藏"切换时累加 favorite_count，
        // 兼容 MySQL ON DUPLICATE KEY UPDATE 在新旧值一致时返回 0 行的边界
        boolean alreadyActive = trTradeFavoriteMapper.existsFavorite(userId, goodsId) > 0;
        trTradeFavoriteMapper.upsertFavorite(userId, goodsId, createBy);
        if (!alreadyActive)
        {
            trTradeGoodsMapper.increaseFavoriteCount(goodsId);
            return 1;
        }
        return 0;
    }

    @Override
    @Transactional
    public int removeFavorite(Long userId, Long goodsId, String updateBy)
    {
        if (userId == null)
        {
            throw new ServiceException("取消收藏失败，用户ID不能为空");
        }
        if (goodsId == null)
        {
            throw new ServiceException("取消收藏失败，商品ID不能为空");
        }
        int rows = trTradeFavoriteMapper.softDeleteByUserAndGoods(userId, goodsId, updateBy);
        if (rows > 0)
        {
            trTradeGoodsMapper.decreaseFavoriteCount(goodsId);
        }
        return rows;
    }

    @Override
    public boolean existsFavorite(Long userId, Long goodsId)
    {
        if (userId == null || goodsId == null)
        {
            return false;
        }
        return trTradeFavoriteMapper.existsFavorite(userId, goodsId) > 0;
    }

    @Override
    public List<TrTradeGoods> selectMyFavoriteGoods(Long userId)
    {
        if (userId == null)
        {
            throw new ServiceException("用户ID不能为空");
        }
        return trTradeFavoriteMapper.selectMyFavoriteGoods(userId);
    }

    /**
     * 修改商品收藏
     *
     * @param trTradeFavorite 商品收藏
     * @return 结果
     */
    @Override
    public int updateTrTradeFavorite(TrTradeFavorite trTradeFavorite)
    {
        trTradeFavorite.setUpdateBy(SecurityUtils.getUsername());
        trTradeFavorite.setUpdateTime(DateUtils.getNowDate());
        return trTradeFavoriteMapper.updateTrTradeFavorite(trTradeFavorite);
    }

    /**
     * 批量删除商品收藏
     *
     * @param favoriteIds 需要删除的商品收藏主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeFavoriteByFavoriteIds(Long[] favoriteIds)
    {
        return trTradeFavoriteMapper.deleteTrTradeFavoriteByFavoriteIds(favoriteIds);
    }

    /**
     * 删除商品收藏信息
     *
     * @param favoriteId 商品收藏主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeFavoriteByFavoriteId(Long favoriteId)
    {
        return trTradeFavoriteMapper.deleteTrTradeFavoriteByFavoriteId(favoriteId);
    }
}
