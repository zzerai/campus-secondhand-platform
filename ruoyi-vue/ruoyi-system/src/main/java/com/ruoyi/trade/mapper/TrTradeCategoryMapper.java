package com.ruoyi.trade.mapper;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeCategory;

/**
 * 商品分类Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface TrTradeCategoryMapper 
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
     * 删除商品分类
     * 
     * @param categoryId 商品分类主键
     * @return 结果
     */
    public int deleteTrTradeCategoryByCategoryId(Long categoryId);

    /**
     * 批量删除商品分类
     *
     * @param categoryIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeCategoryByCategoryIds(Long[] categoryIds);

    /**
     * 同父级下校验分类名称是否已存在（含已删数据需排除）
     *
     * @param categoryName 分类名称
     * @param parentId 父级分类ID
     * @return 命中的分类，未命中返回 null
     */
    public TrTradeCategory checkCategoryNameUnique(@org.apache.ibatis.annotations.Param("categoryName") String categoryName,
                                                   @org.apache.ibatis.annotations.Param("parentId") Long parentId);

    /**
     * 统计指定分类下未删除的子分类数量
     *
     * @param categoryId 父级分类ID
     * @return 子分类数量
     */
    public int countChildrenByCategoryId(Long categoryId);

    /**
     * 统计指定分类下未删除的商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    public int countGoodsByCategoryId(Long categoryId);
}
