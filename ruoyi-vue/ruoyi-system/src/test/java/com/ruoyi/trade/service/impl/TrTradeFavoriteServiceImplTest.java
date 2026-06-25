package com.ruoyi.trade.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.mapper.TrTradeFavoriteMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;

/**
 * 商品收藏 Service 单测：覆盖添加 / 取消 / 是否已收藏 / 我的收藏，并验证 favorite_count 维护仅在状态切换时发生。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeFavoriteServiceImplTest
{
    @Mock private TrTradeFavoriteMapper trTradeFavoriteMapper;
    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;

    @InjectMocks
    private TrTradeFavoriteServiceImpl service;

    /** 构造一个已上架、属于 sellerId 的商品。 */
    private TrTradeGoods onSaleGoods(Long goodsId, Long sellerId)
    {
        TrTradeGoods goods = new TrTradeGoods();
        goods.setGoodsId(goodsId);
        goods.setSellerId(sellerId);
        goods.setGoodsStatus("1");
        return goods;
    }

    @Test
    void addFavoriteShouldRejectMissingIds()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.addFavorite(null, 1L, "u1"));
        Assertions.assertThrows(ServiceException.class,
                () -> service.addFavorite(1L, null, "u1"));
        verify(trTradeFavoriteMapper, never()).upsertFavorite(anyLong(), anyLong(), anyString());
    }

    @Test
    void addFavoriteShouldRejectMissingGoods()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(99L)).thenReturn(null);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.addFavorite(1L, 99L, "u1"));
        Assertions.assertTrue(ex.getMessage().contains("商品不存在"));
        verify(trTradeFavoriteMapper, never()).upsertFavorite(anyLong(), anyLong(), anyString());
    }

    @Test
    void addFavoriteShouldRejectOwnGoods()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L))
                .thenReturn(onSaleGoods(10L, 3L));
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.addFavorite(3L, 10L, "u3"));
        Assertions.assertTrue(ex.getMessage().contains("不能收藏自己"));
        verify(trTradeFavoriteMapper, never()).upsertFavorite(anyLong(), anyLong(), anyString());
    }

    @Test
    void addFavoriteShouldRejectGoodsNotOnSale()
    {
        TrTradeGoods goods = onSaleGoods(10L, 3L);
        goods.setGoodsStatus("0"); // 待审核
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L)).thenReturn(goods);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.addFavorite(5L, 10L, "u5"));
        Assertions.assertTrue(ex.getMessage().contains("只能收藏已上架"));
        verify(trTradeFavoriteMapper, never()).upsertFavorite(anyLong(), anyLong(), anyString());
    }

    @Test
    void addFavoriteShouldUpsertAndIncrementCountWhenNewlyFavorited()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L))
                .thenReturn(onSaleGoods(10L, 3L));
        // 当前未收藏 → upsert 后应累加 favorite_count
        when(trTradeFavoriteMapper.existsFavorite(5L, 10L)).thenReturn(0);

        Assertions.assertEquals(1, service.addFavorite(5L, 10L, "u5"));
        verify(trTradeFavoriteMapper).upsertFavorite(5L, 10L, "u5");
        verify(trTradeGoodsMapper).increaseFavoriteCount(10L);
    }

    @Test
    void addFavoriteShouldBeIdempotentWhenAlreadyFavorited()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(10L))
                .thenReturn(onSaleGoods(10L, 3L));
        // 原本已收藏 → upsert 仅刷新 update_time，favorite_count 不应再加
        when(trTradeFavoriteMapper.existsFavorite(5L, 10L)).thenReturn(1);

        Assertions.assertEquals(0, service.addFavorite(5L, 10L, "u5"));
        verify(trTradeFavoriteMapper).upsertFavorite(5L, 10L, "u5");
        verify(trTradeGoodsMapper, never()).increaseFavoriteCount(anyLong());
    }

    @Test
    void removeFavoriteShouldRejectMissingIds()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.removeFavorite(null, 1L, "u1"));
        Assertions.assertThrows(ServiceException.class,
                () -> service.removeFavorite(1L, null, "u1"));
        verify(trTradeFavoriteMapper, never())
                .softDeleteByUserAndGoods(anyLong(), anyLong(), anyString());
    }

    @Test
    void removeFavoriteShouldDecrementCountWhenSucceeded()
    {
        when(trTradeFavoriteMapper.softDeleteByUserAndGoods(eq(5L), eq(10L), eq("u5")))
                .thenReturn(1);

        Assertions.assertEquals(1, service.removeFavorite(5L, 10L, "u5"));
        verify(trTradeGoodsMapper).decreaseFavoriteCount(10L);
    }

    @Test
    void removeFavoriteShouldNotDecrementWhenNoActiveRow()
    {
        // 原本就未收藏：幂等成功，但 favorite_count 不应被减
        when(trTradeFavoriteMapper.softDeleteByUserAndGoods(eq(5L), eq(10L), eq("u5")))
                .thenReturn(0);

        Assertions.assertEquals(0, service.removeFavorite(5L, 10L, "u5"));
        verify(trTradeGoodsMapper, never()).decreaseFavoriteCount(anyLong());
    }

    @Test
    void existsFavoriteShouldReturnFalseForNullIds()
    {
        Assertions.assertFalse(service.existsFavorite(null, 1L));
        Assertions.assertFalse(service.existsFavorite(1L, null));
        verify(trTradeFavoriteMapper, never()).existsFavorite(anyLong(), anyLong());
    }

    @Test
    void existsFavoriteShouldReflectMapperResult()
    {
        when(trTradeFavoriteMapper.existsFavorite(5L, 10L)).thenReturn(1);
        Assertions.assertTrue(service.existsFavorite(5L, 10L));

        when(trTradeFavoriteMapper.existsFavorite(5L, 11L)).thenReturn(0);
        Assertions.assertFalse(service.existsFavorite(5L, 11L));
    }

    @Test
    void selectMyFavoriteGoodsShouldRejectNullUserId()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.selectMyFavoriteGoods(null));
    }

    @Test
    void selectMyFavoriteGoodsShouldDelegateToMapper()
    {
        List<TrTradeGoods> expected = Arrays.asList(onSaleGoods(10L, 3L), onSaleGoods(11L, 4L));
        when(trTradeFavoriteMapper.selectMyFavoriteGoods(5L)).thenReturn(expected);

        Assertions.assertEquals(expected, service.selectMyFavoriteGoods(5L));
    }

    @Test
    void selectMyFavoriteGoodsShouldReturnEmptyWhenNone()
    {
        when(trTradeFavoriteMapper.selectMyFavoriteGoods(5L)).thenReturn(Collections.emptyList());
        Assertions.assertTrue(service.selectMyFavoriteGoods(5L).isEmpty());
    }
}
