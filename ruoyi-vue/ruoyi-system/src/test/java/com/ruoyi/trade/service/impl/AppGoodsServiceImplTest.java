package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.dto.AppGoodsPublishDto;
import com.ruoyi.trade.domain.dto.AppGoodsUpdateDto;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.task.GoodsViewCountBuffer;

/**
 * 移动端商品业务单测。
 */
@ExtendWith(MockitoExtension.class)
class AppGoodsServiceImplTest
{
    @Mock
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Mock
    private GoodsViewCountBuffer goodsViewCountBuffer;

    @InjectMocks
    private AppGoodsServiceImpl appGoodsService;

    @Test
    void publishShouldValidateAndDefaultPendingStatus()
    {
        AppGoodsPublishDto dto = validPublishDto();
        ArgumentCaptor<TrTradeGoods> captor = ArgumentCaptor.forClass(TrTradeGoods.class);
        when(trTradeGoodsMapper.insertTrTradeGoods(captor.capture())).thenReturn(1);

        int rows = appGoodsService.publishGoods(dto, 3L, "student");

        Assertions.assertEquals(1, rows);
        TrTradeGoods saved = captor.getValue();
        Assertions.assertEquals("0", saved.getGoodsStatus());
        Assertions.assertEquals(3L, saved.getSellerId());
        Assertions.assertEquals("student", saved.getCreateBy());
        // 关键防御断言：审核字段 / 统计字段 / del_flag 均未被 DTO 注入
        Assertions.assertNull(saved.getAuditUserId());
        Assertions.assertNull(saved.getAuditTime());
        Assertions.assertNull(saved.getAuditRemark());
        Assertions.assertNull(saved.getViewCount());
        Assertions.assertNull(saved.getFavoriteCount());
        Assertions.assertNull(saved.getDelFlag());
    }

    @Test
    void publishShouldRejectEmptyTitle()
    {
        AppGoodsPublishDto dto = validPublishDto();
        dto.setTitle("");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appGoodsService.publishGoods(dto, 3L, "student"));

        Assertions.assertTrue(ex.getMessage().contains("商品标题不能为空"));
        verify(trTradeGoodsMapper, never()).insertTrTradeGoods(any());
    }

    @Test
    void publishShouldRejectMoreThanNineImages()
    {
        AppGoodsPublishDto dto = validPublishDto();
        dto.setImageUrls("a,b,c,d,e,f,g,h,i,j");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appGoodsService.publishGoods(dto, 3L, "student"));

        Assertions.assertTrue(ex.getMessage().contains("图片"));
        verify(trTradeGoodsMapper, never()).insertTrTradeGoods(any());
    }

    @Test
    void updateShouldMergeMissingFieldsAndResetToPending()
    {
        TrTradeGoods oldGoods = persistedGoods();
        oldGoods.setGoodsStatus("1");

        AppGoodsUpdateDto dto = new AppGoodsUpdateDto();
        dto.setGoodsId(10L);
        dto.setPrice(new BigDecimal("18.50"));
        // 让本测试聚焦字段合并 + 状态重置：传入与旧库相同的图片 CSV，避免触发图片重建分支
        dto.setImageUrls("a.jpg,b.jpg");

        ArgumentCaptor<TrTradeGoods> captor = ArgumentCaptor.forClass(TrTradeGoods.class);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(oldGoods);
        when(trTradeGoodsMapper.selectGoodsImageUrls(10L)).thenReturn("a.jpg,b.jpg");
        when(trTradeGoodsMapper.updateTrTradeGoods(captor.capture())).thenReturn(1);

        int rows = appGoodsService.updateGoods(dto, 3L, "student");

        Assertions.assertEquals(1, rows);
        TrTradeGoods saved = captor.getValue();
        // 未传字段沿用旧值
        Assertions.assertEquals("二手教材", saved.getTitle());
        Assertions.assertEquals(5L, saved.getCategoryId());
        Assertions.assertEquals(new BigDecimal("18.50"), saved.getPrice());
        Assertions.assertEquals("student", saved.getUpdateBy());
        // 状态强制重置为待审核
        Assertions.assertEquals("0", saved.getGoodsStatus());
        // sellerId 走 token 推导
        Assertions.assertEquals(3L, saved.getSellerId());
        // 防注入断言
        Assertions.assertNull(saved.getAuditUserId());
        Assertions.assertNull(saved.getViewCount());
        Assertions.assertNull(saved.getFavoriteCount());
        // 图片未变化 → 不应触发删图/批插
        verify(trTradeGoodsMapper, never()).deleteGoodsImagesByGoodsId(anyLong());
        verify(trTradeGoodsMapper, never()).batchInsertGoodsImages(anyLong(), any(), any());
    }

    @Test
    void updateShouldRebuildImagesOnlyWhenChanged()
    {
        TrTradeGoods oldGoods = persistedGoods();
        oldGoods.setGoodsId(20L);
        oldGoods.setGoodsStatus("1");

        AppGoodsUpdateDto dto = new AppGoodsUpdateDto();
        dto.setGoodsId(20L);
        // 新图片集与旧不同
        dto.setImageUrls("new1.jpg,new2.jpg");

        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(20L)).thenReturn(oldGoods);
        when(trTradeGoodsMapper.selectGoodsImageUrls(20L)).thenReturn("old.jpg");
        when(trTradeGoodsMapper.updateTrTradeGoods(any())).thenReturn(1);

        appGoodsService.updateGoods(dto, 3L, "student");

        verify(trTradeGoodsMapper).deleteGoodsImagesByGoodsId(20L);
        verify(trTradeGoodsMapper).batchInsertGoodsImages(eq(20L), any(), eq("student"));
    }

    @Test
    void updateShouldRejectGoodsOwnedByOthers()
    {
        TrTradeGoods oldGoods = persistedGoods();
        oldGoods.setSellerId(3L);

        AppGoodsUpdateDto dto = new AppGoodsUpdateDto();
        dto.setGoodsId(10L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(oldGoods);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appGoodsService.updateGoods(dto, 4L, "intruder"));

        Assertions.assertTrue(ex.getMessage().contains("只能修改自己发布的商品"));
        verify(trTradeGoodsMapper, never()).updateTrTradeGoods(any());
    }

    @Test
    void updateShouldRejectSoldGoods()
    {
        TrTradeGoods oldGoods = persistedGoods();
        oldGoods.setGoodsStatus("4");

        AppGoodsUpdateDto dto = new AppGoodsUpdateDto();
        dto.setGoodsId(10L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(oldGoods);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appGoodsService.updateGoods(dto, 3L, "student"));

        Assertions.assertTrue(ex.getMessage().contains("已售出"));
        verify(trTradeGoodsMapper, never()).updateTrTradeGoods(any());
    }

    @Test
    void offlineShouldRejectWhenGoodsNotOnSale()
    {
        when(trTradeGoodsMapper.updateGoodsStatusByOwner(10L, 3L, "3", "1")).thenReturn(0);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appGoodsService.offlineGoods(10L, 3L));

        Assertions.assertTrue(ex.getMessage().contains("只能下架"));
    }

    @Test
    void detailShouldHideNonOnSaleGoodsFromNonOwner()
    {
        TrTradeGoods goods = persistedGoods();
        goods.setGoodsStatus("0");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(goods);

        Assertions.assertNull(appGoodsService.selectTradeGoodsById(10L, null));
        // 详情被拦截返回 null（非卖家看待审核商品）以及卖家查看自己商品，均不应累加浏览量
        verify(goodsViewCountBuffer, never()).increment(10L);
        Assertions.assertNotNull(appGoodsService.selectTradeGoodsById(10L, 3L));
    }

    @Test
    void deleteShouldRejectWhenMapperDoesNotAffectRows()
    {
        when(trTradeGoodsMapper.deleteGoodsByOwner(10L, 3L)).thenReturn(0);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> appGoodsService.deleteGoods(10L, 3L));

        Assertions.assertTrue(ex.getMessage().contains("只能删除自己发布"));
    }

    private AppGoodsPublishDto validPublishDto()
    {
        AppGoodsPublishDto dto = new AppGoodsPublishDto();
        dto.setTitle("二手教材");
        dto.setCategoryId(5L);
        dto.setPrice(new BigDecimal("20.00"));
        dto.setOriginalPrice(new BigDecimal("68.00"));
        dto.setDescription("九成新，适合复习使用");
        dto.setQuality("九成新");
        dto.setTradePlace("一食堂门口");
        dto.setContactWay("QQ:123456");
        dto.setRemark("测试数据");
        return dto;
    }

    private TrTradeGoods persistedGoods()
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(10L);
        goods.setSellerId(3L);
        goods.setTitle("二手教材");
        goods.setCategoryId(5L);
        goods.setPrice(new BigDecimal("20.00"));
        goods.setOriginalPrice(new BigDecimal("68.00"));
        goods.setDescription("九成新，适合复习使用");
        goods.setQuality("九成新");
        goods.setTradePlace("一食堂门口");
        goods.setContactWay("QQ:123456");
        goods.setRemark("测试数据");
        return goods;
    }
}
