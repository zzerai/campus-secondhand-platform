package com.ruoyi.trade.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrTradeOrderLog;
import com.ruoyi.trade.mapper.TrTradeOrderLogMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TrTradeOrderLogServiceImpl 单元测试
 */
class TrTradeOrderLogServiceImplTest {

    @Mock
    private TrTradeOrderLogMapper trTradeOrderLogMapper;

    @InjectMocks
    private TrTradeOrderLogServiceImpl trTradeOrderLogService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockedStatic<DateUtils> dateUtilsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("testUser");
        dateUtilsMock = mockStatic(DateUtils.class);
        dateUtilsMock.when(DateUtils::getNowDate).thenReturn(new Date());
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
        dateUtilsMock.close();
    }

    @Test
    void testSelectTrTradeOrderLogByLogId() {
        TrTradeOrderLog orderLog = new TrTradeOrderLog();
        orderLog.setLogId(1L);
        when(trTradeOrderLogMapper.selectTrTradeOrderLogByLogId(1L)).thenReturn(orderLog);

        TrTradeOrderLog result = trTradeOrderLogService.selectTrTradeOrderLogByLogId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getLogId());
    }

    @Test
    void testSelectTrTradeOrderLogList() {
        TrTradeOrderLog queryParam = new TrTradeOrderLog();
        TrTradeOrderLog log1 = new TrTradeOrderLog();
        log1.setLogId(1L);
        TrTradeOrderLog log2 = new TrTradeOrderLog();
        log2.setLogId(2L);

        when(trTradeOrderLogMapper.selectTrTradeOrderLogList(queryParam))
                .thenReturn(Arrays.asList(log1, log2));

        List<TrTradeOrderLog> result = trTradeOrderLogService.selectTrTradeOrderLogList(queryParam);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testInsertTrTradeOrderLog() {
        TrTradeOrderLog orderLog = new TrTradeOrderLog();
        when(trTradeOrderLogMapper.insertTrTradeOrderLog(any(TrTradeOrderLog.class))).thenReturn(1);

        int result = trTradeOrderLogService.insertTrTradeOrderLog(orderLog);

        assertEquals(1, result);
        assertEquals("testUser", orderLog.getCreateBy());
        assertEquals("testUser", orderLog.getUpdateBy());
        assertNotNull(orderLog.getCreateTime());
        assertNotNull(orderLog.getUpdateTime());
    }

    @Test
    void testUpdateTrTradeOrderLog() {
        TrTradeOrderLog orderLog = new TrTradeOrderLog();
        orderLog.setLogId(1L);
        when(trTradeOrderLogMapper.updateTrTradeOrderLog(any(TrTradeOrderLog.class))).thenReturn(1);

        int result = trTradeOrderLogService.updateTrTradeOrderLog(orderLog);

        assertEquals(1, result);
        assertEquals("testUser", orderLog.getUpdateBy());
        assertNotNull(orderLog.getUpdateTime());
    }

    @Test
    void testDeleteTrTradeOrderLogByLogIds() {
        Long[] logIds = {1L, 2L, 3L};
        when(trTradeOrderLogMapper.deleteTrTradeOrderLogByLogIds(logIds)).thenReturn(3);

        int result = trTradeOrderLogService.deleteTrTradeOrderLogByLogIds(logIds);

        assertEquals(3, result);
    }

    @Test
    void testDeleteTrTradeOrderLogByLogId() {
        when(trTradeOrderLogMapper.deleteTrTradeOrderLogByLogId(1L)).thenReturn(1);

        int result = trTradeOrderLogService.deleteTrTradeOrderLogByLogId(1L);

        assertEquals(1, result);
    }

    @Test
    void testSelectTrTradeOrderLogList_Empty() {
        TrTradeOrderLog queryParam = new TrTradeOrderLog();
        when(trTradeOrderLogMapper.selectTrTradeOrderLogList(queryParam))
                .thenReturn(Collections.emptyList());

        List<TrTradeOrderLog> result = trTradeOrderLogService.selectTrTradeOrderLogList(queryParam);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectTrTradeOrderLogByLogId_NotFound() {
        when(trTradeOrderLogMapper.selectTrTradeOrderLogByLogId(999L)).thenReturn(null);

        TrTradeOrderLog result = trTradeOrderLogService.selectTrTradeOrderLogByLogId(999L);

        assertNull(result);
    }
}