package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeDispute;

/**
 * 交易争议处理Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeDisputeService 
{
    /**
     * 查询交易争议处理
     * 
     * @param disputeId 交易争议处理主键
     * @return 交易争议处理
     */
    public TrTradeDispute selectTrTradeDisputeByDisputeId(Long disputeId);

    /**
     * 查询交易争议处理列表
     * 
     * @param trTradeDispute 交易争议处理
     * @return 交易争议处理集合
     */
    public List<TrTradeDispute> selectTrTradeDisputeList(TrTradeDispute trTradeDispute);

    /**
     * 新增交易争议处理
     * 
     * @param trTradeDispute 交易争议处理
     * @return 结果
     */
    public int insertTrTradeDispute(TrTradeDispute trTradeDispute);

    /**
     * 提交争议：先以条件更新将关联订单从“已完成”转入“争议中”，再插入争议记录，两步在同一事务内完成。
     * 服务层会校验订单存在性、状态并防止并发/重复提交；调用方仍需自行校验订单归属。
     *
     * @param trTradeDispute 争议（调用方需已校验订单归属）
     * @throws com.ruoyi.common.exception.ServiceException 订单不存在或状态不可发起争议时抛出
     */
    public void submitDispute(TrTradeDispute trTradeDispute);

    /**
     * 修改交易争议处理
     * 
     * @param trTradeDispute 交易争议处理
     * @return 结果
     */
    public int updateTrTradeDispute(TrTradeDispute trTradeDispute);

    /**
     * 人工仲裁单条争议（处理后置为 3已处理）。
     *
     * @param disputeId 争议ID
     * @param handleResult 仲裁结论
     * @param faultParty 管理员手动判定的责任方（respondent/applicant/both/none，见 CreditConstants.FAULT_*；
     *                   非空且非 none 时扣对应方信用分），可传 null 表示不判责
     * @param refundToBuyer 是否判退款给买家（为真时在同一事务内调支付宝退款，订单争议中→已退款）
     * @return 结果
     */
    public int handleDispute(Long disputeId, String handleResult, String faultParty, boolean refundToBuyer);

    /**
     * 批量人工仲裁争议（跳过已处理或不存在的）
     *
     * @param disputeIds 争议ID集合
     * @param handleResult 仲裁结论
     * @return 成功处理的条数
     */
    public int batchHandleDispute(Long[] disputeIds, String handleResult);

    /**
     * 批量删除交易争议处理
     *
     * @param disputeIds 需要删除的交易争议处理主键集合
     * @return 结果
     */
    public int deleteTrTradeDisputeByDisputeIds(Long[] disputeIds);

    /**
     * 删除交易争议处理信息
     * 
     * @param disputeId 交易争议处理主键
     * @return 结果
     */
    public int deleteTrTradeDisputeByDisputeId(Long disputeId);
}
