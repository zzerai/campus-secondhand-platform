package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.dto.AppGoodsPublishDto;
import com.ruoyi.trade.domain.dto.AppGoodsUpdateDto;
import com.ruoyi.trade.mapper.TrTradeFavoriteMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.IAppGoodsService;
import com.ruoyi.trade.task.GoodsViewCountBuffer;

/**
 * 移动端商品业务实现。
 *
 * <p>商品状态枚举（参见 CLAUDE.md / 01_ddl.sql）：
 * 0-待审核 1-已上架 2-审核拒绝 3-已下架 4-已售出。</p>
 */
@Service
public class AppGoodsServiceImpl implements IAppGoodsService
{
    /** 待审核 */
    private static final String GOODS_PENDING = "0";
    /** 已上架 */
    private static final String GOODS_ON_SALE = "1";
    /** 已下架 */
    private static final String GOODS_OFFLINE = "3";
    /** 已售出 */
    private static final String GOODS_SOLD = "4";

    /** 单个商品最多图片数 */
    private static final int MAX_IMAGES = 9;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrTradeFavoriteMapper trTradeFavoriteMapper;

    @Autowired
    private GoodsViewCountBuffer goodsViewCountBuffer;

    @Override
    public List<TrTradeGoods> selectAppGoodsList(TrTradeGoods goods)
    {
        return trTradeGoodsMapper.selectAppGoodsList(goods);
    }

    @Override
    public TrTradeGoods selectTradeGoodsById(Long goodsId, Long currentUserId)
    {
        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (goods == null)
        {
            return null;
        }
        boolean owner = currentUserId != null && currentUserId.equals(goods.getSellerId());
        // 非卖家本人只能查看已上架商品，避免泄露待审核/已拒绝/已下架商品
        if (!owner && !GOODS_ON_SALE.equals(goods.getGoodsStatus()))
        {
            return null;
        }
        // 单独取图片 CSV 快照供前端展示
        goods.setImageUrls(trTradeGoodsMapper.selectGoodsImageUrls(goodsId));
        // 仅他人浏览才累加浏览量，卖家查看自己的商品不计数；
        // 浏览量先入 Redis 缓冲，由 GoodsViewCountBuffer 定时批量回刷，避免每次浏览写库
        if (!owner)
        {
            goodsViewCountBuffer.increment(goodsId);
            // 匿名/非卖家访问时清除学号、审核痕迹与审计账号等不应外泄的字段
            sanitizeForGuest(goods);
        }
        // 登录态下回填"是否已收藏"，便于前端心形图标渲染；卖家本人/匿名为 null
        if (currentUserId != null && !owner)
        {
            goods.setIsFavorite(trTradeFavoriteMapper.existsFavorite(currentUserId, goodsId) > 0);
        }
        return goods;
    }

    @Override
    public List<TrTradeGoods> selectMyGoodsList(TrTradeGoods goods)
    {
        return trTradeGoodsMapper.selectMyGoodsList(goods);
    }

    @Override
    @Transactional
    public int publishGoods(AppGoodsPublishDto dto, Long sellerId, String createBy)
    {
        if (sellerId == null)
        {
            throw new ServiceException("卖家ID不能为空");
        }
        if (dto == null)
        {
            throw new ServiceException("请求体不能为空");
        }

        // 仅从 DTO 取用户可填字段：审核痕迹 / view / favorite / 状态 / del_flag 等服务端固定
        TrTradeGoods goods = new TrTradeGoods();
        goods.setSellerId(sellerId);
        goods.setCategoryId(dto.getCategoryId());
        goods.setTitle(dto.getTitle());
        goods.setPrice(dto.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice());
        goods.setQuality(dto.getQuality());
        goods.setDescription(dto.getDescription());
        goods.setTradePlace(dto.getTradePlace());
        goods.setContactWay(dto.getContactWay());
        goods.setImageUrls(dto.getImageUrls());
        goods.setRemark(dto.getRemark());
        goods.setCreateBy(createBy);
        goods.setUpdateBy(createBy);
        // createTime / updateTime 由 AuditTimeFillInterceptor 兜底
        // 移动端发布的商品一律进入待审核，由管理员审核后上架
        goods.setGoodsStatus(GOODS_PENDING);

        validateGoods(goods);
        List<String> imageUrls = splitImages(goods.getImageUrls());
        validateImages(imageUrls);

        int rows = trTradeGoodsMapper.insertTrTradeGoods(goods);
        if (!imageUrls.isEmpty())
        {
            trTradeGoodsMapper.batchInsertGoodsImages(goods.getGoodsId(), imageUrls, createBy);
        }
        return rows;
    }

    @Override
    @Transactional
    public int updateGoods(AppGoodsUpdateDto dto, Long sellerId, String updateBy)
    {
        if (dto == null || dto.getGoodsId() == null)
        {
            throw new ServiceException("商品ID不能为空");
        }
        if (sellerId == null)
        {
            throw new ServiceException("用户ID不能为空");
        }
        TrTradeGoods oldGoods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(dto.getGoodsId());
        if (oldGoods == null || !oldGoods.getSellerId().equals(sellerId))
        {
            throw new ServiceException("只能修改自己发布的商品");
        }
        if (GOODS_SOLD.equals(oldGoods.getGoodsStatus()))
        {
            throw new ServiceException("已售出的商品不能修改");
        }
        // 在合并旧值之前先取旧图片 CSV，用于后续比较是否实际变更
        String oldImageCsv = trTradeGoodsMapper.selectGoodsImageUrls(oldGoods.getGoodsId());

        // 仅从 DTO 取用户可填字段；卖家ID 用 token 推导而非 DTO；审核字段服务端强制不下发
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(dto.getGoodsId());
        goods.setSellerId(sellerId);
        goods.setCategoryId(dto.getCategoryId() != null ? dto.getCategoryId() : oldGoods.getCategoryId());
        goods.setTitle(dto.getTitle() != null ? dto.getTitle() : oldGoods.getTitle());
        goods.setPrice(dto.getPrice() != null ? dto.getPrice() : oldGoods.getPrice());
        goods.setOriginalPrice(dto.getOriginalPrice() != null ? dto.getOriginalPrice() : oldGoods.getOriginalPrice());
        goods.setQuality(dto.getQuality() != null ? dto.getQuality() : oldGoods.getQuality());
        goods.setDescription(dto.getDescription() != null ? dto.getDescription() : oldGoods.getDescription());
        goods.setTradePlace(dto.getTradePlace() != null ? dto.getTradePlace() : oldGoods.getTradePlace());
        goods.setContactWay(dto.getContactWay() != null ? dto.getContactWay() : oldGoods.getContactWay());
        goods.setRemark(dto.getRemark() != null ? dto.getRemark() : oldGoods.getRemark());
        goods.setImageUrls(dto.getImageUrls() != null ? dto.getImageUrls() : oldImageCsv);
        goods.setUpdateBy(updateBy);
        // 商品内容变更后需重新审核，状态由服务端强制重置
        goods.setGoodsStatus(GOODS_PENDING);

        validateGoods(goods);
        List<String> imageUrls = splitImages(goods.getImageUrls());
        validateImages(imageUrls);

        int rows = trTradeGoodsMapper.updateTrTradeGoods(goods);
        // 仅在图片实际变更时才重建：保持未变图片的 image_id 不变，避免被收藏/外链失效
        List<String> oldImageUrls = splitImages(oldImageCsv);
        if (!oldImageUrls.equals(imageUrls))
        {
            trTradeGoodsMapper.deleteGoodsImagesByGoodsId(goods.getGoodsId());
            if (!imageUrls.isEmpty())
            {
                trTradeGoodsMapper.batchInsertGoodsImages(goods.getGoodsId(), imageUrls, updateBy);
            }
        }
        return rows;
    }

    @Override
    public int offlineGoods(Long goodsId, Long userId)
    {
        // 仅允许把"已上架"的商品下架为"已下架"
        int rows = trTradeGoodsMapper.updateGoodsStatusByOwner(goodsId, userId, GOODS_OFFLINE, GOODS_ON_SALE);
        if (rows == 0)
        {
            throw new ServiceException("只能下架自己发布且已上架的商品");
        }
        return rows;
    }

    @Override
    public int deleteGoods(Long goodsId, Long userId)
    {
        int rows = trTradeGoodsMapper.deleteGoodsByOwner(goodsId, userId);
        if (rows == 0)
        {
            throw new ServiceException("只能删除自己发布且未售出的商品");
        }
        return rows;
    }

    /**
     * 匿名 / 非卖家查看商品详情时，清除不应对外暴露的字段：
     * 卖家学号、审核痕迹（审核管理员/时间/意见）、删除标记及审计账号。
     */
    private void sanitizeForGuest(TrTradeGoods goods)
    {
        goods.setSellerStudentNo(null);
        goods.setAuditUserId(null);
        goods.setAuditTime(null);
        goods.setAuditRemark(null);
        goods.setDelFlag(null);
        goods.setCreateBy(null);
        goods.setUpdateBy(null);
    }

    private void validateGoods(TrTradeGoods goods)
    {
        if (StringUtils.isEmpty(goods.getTitle()))
        {
            throw new ServiceException("商品标题不能为空");
        }
        if (goods.getPrice() == null || goods.getPrice().compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("商品价格不能小于0");
        }
        if (goods.getCategoryId() == null)
        {
            throw new ServiceException("商品分类不能为空");
        }
        if (StringUtils.isEmpty(goods.getDescription()))
        {
            throw new ServiceException("商品描述不能为空");
        }
    }

    private void validateImages(List<String> imageUrls)
    {
        if (imageUrls.size() > MAX_IMAGES)
        {
            throw new ServiceException("商品图片最多上传 " + MAX_IMAGES + " 张");
        }
    }

    /**
     * 将逗号分隔的图片字符串拆分为去空白、去空项的 URL 列表。
     */
    private List<String> splitImages(String images)
    {
        List<String> urls = new ArrayList<>();
        if (StringUtils.isEmpty(images))
        {
            return urls;
        }
        for (String url : images.split(","))
        {
            String trimmed = url.trim();
            if (StringUtils.isNotEmpty(trimmed))
            {
                urls.add(trimmed);
            }
        }
        return urls;
    }

}
