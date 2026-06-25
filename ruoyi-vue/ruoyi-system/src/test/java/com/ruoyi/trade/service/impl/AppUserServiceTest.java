package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.vo.AppUserHomepageVo;
import com.ruoyi.trade.mapper.TrTradeEvaluationMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.ITrStudentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * AppUserService 公开主页聚合单元测试
 */
class AppUserServiceTest {

    @Mock
    private ITrStudentUserService studentUserService;

    @Mock
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Mock
    private TrTradeEvaluationMapper trTradeEvaluationMapper;

    @InjectMocks
    private AppUserServiceImpl appUserService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Map<String, Object> dist(int score, int cnt) {
        Map<String, Object> m = new HashMap<>();
        m.put("score", score);
        m.put("cnt", cnt);
        return m;
    }

    /**
     * 正常聚合：基本信息 + 在售/已售数 + 均分 + 好评率（4~5 星占比）。
     */
    @Test
    void testGetUserHomepage_NormalAggregation() {
        TrStudentUser user = new TrStudentUser();
        user.setUserId(7L);
        user.setNickname("小明");
        user.setAvatar("/avatar/a.png");
        user.setCreditScore(95L);
        when(studentUserService.selectTrStudentUserByUserId(7L)).thenReturn(user);

        when(trTradeGoodsMapper.countSellerGoodsByStatus(eq(7L), eq("1"))).thenReturn(3L);
        when(trTradeGoodsMapper.countSellerGoodsByStatus(eq(7L), eq("4"))).thenReturn(5L);

        when(trTradeEvaluationMapper.avgScoreByUserId(7L)).thenReturn(new BigDecimal("4.6"));
        List<Map<String, Object>> dist = new ArrayList<>();
        dist.add(dist(5, 7)); // 好评
        dist.add(dist(4, 1)); // 好评
        dist.add(dist(2, 2)); // 差评
        when(trTradeEvaluationMapper.scoreDistributionByUserId(7L)).thenReturn(dist);

        AppUserHomepageVo vo = appUserService.getUserHomepage(7L);

        assertNotNull(vo);
        assertEquals(7L, vo.getUserId());
        assertEquals("小明", vo.getNickname());
        assertEquals(95L, vo.getCreditScore());
        assertEquals(3L, vo.getOnSaleCount());
        assertEquals(5L, vo.getSoldCount());
        assertEquals(4.6, vo.getAverageScore());
        assertEquals(10, vo.getTotalReceived());
        // 8/10 = 80%
        assertEquals(80, vo.getGoodRate());
    }

    /**
     * 无评价时：均分 0、好评率 0、总数 0，不抛异常。
     */
    @Test
    void testGetUserHomepage_NoEvaluations() {
        TrStudentUser user = new TrStudentUser();
        user.setUserId(9L);
        user.setNickname("新人");
        when(studentUserService.selectTrStudentUserByUserId(9L)).thenReturn(user);
        when(trTradeGoodsMapper.countSellerGoodsByStatus(eq(9L), eq("1"))).thenReturn(0L);
        when(trTradeGoodsMapper.countSellerGoodsByStatus(eq(9L), eq("4"))).thenReturn(0L);
        when(trTradeEvaluationMapper.avgScoreByUserId(9L)).thenReturn(null);
        when(trTradeEvaluationMapper.scoreDistributionByUserId(9L)).thenReturn(new ArrayList<>());

        AppUserHomepageVo vo = appUserService.getUserHomepage(9L);

        assertNotNull(vo);
        assertEquals(0.0, vo.getAverageScore());
        assertEquals(0, vo.getGoodRate());
        assertEquals(0, vo.getTotalReceived());
    }

    /**
     * 用户不存在：返回 null。
     */
    @Test
    void testGetUserHomepage_UserNotFound() {
        when(studentUserService.selectTrStudentUserByUserId(404L)).thenReturn(null);
        assertNull(appUserService.getUserHomepage(404L));
    }

    /**
     * userId 为 null：返回 null，不查库。
     */
    @Test
    void testGetUserHomepage_NullUserId() {
        assertNull(appUserService.getUserHomepage(null));
    }
}
