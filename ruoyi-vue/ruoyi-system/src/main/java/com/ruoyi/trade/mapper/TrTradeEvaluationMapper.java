package com.ruoyi.trade.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeEvaluation;

/**
 * 交易评价Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeEvaluationMapper 
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
     * 删除交易评价
     * 
     * @param evaluationId 交易评价主键
     * @return 结果
     */
    public int deleteTrTradeEvaluationByEvaluationId(Long evaluationId);

    /**
     * 批量删除交易评价
     *
     * @param evaluationIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeEvaluationByEvaluationIds(Long[] evaluationIds);

    /**
     * 校验同一订单下某用户是否已经评价过。
     *
     * @param orderId    订单ID
     * @param fromUserId 评价人ID
     * @return 命中行数（&gt;0 表示已评价）
     */
    public int countByOrderAndFromUser(@Param("orderId") Long orderId,
                                       @Param("fromUserId") Long fromUserId);

    /**
     * 查询用户收到的评价（含用户昵称头像、订单商品信息）。
     */
    public List<com.ruoyi.trade.domain.vo.AppEvaluationVo> selectReceivedByUserId(@Param("userId") Long userId);

    /**
     * 查询用户发出的评价（含用户昵称头像、订单商品信息）。
     */
    public List<com.ruoyi.trade.domain.vo.AppEvaluationVo> selectSentByUserId(@Param("userId") Long userId);

    /**
     * 查询用户收到的评分分布（各星级数量）。
     */
    public List<java.util.Map<String, Object>> scoreDistributionByUserId(@Param("userId") Long userId);

    /**
     * 查询用户收到的平均分。
     */
    public java.math.BigDecimal avgScoreByUserId(@Param("userId") Long userId);
}
