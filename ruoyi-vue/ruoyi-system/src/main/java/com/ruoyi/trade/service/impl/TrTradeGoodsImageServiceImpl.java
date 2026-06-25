package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeGoodsImage;
import com.ruoyi.trade.mapper.TrTradeGoodsImageMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.ITrTradeGoodsImageService;

/**
 * 商品图片Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeGoodsImageServiceImpl implements ITrTradeGoodsImageService 
{
    @Autowired
    private TrTradeGoodsImageMapper trTradeGoodsImageMapper;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    /**
     * 查询商品图片
     * 
     * @param imageId 商品图片主键
     * @return 商品图片
     */
    @Override
    public TrTradeGoodsImage selectTrTradeGoodsImageByImageId(Long imageId)
    {
        return trTradeGoodsImageMapper.selectTrTradeGoodsImageByImageId(imageId);
    }

    /**
     * 查询商品图片列表
     * 
     * @param trTradeGoodsImage 商品图片
     * @return 商品图片
     */
    @Override
    public List<TrTradeGoodsImage> selectTrTradeGoodsImageList(TrTradeGoodsImage trTradeGoodsImage)
    {
        return trTradeGoodsImageMapper.selectTrTradeGoodsImageList(trTradeGoodsImage);
    }

    /**
     * 新增商品图片
     * 
     * @param trTradeGoodsImage 商品图片
     * @return 结果
     */
    @Override
    public int insertTrTradeGoodsImage(TrTradeGoodsImage trTradeGoodsImage)
    {
        ensureGoodsExists(trTradeGoodsImage.getGoodsId());
        String username = SecurityUtils.getUsername();
        trTradeGoodsImage.setCreateBy(username);
        trTradeGoodsImage.setCreateTime(DateUtils.getNowDate());
        trTradeGoodsImage.setUpdateBy(username);
        trTradeGoodsImage.setUpdateTime(DateUtils.getNowDate());
        return trTradeGoodsImageMapper.insertTrTradeGoodsImage(trTradeGoodsImage);
    }

    /**
     * 修改商品图片
     * 
     * @param trTradeGoodsImage 商品图片
     * @return 结果
     */
    @Override
    public int updateTrTradeGoodsImage(TrTradeGoodsImage trTradeGoodsImage)
    {
        // 仅在调用方显式改 goodsId 时才校验目标商品存在，避免普通字段更新被多余 SELECT 拖慢
        if (trTradeGoodsImage.getGoodsId() != null)
        {
            ensureGoodsExists(trTradeGoodsImage.getGoodsId());
        }
        trTradeGoodsImage.setUpdateBy(SecurityUtils.getUsername());
        trTradeGoodsImage.setUpdateTime(DateUtils.getNowDate());
        return trTradeGoodsImageMapper.updateTrTradeGoodsImage(trTradeGoodsImage);
    }

    /**
     * 批量删除商品图片
     * 
     * @param imageIds 需要删除的商品图片主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeGoodsImageByImageIds(Long[] imageIds)
    {
        return trTradeGoodsImageMapper.deleteTrTradeGoodsImageByImageIds(imageIds);
    }

    /**
     * 删除商品图片信息
     * 
     * @param imageId 商品图片主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeGoodsImageByImageId(Long imageId)
    {
        return trTradeGoodsImageMapper.deleteTrTradeGoodsImageByImageId(imageId);
    }

    /**
     * 校验 goodsId 对应商品存在且未逻辑删除，阻止越权挂图到任意/已删商品上。
     */
    private void ensureGoodsExists(Long goodsId)
    {
        if (goodsId == null)
        {
            throw new ServiceException("商品ID不能为空");
        }
        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (goods == null)
        {
            throw new ServiceException("商品不存在或已删除（ID=" + goodsId + "）");
        }
    }
}
