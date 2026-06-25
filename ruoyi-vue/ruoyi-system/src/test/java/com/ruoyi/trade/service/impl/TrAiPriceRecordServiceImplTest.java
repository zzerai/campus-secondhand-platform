package com.ruoyi.trade.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrAiPriceRecord;
import com.ruoyi.trade.mapper.TrAiPriceRecordMapper;
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
 * TrAiPriceRecordServiceImpl 单元测试
 */
class TrAiPriceRecordServiceImplTest {

    @Mock
    private TrAiPriceRecordMapper trAiPriceRecordMapper;

    @InjectMocks
    private TrAiPriceRecordServiceImpl trAiPriceRecordService;

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
    void testSelectTrAiPriceRecordByRecordId() {
        TrAiPriceRecord record = new TrAiPriceRecord();
        record.setRecordId(1L);
        when(trAiPriceRecordMapper.selectTrAiPriceRecordByRecordId(1L)).thenReturn(record);

        TrAiPriceRecord result = trAiPriceRecordService.selectTrAiPriceRecordByRecordId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getRecordId());
    }

    @Test
    void testSelectTrAiPriceRecordList() {
        TrAiPriceRecord queryParam = new TrAiPriceRecord();
        TrAiPriceRecord record1 = new TrAiPriceRecord();
        record1.setRecordId(1L);
        TrAiPriceRecord record2 = new TrAiPriceRecord();
        record2.setRecordId(2L);

        when(trAiPriceRecordMapper.selectTrAiPriceRecordList(queryParam))
                .thenReturn(Arrays.asList(record1, record2));

        List<TrAiPriceRecord> result = trAiPriceRecordService.selectTrAiPriceRecordList(queryParam);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testInsertTrAiPriceRecord() {
        TrAiPriceRecord record = new TrAiPriceRecord();
        when(trAiPriceRecordMapper.insertTrAiPriceRecord(any(TrAiPriceRecord.class))).thenReturn(1);

        int result = trAiPriceRecordService.insertTrAiPriceRecord(record);

        assertEquals(1, result);
        assertEquals("testUser", record.getCreateBy());
        assertEquals("testUser", record.getUpdateBy());
        assertNotNull(record.getCreateTime());
        assertNotNull(record.getUpdateTime());
    }

    @Test
    void testUpdateTrAiPriceRecord() {
        TrAiPriceRecord record = new TrAiPriceRecord();
        record.setRecordId(1L);
        when(trAiPriceRecordMapper.updateTrAiPriceRecord(any(TrAiPriceRecord.class))).thenReturn(1);

        int result = trAiPriceRecordService.updateTrAiPriceRecord(record);

        assertEquals(1, result);
        assertEquals("testUser", record.getUpdateBy());
        assertNotNull(record.getUpdateTime());
    }

    @Test
    void testDeleteTrAiPriceRecordByRecordIds() {
        Long[] recordIds = {1L, 2L, 3L};
        when(trAiPriceRecordMapper.deleteTrAiPriceRecordByRecordIds(recordIds)).thenReturn(3);

        int result = trAiPriceRecordService.deleteTrAiPriceRecordByRecordIds(recordIds);

        assertEquals(3, result);
    }

    @Test
    void testDeleteTrAiPriceRecordByRecordId() {
        when(trAiPriceRecordMapper.deleteTrAiPriceRecordByRecordId(1L)).thenReturn(1);

        int result = trAiPriceRecordService.deleteTrAiPriceRecordByRecordId(1L);

        assertEquals(1, result);
    }

    @Test
    void testSelectTrAiPriceRecordList_Empty() {
        TrAiPriceRecord queryParam = new TrAiPriceRecord();
        when(trAiPriceRecordMapper.selectTrAiPriceRecordList(queryParam))
                .thenReturn(Collections.emptyList());

        List<TrAiPriceRecord> result = trAiPriceRecordService.selectTrAiPriceRecordList(queryParam);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectTrAiPriceRecordByRecordId_NotFound() {
        when(trAiPriceRecordMapper.selectTrAiPriceRecordByRecordId(999L)).thenReturn(null);

        TrAiPriceRecord result = trAiPriceRecordService.selectTrAiPriceRecordByRecordId(999L);

        assertNull(result);
    }
}