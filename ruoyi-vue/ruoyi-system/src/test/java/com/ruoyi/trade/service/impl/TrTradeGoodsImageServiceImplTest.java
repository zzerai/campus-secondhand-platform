package com.ruoyi.trade.service.impl;

import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeGoodsImage;
import com.ruoyi.trade.mapper.TrTradeGoodsImageMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;

/**
 * 商品图片 Service 单测：聚焦校验 goodsId 防越权挂图。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeGoodsImageServiceImplTest
{
    @Mock private TrTradeGoodsImageMapper trTradeGoodsImageMapper;
    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;

    @InjectMocks
    private TrTradeGoodsImageServiceImpl service;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName("admin");
        LoginUser loginUser = new LoginUser(1L, 1L, sysUser,
                new HashSet<>(Collections.singletonList("*:*:*")));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, "", loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext()
    {
        SecurityContextHolder.clearContext();
    }

    @Test
    void insertShouldRejectMissingGoodsId()
    {
        TrTradeGoodsImage img = new TrTradeGoodsImage();
        img.setImageUrl("http://x/y.jpg");
        Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeGoodsImage(img));
        verify(trTradeGoodsImageMapper, never()).insertTrTradeGoodsImage(any());
    }

    @Test
    void insertShouldRejectNonExistingGoods()
    {
        TrTradeGoodsImage img = new TrTradeGoodsImage();
        img.setGoodsId(99L);
        img.setImageUrl("http://x/y.jpg");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(99L)).thenReturn(null);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeGoodsImage(img));
        Assertions.assertTrue(ex.getMessage().contains("商品不存在"));
        verify(trTradeGoodsImageMapper, never()).insertTrTradeGoodsImage(any());
    }

    @Test
    void insertShouldPassWhenGoodsExists()
    {
        TrTradeGoodsImage img = new TrTradeGoodsImage();
        img.setGoodsId(5L);
        img.setImageUrl("http://x/y.jpg");
        TrTradeGoods existing = new TrTradeGoods();
        existing.setGoodsId(5L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(5L)).thenReturn(existing);
        when(trTradeGoodsImageMapper.insertTrTradeGoodsImage(img)).thenReturn(1);

        Assertions.assertEquals(1, service.insertTrTradeGoodsImage(img));
        verify(trTradeGoodsImageMapper).insertTrTradeGoodsImage(img);
    }

    @Test
    void updateShouldSkipCheckWhenGoodsIdAbsent()
    {
        TrTradeGoodsImage img = new TrTradeGoodsImage();
        img.setImageId(10L);
        img.setImageUrl("http://x/y2.jpg");
        // goodsId 未传 → 不应查库
        when(trTradeGoodsImageMapper.updateTrTradeGoodsImage(img)).thenReturn(1);

        Assertions.assertEquals(1, service.updateTrTradeGoodsImage(img));
        verify(trTradeGoodsMapper, never()).selectTrTradeGoodsByGoodsId(any());
    }

    @Test
    void updateShouldCheckGoodsWhenGoodsIdProvided()
    {
        TrTradeGoodsImage img = new TrTradeGoodsImage();
        img.setImageId(10L);
        img.setGoodsId(99L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(99L)).thenReturn(null);

        Assertions.assertThrows(ServiceException.class,
                () -> service.updateTrTradeGoodsImage(img));
        verify(trTradeGoodsImageMapper, never()).updateTrTradeGoodsImage(any());
    }
}
