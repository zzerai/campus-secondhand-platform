package com.ruoyi.trade.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.mapper.TrAiAuditRecordMapper;
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
 * TrAiAuditRecordServiceImpl 单元测试
 */
class TrAiAuditRecordServiceImplTest {

    @Mock
    private TrAiAuditRecordMapper trAiAuditRecordMapper;

    @InjectMocks
    private TrAiAuditRecordServiceImpl trAiAuditRecordService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockedStatic<DateUtils> dateUtilsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock SecurityUtils 静态方法
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("testUser");
        
        // Mock DateUtils 静态方法
        dateUtilsMock = mockStatic(DateUtils.class);
        dateUtilsMock.when(DateUtils::getNowDate).thenReturn(new Date());
    }

    @AfterEach
    void tearDown() {
        // 释放静态 Mock
        securityUtilsMock.close();
        dateUtilsMock.close();
    }

    /**
     * 测试根据ID查询AI审核记录
     */
    @Test
    void testSelectTrAiAuditRecordByRecordId() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setRecordId(1L);
        
        when(trAiAuditRecordMapper.selectTrAiAuditRecordByRecordId(1L)).thenReturn(record);
        
        // 执行测试
        TrAiAuditRecord result = trAiAuditRecordService.selectTrAiAuditRecordByRecordId(1L);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getRecordId());
        verify(trAiAuditRecordMapper, times(1)).selectTrAiAuditRecordByRecordId(1L);
    }

    /**
     * 测试查询AI审核记录列表
     */
    @Test
    void testSelectTrAiAuditRecordList() {
        // 准备数据
        TrAiAuditRecord queryParam = new TrAiAuditRecord();
        
        TrAiAuditRecord record1 = new TrAiAuditRecord();
        record1.setRecordId(1L);
        TrAiAuditRecord record2 = new TrAiAuditRecord();
        record2.setRecordId(2L);
        
        when(trAiAuditRecordMapper.selectTrAiAuditRecordList(queryParam))
                .thenReturn(Arrays.asList(record1, record2));
        
        // 执行测试
        List<TrAiAuditRecord> result = trAiAuditRecordService.selectTrAiAuditRecordList(queryParam);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(trAiAuditRecordMapper, times(1)).selectTrAiAuditRecordList(queryParam);
    }

    /**
     * 测试新增AI审核记录
     */
    @Test
    void testInsertTrAiAuditRecord() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        
        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(1);
        
        // 执行测试
        int result = trAiAuditRecordService.insertTrAiAuditRecord(record);
        
        // 验证结果
        assertEquals(1, result);
        // 验证 Service 层自动填充了创建人和创建时间
        assertEquals("testUser", record.getCreateBy());
        assertEquals("testUser", record.getUpdateBy());
        assertNotNull(record.getCreateTime());
        assertNotNull(record.getUpdateTime());
        verify(trAiAuditRecordMapper, times(1)).insertTrAiAuditRecord(record);
    }

    /**
     * 测试修改AI审核记录
     */
    @Test
    void testUpdateTrAiAuditRecord() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setRecordId(1L);
        
        when(trAiAuditRecordMapper.updateTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(1);
        
        // 执行测试
        int result = trAiAuditRecordService.updateTrAiAuditRecord(record);
        
        // 验证结果
        assertEquals(1, result);
        // 验证更新人和更新时间被自动填充
        assertEquals("testUser", record.getUpdateBy());
        assertNotNull(record.getUpdateTime());
        verify(trAiAuditRecordMapper, times(1)).updateTrAiAuditRecord(record);
    }

    /**
     * 测试批量删除AI审核记录
     */
    @Test
    void testDeleteTrAiAuditRecordByRecordIds() {
        // 准备数据
        Long[] recordIds = {1L, 2L, 3L};
        
        when(trAiAuditRecordMapper.deleteTrAiAuditRecordByRecordIds(recordIds)).thenReturn(3);
        
        // 执行测试
        int result = trAiAuditRecordService.deleteTrAiAuditRecordByRecordIds(recordIds);
        
        // 验证结果
        assertEquals(3, result);
        verify(trAiAuditRecordMapper, times(1)).deleteTrAiAuditRecordByRecordIds(recordIds);
    }

    /**
     * 测试删除单个AI审核记录
     */
    @Test
    void testDeleteTrAiAuditRecordByRecordId() {
        // 准备数据
        Long recordId = 1L;
        
        when(trAiAuditRecordMapper.deleteTrAiAuditRecordByRecordId(recordId)).thenReturn(1);
        
        // 执行测试
        int result = trAiAuditRecordService.deleteTrAiAuditRecordByRecordId(recordId);
        
        // 验证结果
        assertEquals(1, result);
        verify(trAiAuditRecordMapper, times(1)).deleteTrAiAuditRecordByRecordId(recordId);
    }

    /**
     * 测试查询空列表
     */
    @Test
    void testSelectTrAiAuditRecordList_Empty() {
        // 准备数据
        TrAiAuditRecord queryParam = new TrAiAuditRecord();
        
        when(trAiAuditRecordMapper.selectTrAiAuditRecordList(queryParam))
                .thenReturn(Collections.emptyList());
        
        // 执行测试
        List<TrAiAuditRecord> result = trAiAuditRecordService.selectTrAiAuditRecordList(queryParam);
        
        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(trAiAuditRecordMapper, times(1)).selectTrAiAuditRecordList(queryParam);
    }

    /**
     * 测试查询不存在的记录
     */
    @Test
    void testSelectTrAiAuditRecordByRecordId_NotFound() {
        // 准备数据
        when(trAiAuditRecordMapper.selectTrAiAuditRecordByRecordId(999L)).thenReturn(null);
        
        // 执行测试
        TrAiAuditRecord result = trAiAuditRecordService.selectTrAiAuditRecordByRecordId(999L);
        
        // 验证结果
        assertNull(result);
        verify(trAiAuditRecordMapper, times(1)).selectTrAiAuditRecordByRecordId(999L);
    }

    /**
     * 测试新增返回0的情况
     */
    @Test
    void testInsertTrAiAuditRecord_ReturnZero() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        
        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(0);
        
        // 执行测试
        int result = trAiAuditRecordService.insertTrAiAuditRecord(record);
        
        // 验证结果
        assertEquals(0, result);
        // 即使返回0，字段仍然应该被填充
        assertEquals("testUser", record.getCreateBy());
    }

    /**
     * 测试修改返回0的情况（记录不存在）
     */
    @Test
    void testUpdateTrAiAuditRecord_ReturnZero() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setRecordId(999L);
        
        when(trAiAuditRecordMapper.updateTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(0);
        
        // 执行测试
        int result = trAiAuditRecordService.updateTrAiAuditRecord(record);
        
        // 验证结果
        assertEquals(0, result);
        assertEquals("testUser", record.getUpdateBy());
    }
}