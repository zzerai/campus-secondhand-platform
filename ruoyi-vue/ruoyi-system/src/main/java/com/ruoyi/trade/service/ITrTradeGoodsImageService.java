package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeGoodsImage;

/**
 * 商品图片Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeGoodsImageService 
{
    /**
     * 查询商品图片
     * 
     * @param imageId 商品图片主键
     * @return 商品图片
     */
    public TrTradeGoodsImage selectTrTradeGoodsImageByImageId(Long imageId);

    /**
     * 查询商品图片列表
     * 
     * @param trTradeGoodsImage 商品图片
     * @return 商品图片集合
     */
    public List<TrTradeGoodsImage> selectTrTradeGoodsImageList(TrTradeGoodsImage trTradeGoodsImage);

    /**
     * 新增商品图片
     * 
     * @param trTradeGoodsImage 商品图片
     * @return 结果
     */
    public int insertTrTradeGoodsImage(TrTradeGoodsImage trTradeGoodsImage);

    /**
     * 修改商品图片
     * 
     * @param trTradeGoodsImage 商品图片
     * @return 结果
     */
    public int updateTrTradeGoodsImage(TrTradeGoodsImage trTradeGoodsImage);

    /**
     * 批量删除商品图片
     * 
     * @param imageIds 需要删除的商品图片主键集合
     * @return 结果
     */
    public int deleteTrTradeGoodsImageByImageIds(Long[] imageIds);

    /**
     * 删除商品图片信息
     * 
     * @param imageId 商品图片主键
     * @return 结果
     */
    public int deleteTrTradeGoodsImageByImageId(Long imageId);
}
