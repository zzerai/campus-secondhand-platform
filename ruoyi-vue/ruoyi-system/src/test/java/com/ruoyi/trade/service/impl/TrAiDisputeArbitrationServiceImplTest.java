package com.ruoyi.trade.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrTradeDispute;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.ai.DisputeArbitrationAi;

/**
 * AI 争议仲裁 Service 单元测试：覆盖状态机校验与 AI 失败兜底回写。
 *
 * <p>重构为 langchain4j AiServices 模式后，AI 调用契约从底层 {@code ChatModel.chat}
 * 改为声明式 {@link DisputeArbitrationAi#arbitrate}，本测试相应改 mock 对象。</p>
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class TrAiDisputeArbitrationServiceImplTest
{
    @Mock private TrTradeDisputeMapper trTradeDisputeMapper;

    @Mock private TrTradeOrderMapper trTradeOrderMapper;

    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;

    @Mock private DisputeArbitrationAi disputeArbitrationAi;

    @Mock private DisputePersistService disputePersistService;

    @InjectMocks
    private TrAiDisputeArbitrationServiceImpl service;

    /**
     * 状态机校验：handleStatus 非 '0' 时拒绝 AI 仲裁，防止重复仲裁覆盖人工结果。
     */
    @Test
    void arbitrateDispute_whenStatusNotPending_throws()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setHandleStatus("3"); // 已处理
        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);

        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.arbitrateDispute(1L));
        Assertions.assertTrue(ex.getMessage().contains("不可AI仲裁"));

        // 状态不合法：绝不进 AI 调用、绝不持久化
        verify(disputeArbitrationAi, never()).arbitrate(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(disputePersistService, never())
                .persistArbitrationSuccess(any(), any(), any());
    }

    /**
     * 失败兜底：AI 调用抛异常时 status 保持 '0'，仅把"[AI仲裁失败] xxx"写入 ai_analysis；
     * 不写审核记录，方便管理员后续从 /trade/ai/audit/dispute 重试。
     */
    @Test
    void arbitrateDisputeAsync_whenAiThrows_writesErrorMessageAndKeepStatus()
    {
        TrTradeDispute exist = new TrTradeDispute();
        exist.setDisputeId(1L);
        exist.setOrderId(100L);
        exist.setHandleStatus("0");
        exist.setDisputeType("商品不符");
        exist.setDisputeContent("收到的商品与描述不符");
        TrTradeOrder order = new TrTradeOrder();
        order.setOrderId(100L);
        order.setOrderStatus("5");

        when(trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(1L)).thenReturn(exist);
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(100L)).thenReturn(order);
        when(disputeArbitrationAi.arbitrate(
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("模拟AI接口宕机"));

        // arbitrateDisputeAsync 会吞掉异常（仅打日志），自身不抛
        service.arbitrateDisputeAsync(1L);

        // 失败兜底：恰好回写一次 ai_analysis，状态不变（handleStatus 传 null）
        verify(trTradeDisputeMapper, times(1)).updateAiAnalysisIfPending(
                eq(1L),
                argThat(msg -> msg != null && msg.startsWith("[AI仲裁失败]")
                        && msg.contains("模拟AI接口宕机")),
                isNull());
        // 失败时不应触发任何成功持久化路径
        verify(disputePersistService, never())
                .persistArbitrationSuccess(any(), any(), any());
    }
}
