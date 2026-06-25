package com.ruoyi.trade.service.impl;

import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.mapper.TrAiAuditRecordMapper;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DisputePersistService 单元测试
 */
class DisputePersistServiceTest {

    @Mock
    private TrAiAuditRecordMapper trAiAuditRecordMapper;

    @Mock
    private TrTradeDisputeMapper trTradeDisputeMapper;

    @InjectMocks
    private DisputePersistService disputePersistService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试正常流程：插入审核记录 + 更新争议状态成功
     */
    @Test
    void testPersistArbitrationSuccess_NormalFlow() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setRecordId(1L);
        record.setBusinessId(100L);
        record.setRiskLevel("low");
        record.setSuggestion("通过");

        Long disputeId = 100L;
        String aiResult = "{\"riskLevel\":\"low\",\"suggestion\":\"通过\"}";

        // Mock Mapper 行为
        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(1);
        when(trTradeDisputeMapper.updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), anyString()))
                .thenReturn(1);

        // 执行测试
        int result = disputePersistService.persistArbitrationSuccess(record, disputeId, aiResult);

        // 验证结果
        assertEquals(1, result);
        // 验证审核记录被插入
        verify(trAiAuditRecordMapper, times(1)).insertTrAiAuditRecord(record);
        // 验证争议状态被更新
        verify(trTradeDisputeMapper, times(1))
                .updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), eq("2"));
    }

    /**
     * 测试争议已被人工抢占：更新返回0
     */
    @Test
    void testPersistArbitrationSuccess_DisputeAlreadyHandled() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setRecordId(2L);
        record.setBusinessId(200L);

        Long disputeId = 200L;
        String aiResult = "{\"riskLevel\":\"high\",\"suggestion\":\"拒绝\"}";

        // Mock：审核记录插入成功，但争议已被人工处理（乐观锁返回0）
        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(1);
        when(trTradeDisputeMapper.updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), anyString()))
                .thenReturn(0);

        // 执行测试
        int result = disputePersistService.persistArbitrationSuccess(record, disputeId, aiResult);

        // 验证结果：返回0表示争议未被更新（已被人工抢占）
        assertEquals(0, result);
        // 审核记录仍然被插入了
        verify(trAiAuditRecordMapper, times(1)).insertTrAiAuditRecord(record);
        verify(trTradeDisputeMapper, times(1))
                .updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), eq("2"));
    }

    /**
     * 测试插入审核记录失败：抛异常
     */
    @Test
    void testPersistArbitrationSuccess_InsertFailed() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        Long disputeId = 300L;
        String aiResult = "ai result";

        // Mock：插入审核记录失败
        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class)))
                .thenThrow(new RuntimeException("数据库异常"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            disputePersistService.persistArbitrationSuccess(record, disputeId, aiResult);
        });

        // 验证：插入失败后，争议更新不应该被调用
        verify(trTradeDisputeMapper, never())
                .updateAiAnalysisIfPending(any(), anyString(), anyString());
    }

    /**
     * 测试更新争议状态失败：抛异常
     */
    @Test
    void testPersistArbitrationSuccess_UpdateFailed() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        Long disputeId = 400L;
        String aiResult = "ai result";

        // Mock：插入成功，但更新失败
        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(1);
        when(trTradeDisputeMapper.updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), anyString()))
                .thenThrow(new RuntimeException("数据库异常"));

        // 执行测试并验证异常
        assertThrows(RuntimeException.class, () -> {
            disputePersistService.persistArbitrationSuccess(record, disputeId, aiResult);
        });

        // 验证两个操作都被调用了（事务回滚由 Spring 处理）
        verify(trAiAuditRecordMapper, times(1)).insertTrAiAuditRecord(record);
        verify(trTradeDisputeMapper, times(1))
                .updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), anyString());
    }

    /**
     * 测试参数传递：验证 disputeId 和 aiResult 正确传递给 Mapper
     */
    @Test
    void testPersistArbitrationSuccess_CorrectParameters() {
        // 准备数据
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setBusinessId(500L);

        Long disputeId = 500L;
        String aiResult = "specific-ai-result";

        when(trAiAuditRecordMapper.insertTrAiAuditRecord(any(TrAiAuditRecord.class))).thenReturn(1);
        when(trTradeDisputeMapper.updateAiAnalysisIfPending(eq(disputeId), eq(aiResult), anyString()))
                .thenReturn(1);

        // 执行测试
        disputePersistService.persistArbitrationSuccess(record, disputeId, aiResult);

        // 验证参数正确传递
        verify(trTradeDisputeMapper, times(1))
                .updateAiAnalysisIfPending(eq(500L), eq("specific-ai-result"), eq("2"));
    }
}