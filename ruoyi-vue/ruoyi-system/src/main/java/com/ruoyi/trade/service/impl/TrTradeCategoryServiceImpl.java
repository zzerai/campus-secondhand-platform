package com.ruoyi.trade.service.impl;

import java.util.List;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.mapper.TrTradeCategoryMapper;
import com.ruoyi.trade.domain.TrTradeCategory;
import com.ruoyi.trade.service.ITrTradeCategoryService;

/**
 * 商品分类Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-11
 */
@Service
public class TrTradeCategoryServiceImpl implements ITrTradeCategoryService
{
    /** 启用分类列表缓存键（不分页，供下拉/级联使用） */
    private static final String ACTIVE_CATEGORY_CACHE_KEY = CacheConstants.TRADE_CATEGORY_KEY + "active";

    @Autowired
    private TrTradeCategoryMapper trTradeCategoryMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public TrTradeCategory selectTrTradeCategoryByCategoryId(Long categoryId)
    {
        return trTradeCategoryMapper.selectTrTradeCategoryByCategoryId(categoryId);
    }

    @Override
    public List<TrTradeCategory> selectTrTradeCategoryList(TrTradeCategory trTradeCategory)
    {
        return trTradeCategoryMapper.selectTrTradeCategoryList(trTradeCategory);
    }

    @Override
    public int insertTrTradeCategory(TrTradeCategory trTradeCategory)
    {
        if (trTradeCategory.getParentId() == null)
        {
            trTradeCategory.setParentId(0L);
        }
        if (!checkCategoryNameUnique(trTradeCategory))
        {
            throw new ServiceException("新增分类'" + trTradeCategory.getCategoryName() + "'失败，同级下名称已存在");
        }
        String username = SecurityUtils.getUsername();
        trTradeCategory.setCreateBy(username);
        trTradeCategory.setCreateTime(DateUtils.getNowDate());
        int rows = trTradeCategoryMapper.insertTrTradeCategory(trTradeCategory);
        evictActiveCategoryCache();
        return rows;
    }

    @Override
    public int updateTrTradeCategory(TrTradeCategory trTradeCategory)
    {
        if (trTradeCategory.getCategoryId() == null)
        {
            throw new ServiceException("修改分类失败，分类ID不能为空");
        }
        // 不允许父级指向自身
        if (trTradeCategory.getParentId() != null
                && trTradeCategory.getParentId().longValue() == trTradeCategory.getCategoryId().longValue())
        {
            throw new ServiceException("修改分类'" + trTradeCategory.getCategoryName() + "'失败，父级不能为自身");
        }
        // 已有子分类的不允许再降级为他人的子节点（保持简单的 2 层结构）
        if (trTradeCategory.getParentId() != null && trTradeCategory.getParentId() != 0L
                && trTradeCategoryMapper.countChildrenByCategoryId(trTradeCategory.getCategoryId()) > 0)
        {
            throw new ServiceException("修改分类'" + trTradeCategory.getCategoryName() + "'失败，存在子分类时不能改为下级分类");
        }
        if (StringUtils.isNotEmpty(trTradeCategory.getCategoryName()) && !checkCategoryNameUnique(trTradeCategory))
        {
            throw new ServiceException("修改分类'" + trTradeCategory.getCategoryName() + "'失败，同级下名称已存在");
        }
        trTradeCategory.setUpdateBy(SecurityUtils.getUsername());
        trTradeCategory.setUpdateTime(DateUtils.getNowDate());
        int rows = trTradeCategoryMapper.updateTrTradeCategory(trTradeCategory);
        evictActiveCategoryCache();
        return rows;
    }

    @Override
    public int deleteTrTradeCategoryByCategoryIds(Long[] categoryIds)
    {
        for (Long id : categoryIds)
        {
            ensureDeletable(id);
        }
        int rows = trTradeCategoryMapper.deleteTrTradeCategoryByCategoryIds(categoryIds);
        evictActiveCategoryCache();
        return rows;
    }

    @Override
    public int deleteTrTradeCategoryByCategoryId(Long categoryId)
    {
        ensureDeletable(categoryId);
        int rows = trTradeCategoryMapper.deleteTrTradeCategoryByCategoryId(categoryId);
        evictActiveCategoryCache();
        return rows;
    }

    @Override
    public int updateCategoryStatus(TrTradeCategory category)
    {
        TrTradeCategory update = new TrTradeCategory();
        update.setCategoryId(category.getCategoryId());
        update.setStatus(category.getStatus());
        update.setUpdateBy(StringUtils.isNotEmpty(category.getUpdateBy()) ? category.getUpdateBy() : SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = trTradeCategoryMapper.updateTrTradeCategory(update);
        evictActiveCategoryCache();
        return rows;
    }

    @Override
    public List<TrTradeCategory> selectAllActiveCategories()
    {
        JSONArray cached = redisCache.getCacheObject(ACTIVE_CATEGORY_CACHE_KEY);
        if (cached != null)
        {
            // FastJson2 序列化器读回为 JSONArray，需显式转回实体（与 DictUtils 同一约定）
            return cached.toList(TrTradeCategory.class);
        }
        TrTradeCategory query = new TrTradeCategory();
        query.setStatus("0");
        List<TrTradeCategory> list = trTradeCategoryMapper.selectTrTradeCategoryList(query);
        redisCache.setCacheObject(ACTIVE_CATEGORY_CACHE_KEY, list);
        return list;
    }

    /** 任一分类写操作后清除启用分类列表缓存，下次查询回源重建。 */
    private void evictActiveCategoryCache()
    {
        redisCache.deleteObject(ACTIVE_CATEGORY_CACHE_KEY);
    }

    @Override
    public boolean checkCategoryNameUnique(TrTradeCategory category)
    {
        Long categoryId = category.getCategoryId() == null ? -1L : category.getCategoryId();
        TrTradeCategory info = trTradeCategoryMapper.checkCategoryNameUnique(
                category.getCategoryName(), category.getParentId());
        if (StringUtils.isNotNull(info) && info.getCategoryId().longValue() != categoryId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 删除前的引用与子节点校验
     */
    private void ensureDeletable(Long categoryId)
    {
        if (categoryId == null)
        {
            throw new ServiceException("删除分类失败，分类ID不能为空");
        }
        if (trTradeCategoryMapper.countChildrenByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("删除分类失败，存在子分类，请先删除子分类");
        }
        if (trTradeCategoryMapper.countGoodsByCategoryId(categoryId) > 0)
        {
            throw new ServiceException("删除分类失败，该分类下存在商品");
        }
    }
}
