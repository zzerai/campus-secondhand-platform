package com.ruoyi.trade.service.impl;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.alibaba.fastjson2.JSONArray;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeCategory;
import com.ruoyi.trade.mapper.TrTradeCategoryMapper;

/**
 * 商品分类 Service 单元测试
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrTradeCategoryServiceImplTest
{
    private static final String FAKE_ADMIN = "admin";

    private static final String ACTIVE_CACHE_KEY = CacheConstants.TRADE_CATEGORY_KEY + "active";

    @Mock
    private TrTradeCategoryMapper trTradeCategoryMapper;

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private TrTradeCategoryServiceImpl trTradeCategoryService;

    @BeforeEach
    void setUpSecurityContext()
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(1L);
        sysUser.setUserName(FAKE_ADMIN);
        LoginUser loginUser = new LoginUser(1L, 1L, sysUser, new HashSet<>(Collections.singletonList("*:*:*")));
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
    void insertShouldDefaultParentZeroAndStampAuditFields()
    {
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryName("数码");
        // parentId 故意留 null，期望被默认成 0

        when(trTradeCategoryMapper.checkCategoryNameUnique("数码", 0L)).thenReturn(null);
        when(trTradeCategoryMapper.insertTrTradeCategory(cat)).thenReturn(1);

        int rows = trTradeCategoryService.insertTrTradeCategory(cat);

        Assertions.assertEquals(1, rows);
        Assertions.assertEquals(0L, cat.getParentId());
        Assertions.assertEquals(FAKE_ADMIN, cat.getCreateBy());
        Assertions.assertNotNull(cat.getCreateTime());
    }

    @Test
    void insertShouldRejectDuplicateNameSameParent()
    {
        TrTradeCategory existing = new TrTradeCategory();
        existing.setCategoryId(7L);
        existing.setCategoryName("数码");
        existing.setParentId(0L);
        when(trTradeCategoryMapper.checkCategoryNameUnique("数码", 0L)).thenReturn(existing);

        TrTradeCategory dup = new TrTradeCategory();
        dup.setCategoryName("数码");
        dup.setParentId(0L);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trTradeCategoryService.insertTrTradeCategory(dup));
        Assertions.assertTrue(ex.getMessage().contains("名称已存在"));
        verify(trTradeCategoryMapper, never()).insertTrTradeCategory(dup);
    }

    @Test
    void updateShouldRejectParentEqualsSelf()
    {
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryId(10L);
        cat.setParentId(10L);
        cat.setCategoryName("数码");

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trTradeCategoryService.updateTrTradeCategory(cat));
        Assertions.assertTrue(ex.getMessage().contains("父级不能为自身"));
        verify(trTradeCategoryMapper, never()).updateTrTradeCategory(any(TrTradeCategory.class));
    }

    @Test
    void updateShouldRejectMoveUnderParentWhenHasChildren()
    {
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryId(20L);
        cat.setParentId(1L);
        cat.setCategoryName("数码");

        when(trTradeCategoryMapper.countChildrenByCategoryId(20L)).thenReturn(3);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trTradeCategoryService.updateTrTradeCategory(cat));
        Assertions.assertTrue(ex.getMessage().contains("存在子分类时不能改为下级分类"));
        verify(trTradeCategoryMapper, never()).updateTrTradeCategory(any(TrTradeCategory.class));
    }

    @Test
    void updateShouldStampUpdateByAndAllowSameRecordKeepName()
    {
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryId(30L);
        cat.setParentId(0L);
        cat.setCategoryName("书籍");

        TrTradeCategory existing = new TrTradeCategory();
        existing.setCategoryId(30L);
        existing.setCategoryName("书籍");
        existing.setParentId(0L);
        when(trTradeCategoryMapper.checkCategoryNameUnique("书籍", 0L)).thenReturn(existing);
        when(trTradeCategoryMapper.updateTrTradeCategory(cat)).thenReturn(1);

        int rows = trTradeCategoryService.updateTrTradeCategory(cat);

        Assertions.assertEquals(1, rows);
        Assertions.assertEquals(FAKE_ADMIN, cat.getUpdateBy());
        Assertions.assertNotNull(cat.getUpdateTime());
    }

    @Test
    void deleteShouldRejectWhenHasChildren()
    {
        when(trTradeCategoryMapper.countChildrenByCategoryId(40L)).thenReturn(2);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trTradeCategoryService.deleteTrTradeCategoryByCategoryIds(new Long[] { 40L }));
        Assertions.assertTrue(ex.getMessage().contains("子分类"));
        verify(trTradeCategoryMapper, never()).deleteTrTradeCategoryByCategoryIds(any());
    }

    @Test
    void deleteShouldRejectWhenHasGoods()
    {
        when(trTradeCategoryMapper.countChildrenByCategoryId(50L)).thenReturn(0);
        when(trTradeCategoryMapper.countGoodsByCategoryId(50L)).thenReturn(5);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> trTradeCategoryService.deleteTrTradeCategoryByCategoryIds(new Long[] { 50L }));
        Assertions.assertTrue(ex.getMessage().contains("商品"));
        verify(trTradeCategoryMapper, never()).deleteTrTradeCategoryByCategoryIds(any());
    }

    @Test
    void deleteShouldPassWhenNoChildrenAndNoGoods()
    {
        when(trTradeCategoryMapper.countChildrenByCategoryId(60L)).thenReturn(0);
        when(trTradeCategoryMapper.countGoodsByCategoryId(60L)).thenReturn(0);
        Long[] ids = new Long[] { 60L };
        when(trTradeCategoryMapper.deleteTrTradeCategoryByCategoryIds(ids)).thenReturn(1);

        int rows = trTradeCategoryService.deleteTrTradeCategoryByCategoryIds(ids);

        Assertions.assertEquals(1, rows);
        verify(trTradeCategoryMapper).deleteTrTradeCategoryByCategoryIds(ids);
    }

    @Test
    void updateCategoryStatusShouldOnlyTouchStatusAuditFields()
    {
        TrTradeCategory input = new TrTradeCategory();
        input.setCategoryId(70L);
        input.setStatus("1");
        // 故意带个名字，验证状态接口不会把它当成更新内容
        input.setCategoryName("不该被使用");

        ArgumentCaptor<TrTradeCategory> captor = ArgumentCaptor.forClass(TrTradeCategory.class);
        when(trTradeCategoryMapper.updateTrTradeCategory(captor.capture())).thenReturn(1);

        int rows = trTradeCategoryService.updateCategoryStatus(input);

        Assertions.assertEquals(1, rows);
        TrTradeCategory sent = captor.getValue();
        Assertions.assertEquals(70L, sent.getCategoryId());
        Assertions.assertEquals("1", sent.getStatus());
        Assertions.assertEquals(FAKE_ADMIN, sent.getUpdateBy());
        Assertions.assertNotNull(sent.getUpdateTime());
        Assertions.assertNull(sent.getCategoryName());
        Assertions.assertNull(sent.getParentId());
    }

    @Test
    void checkCategoryNameUniqueAllowsSameRecordEdit()
    {
        TrTradeCategory existing = new TrTradeCategory();
        existing.setCategoryId(80L);
        existing.setCategoryName("家电");
        existing.setParentId(0L);
        when(trTradeCategoryMapper.checkCategoryNameUnique(eq("家电"), eq(0L))).thenReturn(existing);

        TrTradeCategory editing = new TrTradeCategory();
        editing.setCategoryId(80L);
        editing.setCategoryName("家电");
        editing.setParentId(0L);
        Assertions.assertTrue(trTradeCategoryService.checkCategoryNameUnique(editing),
                "编辑自身记录应视为唯一");

        TrTradeCategory other = new TrTradeCategory();
        other.setCategoryId(81L);
        other.setCategoryName("家电");
        other.setParentId(0L);
        Assertions.assertFalse(trTradeCategoryService.checkCategoryNameUnique(other),
                "同父下不同 id 同名应视为重复");
    }

    @Test
    void selectAllActiveCategoriesShouldQueryWithStatusZeroAndCacheOnMiss()
    {
        ArgumentCaptor<TrTradeCategory> captor = ArgumentCaptor.forClass(TrTradeCategory.class);
        when(trTradeCategoryMapper.selectTrTradeCategoryList(captor.capture()))
                .thenReturn(Collections.emptyList());

        // 缓存未命中（getCacheObject 默认返回 null）应回源查询并写回缓存
        Assertions.assertTrue(trTradeCategoryService.selectAllActiveCategories().isEmpty());
        Assertions.assertEquals("0", captor.getValue().getStatus());
        verify(redisCache).setCacheObject(eq(ACTIVE_CACHE_KEY), any());
    }

    @Test
    void selectAllActiveCategoriesShouldReturnCachedAndSkipMapperOnHit()
    {
        TrTradeCategory cached = new TrTradeCategory();
        cached.setCategoryId(5L);
        cached.setCategoryName("数码");
        JSONArray cachedArray = new JSONArray();
        cachedArray.add(cached);
        when(redisCache.getCacheObject(ACTIVE_CACHE_KEY)).thenReturn(cachedArray);

        var result = trTradeCategoryService.selectAllActiveCategories();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("数码", result.get(0).getCategoryName());
        verify(trTradeCategoryMapper, never()).selectTrTradeCategoryList(any());
    }

    @Test
    void insertShouldEvictActiveCategoryCache()
    {
        TrTradeCategory cat = new TrTradeCategory();
        cat.setCategoryName("数码");
        cat.setParentId(0L);
        when(trTradeCategoryMapper.checkCategoryNameUnique("数码", 0L)).thenReturn(null);
        when(trTradeCategoryMapper.insertTrTradeCategory(cat)).thenReturn(1);

        trTradeCategoryService.insertTrTradeCategory(cat);

        verify(redisCache).deleteObject(ACTIVE_CACHE_KEY);
    }

    @Test
    void selectListShouldDelegateToMapper()
    {
        TrTradeCategory query = new TrTradeCategory();
        when(trTradeCategoryMapper.selectTrTradeCategoryList(query)).thenReturn(Collections.emptyList());

        Assertions.assertTrue(trTradeCategoryService.selectTrTradeCategoryList(query).isEmpty());
        verify(trTradeCategoryMapper).selectTrTradeCategoryList(query);
    }

    @Test
    void selectByIdShouldDelegateToMapper()
    {
        TrTradeCategory dummy = new TrTradeCategory();
        dummy.setCategoryId(99L);
        when(trTradeCategoryMapper.selectTrTradeCategoryByCategoryId(99L)).thenReturn(dummy);

        Assertions.assertSame(dummy, trTradeCategoryService.selectTrTradeCategoryByCategoryId(99L));
        verify(trTradeCategoryMapper).selectTrTradeCategoryByCategoryId(anyLong());
    }
}
