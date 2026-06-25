package com.ruoyi.trade.service;

import com.ruoyi.trade.domain.TrAiAuditRecord;

/**
 * AI争议仲裁Service接口
 *
 * @author daj
 * @date 2026-05-20
 */
public interface ITrAiDisputeArbitrationService
{
    /**
     * 对指定争议进行AI仲裁分析，更新争议记录的ai_analysis字段并写入审核记录表
     *
     * @param disputeId 争议ID
     * @return AI审核记录
     */
    TrAiAuditRecord arbitrateDispute(Long disputeId);

    /**
     * 异步对指定争议进行AI仲裁分析，失败仅记录日志、不抛出，避免阻塞或影响调用方流程。
     *
     * @param disputeId 争议ID
     */
    void arbitrateDisputeAsync(Long disputeId);
}
