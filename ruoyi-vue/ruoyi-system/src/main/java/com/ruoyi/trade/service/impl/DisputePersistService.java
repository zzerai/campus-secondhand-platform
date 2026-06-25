package com.ruoyi.trade.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.mapper.TrAiAuditRecordMapper;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;

/**
 * 争议 AI 仲裁结果落库服务：抽到独立 Bean 保证 {@code @Transactional} 走 Spring 代理生效。
 *
 * <p>承担两步原子操作：① 插入 AI 审核记录；② 乐观锁更新争议记录（仅当 handle_status='0' 时回写）。
 * 任一步失败整体回滚，避免出现"有审核记录但争议状态未更新"或反之的中间态。</p>
 *
 * @author daj
 * @date 2026-05-24
 */
@Service
public class DisputePersistService
{
    private static final Logger log = LoggerFactory.getLogger(DisputePersistService.class);

    /** 争议状态：等待人工仲裁（AI 成功后置该状态） */
    private static final String HANDLE_STATUS_WAIT_HUMAN = "2";

    @Autowired
    private TrAiAuditRecordMapper trAiAuditRecordMapper;

    @Autowired
    private TrTradeDisputeMapper trTradeDisputeMapper;

    /**
     * AI 仲裁成功时持久化结果。
     *
     * @return 争议表受影响行数；0 表示争议已被人工抢占（'0' → '3'），此时审核记录已留存但争议状态保持人工结果
     */
    @Transactional(rollbackFor = Exception.class)
    public int persistArbitrationSuccess(TrAiAuditRecord record, Long disputeId, String aiResult)
    {
        trAiAuditRecordMapper.insertTrAiAuditRecord(record);
        int updated = trTradeDisputeMapper.updateAiAnalysisIfPending(
                disputeId, aiResult, HANDLE_STATUS_WAIT_HUMAN);
        if (updated == 0)
        {
            log.warn("AI仲裁回写未生效：争议可能已被人工抢占，disputeId={}", disputeId);
        }
        return updated;
    }
}
