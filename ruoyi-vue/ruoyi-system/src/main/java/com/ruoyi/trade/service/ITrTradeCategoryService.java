package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeCategory;

/**
 * 商品分类Service接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface ITrTradeCategoryService 
{
    /**
     * 查询商品分类
     * 
     * @param categoryId 商品分类主键
     * @return 商品分类
     */
    public TrTradeCategory selectTrTradeCategoryByCategoryId(Long categoryId);

    /**
     * 查询商品分类列表
     * 
     * @param trTradeCategory 商品分类
     * @return 商品分类集合
     */
    public List<TrTradeCategory> selectTrTradeCategoryList(TrTradeCategory trTradeCategory);

    /**
     * 新增商品分类
     * 
     * @param trTradeCategory 商品分类
     * @return 结果
     */
    public int insertTrTradeCategory(TrTradeCategory trTradeCategory);

    /**
     * 修改商品分类
     * 
     * @param trTradeCategory 商品分类
     * @return 结果
     */
    public int updateTrTradeCategory(TrTradeCategory trTradeCategory);

    /**
     * 批量删除商品分类
     * 
     * @param categoryIds 需要删除的商品分类主键集合
     * @return 结果
     */
    public int deleteTrTradeCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 删除商品分类信息
     *
     * @param categoryId 商品分类主键
     * @return 结果
     */
    public int deleteTrTradeCategoryByCategoryId(Long categoryId);

    /**
     * 修改分类状态（启用/停用）
     */
    public int updateCategoryStatus(TrTradeCategory category);

    /**
     * 查询所有正常状态的分类（不分页，前端下拉/级联用）
     */
    public List<TrTradeCategory> selectAllActiveCategories();

    /**
     * 校验同父级下分类名称是否唯一
     */
    public boolean checkCategoryNameUnique(TrTradeCategory category);
}
