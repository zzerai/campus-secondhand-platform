package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.vo.ai.GoodsAuditResult;
import com.ruoyi.trade.mapper.TrAiAuditRecordMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.ai.GoodsAuditAi;

/**
 * AI 商品审核 Service 单元测试。
 *
 * <p>覆盖：商品不存在拒绝 / AI 异常透传 / AI 字段缺失走兜底默认值 / 落库字段完整。
 * 重构为 langchain4j AiServices 模式后，AI 调用契约改为 {@link GoodsAuditAi#audit}。</p>
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrAiGoodsAuditServiceImplTest
{
    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;
    @Mock private TrAiAuditRecordMapper trAiAuditRecordMapper;
    @Mock private GoodsAuditAi goodsAuditAi;

    @InjectMocks
    private TrAiGoodsAuditServiceImpl service;

    @Test
    void auditGoods_whenGoodsNotExist_throws()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(99L)).thenReturn(null);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(99L));
        Assertions.assertTrue(ex.getMessage().contains("商品不存在"));

        verify(goodsAuditAi, never()).audit(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString());
        verify(trAiAuditRecordMapper, never()).insertTrAiAuditRecord(any());
    }

    @Test
    void auditGoods_whenAiThrows_propagatesAsServiceException()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(sampleGoods());
        when(goodsAuditAi.audit(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("模拟AI接口宕机"));

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.auditGoods(1L));
        Assertions.assertTrue(ex.getMessage().startsWith("AI审核"),
                "AiCallExecutor 应把下游异常包装为 ServiceException 并带 'AI审核' 前缀");
        // AI 失败时绝不落审核记录
        verify(trAiAuditRecordMapper, never()).insertTrAiAuditRecord(any());
    }

    @Test
    void auditGoods_whenAiReturnsBlankFields_writesDefaults()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(sampleGoods());
        GoodsAuditResult ai = new GoodsAuditResult();   // 三字段全 null
        when(goodsAuditAi.audit(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(ai);

        TrAiAuditRecord record = service.auditGoods(1L);

        // 兜底默认值
        Assertions.assertEquals("middle",   record.getRiskLevel());
        Assertions.assertEquals("人工复核", record.getSuggestion());
        Assertions.assertEquals("AI未提供明确风险原因", record.getRiskReason());
        // 关键属性
        Assertions.assertEquals(1L, record.getBusinessId());
        Assertions.assertEquals("goods", record.getBusinessType());
        Assertions.assertEquals("0", record.getDelFlag());
        Assertions.assertNotNull(record.getCreateTime());
        Assertions.assertNotNull(record.getInputContent(),
                "input_content 必须留快照，便于管理员回溯 AI 看到的字段");
        verify(trAiAuditRecordMapper, times(1)).insertTrAiAuditRecord(record);
    }

    @Test
    void auditGoods_whenAiReturnsValidResult_persistsRealValues()
    {
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(1L)).thenReturn(sampleGoods());
        GoodsAuditResult ai = new GoodsAuditResult();
        ai.setRiskLevel("low");
        ai.setSuggestion("通过");
        ai.setRiskReason("价格合理、信息完整");
        when(goodsAuditAi.audit(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString()))
                .thenReturn(ai);

        ArgumentCaptor<TrAiAuditRecord> captor = ArgumentCaptor.forClass(TrAiAuditRecord.class);
        service.auditGoods(1L);
        verify(trAiAuditRecordMapper).insertTrAiAuditRecord(captor.capture());

        TrAiAuditRecord saved = captor.getValue();
        Assertions.assertEquals("low", saved.getRiskLevel());
        Assertions.assertEquals("通过", saved.getSuggestion());
        Assertions.assertEquals("价格合理、信息完整", saved.getRiskReason());
        Assertions.assertNotNull(saved.getAiResult(), "应序列化 POJO 写入 ai_result");
        Assertions.assertTrue(saved.getAiResult().contains("\"riskLevel\":\"low\""));
    }

    private TrTradeGoods sampleGoods()
    {
        TrTradeGoods g = new TrTradeGoods();
        g.setGoodsId(1L);
        g.setTitle("二手考研书");
        g.setCategoryName("书籍");
        g.setPrice(new BigDecimal("30.00"));
        g.setOriginalPrice(new BigDecimal("80.00"));
        g.setQuality("九成新");
        g.setDescription("无笔记，几乎全新");
        g.setTradePlace("图书馆门口");
        return g;
    }
}
