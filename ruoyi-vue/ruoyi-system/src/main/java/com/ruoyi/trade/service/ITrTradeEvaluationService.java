package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeEvaluation;

/**
 * 交易评价Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeEvaluationService 
{
    /**
     * 查询交易评价
     * 
     * @param evaluationId 交易评价主键
     * @return 交易评价
     */
    public TrTradeEvaluation selectTrTradeEvaluationByEvaluationId(Long evaluationId);

    /**
     * 查询交易评价列表
     * 
     * @param trTradeEvaluation 交易评价
     * @return 交易评价集合
     */
    public List<TrTradeEvaluation> selectTrTradeEvaluationList(TrTradeEvaluation trTradeEvaluation);

    /**
     * 新增交易评价
     * 
     * @param trTradeEvaluation 交易评价
     * @return 结果
     */
    public int insertTrTradeEvaluation(TrTradeEvaluation trTradeEvaluation);

    /**
     * 修改交易评价
     * 
     * @param trTradeEvaluation 交易评价
     * @return 结果
     */
    public int updateTrTradeEvaluation(TrTradeEvaluation trTradeEvaluation);

    /**
     * 批量删除交易评价
     * 
     * @param evaluationIds 需要删除的交易评价主键集合
     * @return 结果
     */
    public int deleteTrTradeEvaluationByEvaluationIds(Long[] evaluationIds);

    /**
     * 删除交易评价信息
     * 
     * @param evaluationId 交易评价主键
     * @return 结果
     */
    public int deleteTrTradeEvaluationByEvaluationId(Long evaluationId);
}
