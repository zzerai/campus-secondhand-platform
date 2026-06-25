package com.ruoyi.trade.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeDispute;

/**
 * 交易争议处理Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeDisputeMapper 
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
     * 查询用户相关的争议列表（申请人或被申诉人）
     *
     * @param userId 用户ID
     * @return 交易争议处理集合
     */
    public List<TrTradeDispute> selectTrTradeDisputeByUserId(Long userId);

    /**
     * Query pending dispute count.
     *
     * @return pending count
     */
    public Long selectPendingDisputeCount();

    /**
     * 新增交易争议处理
     * 
     * @param trTradeDispute 交易争议处理
     * @return 结果
     */
    public int insertTrTradeDispute(TrTradeDispute trTradeDispute);

    /**
     * 修改交易争议处理
     *
     * @param trTradeDispute 交易争议处理
     * @return 结果
     */
    public int updateTrTradeDispute(TrTradeDispute trTradeDispute);

    /**
     * 人工仲裁争议（更新处理状态、处理人、处理时间、处理结果）
     *
     * @param disputeId 争议ID
     * @param handleStatus 处理状态：3已处理
     * @param handleUserId 处理管理员ID
     * @param handleTime 处理时间
     * @param handleResult 处理结果
     * @param updateBy 更新者
     * @param updateTime 更新时间
     * @return 结果
     */
    public int updateDisputeHandle(@Param("disputeId") Long disputeId,
                                   @Param("handleStatus") String handleStatus,
                                   @Param("handleUserId") Long handleUserId,
                                   @Param("handleTime") Date handleTime,
                                   @Param("handleResult") String handleResult,
                                   @Param("faultParty") String faultParty,
                                   @Param("updateBy") String updateBy,
                                   @Param("updateTime") Date updateTime);

    /**
     * AI 仲裁专用乐观锁更新：仅当 handle_status='0' 时才回写 ai_analysis（成功时同步置 '2'）。
     * 防止 AI 调用过程中管理员人工抢占（已置 '3'）后被 AI 全字段 update 覆盖。
     *
     * @param disputeId 争议ID
     * @param aiAnalysis AI 分析文本，或失败时的 "[AI仲裁失败] ..." 标记
     * @param handleStatus 成功传 "2"（等待人工仲裁）；失败传 null，仅写 ai_analysis 不变更状态
     * @return 影响行数；返回 0 表示已被人工抢占
     */
    public int updateAiAnalysisIfPending(@Param("disputeId") Long disputeId,
                                         @Param("aiAnalysis") String aiAnalysis,
                                         @Param("handleStatus") String handleStatus);

    /**
     * 删除交易争议处理
     * 
     * @param disputeId 交易争议处理主键
     * @return 结果
     */
    public int deleteTrTradeDisputeByDisputeId(Long disputeId);

    /**
     * 批量删除交易争议处理
     * 
     * @param disputeIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeDisputeByDisputeIds(Long[] disputeIds);
}
