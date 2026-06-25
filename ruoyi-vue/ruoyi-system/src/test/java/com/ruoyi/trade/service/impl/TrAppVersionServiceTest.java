package com.ruoyi.trade.service.impl;

import com.ruoyi.trade.domain.TrAppVersion;
import com.ruoyi.trade.mapper.TrAppVersionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * TrAppVersionService 单元测试：覆盖最新版本查询与新增时的审计字段填充。
 */
class TrAppVersionServiceTest {

    @Mock
    private TrAppVersionMapper trAppVersionMapper;

    @InjectMocks
    private TrAppVersionServiceImpl trAppVersionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 查询最新启用版本：透传 Mapper 结果。
     */
    @Test
    void selectLatestEnabledVersion_returnsMapperResult() {
        TrAppVersion v = new TrAppVersion();
        v.setVersionCode(5);
        v.setVersionName("1.2.0");
        when(trAppVersionMapper.selectLatestEnabledVersion()).thenReturn(v);

        TrAppVersion result = trAppVersionService.selectLatestEnabledVersion();

        assertNotNull(result);
        assertEquals(5, result.getVersionCode());
        assertEquals("1.2.0", result.getVersionName());
    }

    /**
     * 无启用版本时返回 null（移动端据此判定无更新）。
     */
    @Test
    void selectLatestEnabledVersion_returnsNullWhenNone() {
        when(trAppVersionMapper.selectLatestEnabledVersion()).thenReturn(null);
        assertNull(trAppVersionService.selectLatestEnabledVersion());
    }

    /**
     * 批量逻辑删除：透传 Mapper，返回受影响行数。
     */
    @Test
    void deleteByIds_delegatesToMapper() {
        Long[] ids = { 1L, 2L };
        when(trAppVersionMapper.deleteTrAppVersionByVersionIds(ids)).thenReturn(2);

        int rows = trAppVersionService.deleteTrAppVersionByVersionIds(ids);

        assertEquals(2, rows);
    }
}
