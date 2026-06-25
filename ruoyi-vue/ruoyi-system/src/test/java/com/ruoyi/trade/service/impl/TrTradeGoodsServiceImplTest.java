package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.TrTradeCategory;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.vo.BatchAuditResult;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeCategoryMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsImageMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;

/**
 * 闲置商品 Service 单元测试，覆盖审核、上下架、增改、删除关键路径。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeGoodsServiceImplTest
{
    private static final String FAKE_ADMIN = "admin";
    private static final Long FAKE_ADMIN_ID = 1L;

    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;
    @Mock private TrTradeGoodsImageMapper trTradeGoodsImageMapper;
    @Mock private TrTradeCategoryMapper trTradeCategoryMapper;
    @Mock private TrStudentUserMapper trStudentUserMapper;
    @Mock private TrTradeOrderMapper trTradeOrderMapper;

    @InjectMocks
    private TrTradeGoodsServiceImpl service;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(FAKE_ADMIN_ID);
        sysUser.setUserName(FAKE_ADMIN);
        LoginUser loginUser = new LoginUser(FAKE_ADMIN_ID, 1L, sysUser,
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

    // -------- insert --------

    @Test
    void insertShouldRejectMissingCategory()
    {
        TrTradeGoods g = baseGoods();
        g.setCategoryId(null);
        Assertions.assertThrows(ServiceException.class, () -> service.insertTrTradeGoods(g));
        verify(trTradeGoodsMapper, never()).insertTrTradeGoods(any());
    }

    @Test
    void insertShouldRejectDisabledCategory()
    {
        TrTradeGoods g = baseGoods();
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryId(10L);
        cat.setStatus("1"); // 停用
        when(trTradeCategoryMapper.selectTrTradeCategoryByCategoryId(10L)).thenReturn(cat);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeGoods(g));
        Assertions.assertTrue(ex.getMessage().contains("分类"));
        verify(trTradeGoodsMapper, never()).insertTrTradeGoods(any());
    }

    @Test
    void insertShouldRejectDisabledSeller()
    {
        TrTradeGoods g = baseGoods();
        stubCategoryOk();
        TrStudentUser s = new TrStudentUser();
        s.setUserId(99L);
        s.setStatus("1");
        when(trStudentUserMapper.selectTrStudentUserByUserId(99L)).thenReturn(s);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.insertTrTradeGoods(g));
        Assertions.assertTrue(ex.getMessage().contains("禁用"));
    }

    @Test
    void insertShouldDefaultStatusAndStampAudit()
    {
        TrTradeGoods g = baseGoods();
        stubCategoryOk();
        stubSellerOk();
        when(trTradeGoodsMapper.insertTrTradeGoods(g)).thenReturn(1);

        int rows = service.insertTrTradeGoods(g);

        Assertions.assertEquals(1, rows);
        Assertions.assertEquals("1", g.getGoodsStatus()); // 默认已上架
        Assertions.assertEquals(FAKE_ADMIN, g.getCreateBy());
        Assertions.assertEquals(FAKE_ADMIN, g.getUpdateBy());
        Assertions.assertNotNull(g.getCreateTime());
    }

    @Test
    void insertShouldRejectNegativePrice()
    {
        TrTradeGoods g = baseGoods();
        g.setPrice(new BigDecimal("-1"));
        stubCategoryOk();
        stubSellerOk();
        Assertions.assertThrows(ServiceException.class, () -> service.insertTrTradeGoods(g));
        verify(trTradeGoodsMapper, never()).insertTrTradeGoods(any());
    }

    // -------- update --------

    @Test
    void updateShouldRejectMissingId()
    {
        TrTradeGoods g = baseGoods();
        g.setGoodsId(null);
        Assertions.assertThrows(ServiceException.class, () -> service.updateTrTradeGoods(g));
    }

    @Test
    void updateShouldRejectSoldGoods()
    {
        TrTradeGoods g = baseGoods();
        g.setGoodsId(5L);
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(5L);
        exist.setGoodsStatus("4"); // 已售出
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(5L)).thenReturn(exist);
        Assertions.assertThrows(ServiceException.class, () -> service.updateTrTradeGoods(g));
        verify(trTradeGoodsMapper, never()).updateTrTradeGoods(any());
    }

    @Test
    void updateShouldStampUpdateByAndClearStatusFields()
    {
        TrTradeGoods g = baseGoods();
        g.setGoodsId(6L);
        g.setCategoryId(null); // 不改外键，跳过 FK 校验
        g.setSellerId(null);
        // 客户端尝试通过 edit 改状态/审核字段 → 服务端必须丢弃
        g.setGoodsStatus("4");
        g.setAuditUserId(999L);
        g.setAuditRemark("我自己通过");
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(6L);
        exist.setGoodsStatus("1");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(6L)).thenReturn(exist);
        when(trTradeGoodsMapper.updateTrTradeGoods(g)).thenReturn(1);

        int rows = service.updateTrTradeGoods(g);

        Assertions.assertEquals(1, rows);
        Assertions.assertEquals(FAKE_ADMIN, g.getUpdateBy());
        Assertions.assertNotNull(g.getUpdateTime());
        Assertions.assertNull(g.getGoodsStatus());
        Assertions.assertNull(g.getAuditUserId());
        Assertions.assertNull(g.getAuditRemark());
    }

    // -------- audit single --------

    @Test
    void auditShouldRejectIllegalTargetStatus()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(1L, "9", "x"));
        Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(1L, "0", "x"));
    }

    @Test
    void auditShouldRejectWhenGoodsMissing()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(null);
        Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(1L, "1", null));
    }

    @Test
    void auditShouldRejectWhenAlreadyAudited()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(1L);
        exist.setGoodsStatus("1");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(exist);
        Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(1L, "1", null));
    }

    @Test
    void auditShouldRequireRemarkWhenReject()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(1L);
        exist.setGoodsStatus("0");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(exist);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(1L, "2", ""));
        Assertions.assertTrue(ex.getMessage().contains("审核意见"));
    }

    @Test
    void auditShouldPassWithAdminContext()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(1L);
        exist.setGoodsStatus("0");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(exist);
        when(trTradeGoodsMapper.updateGoodsAudit(eq(1L), eq("1"), eq(FAKE_ADMIN_ID),
                any(), any(), eq(FAKE_ADMIN), any())).thenReturn(1);

        int rows = service.auditGoods(1L, "1", "ok");

        Assertions.assertEquals(1, rows);
        verify(trTradeGoodsMapper).updateGoodsAudit(eq(1L), eq("1"), eq(FAKE_ADMIN_ID),
                any(), eq("ok"), eq(FAKE_ADMIN), any());
    }

    // -------- batch audit --------

    @Test
    void batchAuditShouldReturnSuccessAndErrorDetail()
    {
        TrTradeGoods ok1 = baseGoods(); ok1.setGoodsId(1L); ok1.setGoodsStatus("0");
        TrTradeGoods notPending = baseGoods(); notPending.setGoodsId(2L); notPending.setGoodsStatus("1");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(ok1);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(2L)).thenReturn(notPending);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(3L)).thenReturn(null);
        when(trTradeGoodsMapper.updateGoodsAudit(eq(1L), eq("1"), any(), any(), any(), any(), any()))
                .thenReturn(1);

        BatchAuditResult result = service.batchAuditGoods(new Long[]{1L, 2L, 3L}, "1", "通过");

        Assertions.assertEquals(1, result.getSuccess());
        Assertions.assertEquals(2, result.getFailure());
        Assertions.assertEquals(2, result.getErrors().size());
        // 失败明细必须能定位到具体 goodsId
        Assertions.assertTrue(result.getErrors().stream().anyMatch(e -> e.getGoodsId().equals(2L)));
        Assertions.assertTrue(result.getErrors().stream().anyMatch(e -> e.getGoodsId().equals(3L)));
        verify(trTradeGoodsMapper, times(1)).updateGoodsAudit(eq(1L), anyString(),
                any(), any(), any(), anyString(), any());
    }

    @Test
    void batchAuditShouldRejectEmptyIds()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.batchAuditGoods(new Long[0], "1", null));
    }

    // -------- offline / online --------

    @Test
    void offlineShouldOnlyAllowOnShelf()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(8L);
        exist.setGoodsStatus("0"); // 待审核
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(8L)).thenReturn(exist);
        Assertions.assertThrows(ServiceException.class, () -> service.offlineGoods(8L));
    }

    @Test
    void offlineShouldFlipStatusFromOneToThree()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(8L);
        exist.setGoodsStatus("1");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(8L)).thenReturn(exist);
        when(trTradeGoodsMapper.updateGoodsStatus(eq(8L), eq("3"), eq(FAKE_ADMIN), any()))
                .thenReturn(1);
        Assertions.assertEquals(1, service.offlineGoods(8L));
    }

    @Test
    void onlineShouldOnlyAllowOffShelf()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(9L);
        exist.setGoodsStatus("1");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(9L)).thenReturn(exist);
        Assertions.assertThrows(ServiceException.class, () -> service.onlineGoods(9L));
    }

    @Test
    void onlineShouldFlipStatusFromThreeToOne()
    {
        TrTradeGoods exist = baseGoods();
        exist.setGoodsId(9L);
        exist.setGoodsStatus("3");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(9L)).thenReturn(exist);
        ArgumentCaptor<String> statusCap = ArgumentCaptor.forClass(String.class);
        when(trTradeGoodsMapper.updateGoodsStatus(eq(9L), statusCap.capture(), any(), any()))
                .thenReturn(1);
        Assertions.assertEquals(1, service.onlineGoods(9L));
        Assertions.assertEquals("1", statusCap.getValue());
    }

    // -------- delete (with related-orders guard) --------

    @Test
    void deleteShouldRejectSoldGoods()
    {
        TrTradeGoods sold = baseGoods();
        sold.setGoodsId(11L);
        sold.setGoodsStatus("4");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(11L)).thenReturn(sold);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.deleteTrTradeGoodsByGoodsId(11L));
        Assertions.assertTrue(ex.getMessage().contains("已售出"));
        verify(trTradeGoodsMapper, never()).deleteTrTradeGoodsByGoodsId(anyLong());
    }

    @Test
    void deleteShouldRejectGoodsWithActiveOrders()
    {
        TrTradeGoods onShelf = baseGoods();
        onShelf.setGoodsId(12L);
        onShelf.setGoodsStatus("1");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(12L)).thenReturn(onShelf);
        when(trTradeOrderMapper.countActiveOrdersByGoodsId(12L)).thenReturn(2);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.deleteTrTradeGoodsByGoodsId(12L));
        Assertions.assertTrue(ex.getMessage().contains("未完成订单"));
    }

    @Test
    void deleteByIdsShouldCheckEachAndDelegate()
    {
        TrTradeGoods g1 = baseGoods(); g1.setGoodsId(1L); g1.setGoodsStatus("0");
        TrTradeGoods g2 = baseGoods(); g2.setGoodsId(2L); g2.setGoodsStatus("3");
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(g1);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(2L)).thenReturn(g2);
        when(trTradeOrderMapper.countActiveOrdersByGoodsId(anyLong())).thenReturn(0);
        Long[] ids = {1L, 2L};
        when(trTradeGoodsMapper.deleteTrTradeGoodsByGoodsIds(ids)).thenReturn(2);

        Assertions.assertEquals(2, service.deleteTrTradeGoodsByGoodsIds(ids));
        verify(trTradeGoodsMapper).deleteTrTradeGoodsByGoodsIds(ids);
    }

    @Test
    void selectByIdShouldAlsoLoadImages()
    {
        TrTradeGoods g = baseGoods();
        g.setGoodsId(7L);
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(7L)).thenReturn(g);
        when(trTradeGoodsImageMapper.selectTrTradeGoodsImageList(any()))
                .thenReturn(Collections.emptyList());

        TrTradeGoods got = service.selectTrTradeGoodsByGoodsId(7L);
        Assertions.assertSame(g, got);
        Assertions.assertNotNull(got.getImages());
        verify(trTradeGoodsImageMapper).selectTrTradeGoodsImageList(any());
    }

    @Test
    void selectByIdMissingShouldReturnNull()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(anyLong())).thenReturn(null);
        Assertions.assertNull(service.selectTrTradeGoodsByGoodsId(99L));
        verify(trTradeGoodsImageMapper, never()).selectTrTradeGoodsImageList(any());
    }

    // -------- helpers --------

    private TrTradeGoods baseGoods()
    {
        TrTradeGoods g = new TrTradeGoods();
        g.setCategoryId(10L);
        g.setSellerId(99L);
        g.setTitle("二手力扣红宝书");
        g.setPrice(new BigDecimal("25.00"));
        return g;
    }

    private void stubCategoryOk()
    {
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryId(10L);
        cat.setStatus("0");
        when(trTradeCategoryMapper.selectTrTradeCategoryByCategoryId(10L)).thenReturn(cat);
    }

    private void stubSellerOk()
    {
        TrStudentUser s = new TrStudentUser();
        s.setUserId(99L);
        s.setStatus("0");
        when(trStudentUserMapper.selectTrStudentUserByUserId(99L)).thenReturn(s);
    }
}
