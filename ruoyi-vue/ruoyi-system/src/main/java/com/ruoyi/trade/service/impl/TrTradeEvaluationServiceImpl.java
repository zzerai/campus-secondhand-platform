package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.domain.TrTradeEvaluation;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.mapper.TrTradeEvaluationMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.ITrTradeEvaluationService;
import com.ruoyi.trade.utils.CreditConstants;

/**
 * 交易评价Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeEvaluationServiceImpl implements ITrTradeEvaluationService 
{
    /** 订单状态：已完成 */
    private static final String ORDER_FINISHED = "3";

    @Autowired
    private TrTradeEvaluationMapper trTradeEvaluationMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 查询交易评价
     * 
     * @param evaluationId 交易评价主键
     * @return 交易评价
     */
    @Override
    public TrTradeEvaluation selectTrTradeEvaluationByEvaluationId(Long evaluationId)
    {
        return trTradeEvaluationMapper.selectTrTradeEvaluationByEvaluationId(evaluationId);
    }

    /**
     * 查询交易评价列表
     * 
     * @param trTradeEvaluation 交易评价
     * @return 交易评价
     */
    @Override
    public List<TrTradeEvaluation> selectTrTradeEvaluationList(TrTradeEvaluation trTradeEvaluation)
    {
        return trTradeEvaluationMapper.selectTrTradeEvaluationList(trTradeEvaluation);
    }

    /**
     * 新增交易评价
     *
     * <p>校验：评分 1-5；订单存在且已完成；评价人是订单买/卖一方；同订单同评价人未评过；
     * 推导 toUserId 为对方角色（避免客户端伪造）。</p>
     */
    @Override
    public int insertTrTradeEvaluation(TrTradeEvaluation trTradeEvaluation)
    {
        validateEvaluation(trTradeEvaluation);
        String username = SecurityUtils.getUsername();
        trTradeEvaluation.setCreateBy(username);
        trTradeEvaluation.setCreateTime(DateUtils.getNowDate());
        trTradeEvaluation.setUpdateBy(username);
        trTradeEvaluation.setUpdateTime(DateUtils.getNowDate());
        int rows = trTradeEvaluationMapper.insertTrTradeEvaluation(trTradeEvaluation);
        if (rows > 0)
        {
            publishCreditByReview(trTradeEvaluation);
        }
        return rows;
    }

    /** 好评(5★) → 被评价人 +1；差评(1-2★) → 被评价人 -1；中评不动分。 */
    private void publishCreditByReview(TrTradeEvaluation evaluation)
    {
        long score = evaluation.getScore();
        int delta;
        String reason;
        if (score >= CreditConstants.GOOD_REVIEW_MIN_SCORE)
        {
            delta = CreditConstants.DELTA_GOOD_REVIEW;
            reason = "收到好评(" + score + "★)";
        }
        else if (score <= CreditConstants.BAD_REVIEW_MAX_SCORE)
        {
            delta = CreditConstants.DELTA_BAD_REVIEW;
            reason = "收到差评(" + score + "★)";
        }
        else
        {
            return;
        }
        eventPublisher.publishEvent(new CreditChangeEvent(evaluation.getToUserId(),
                CreditConstants.TYPE_EVALUATION, delta, CreditConstants.BIZ_EVALUATION,
                evaluation.getEvaluationId(), reason));
    }

    private void validateEvaluation(TrTradeEvaluation evaluation)
    {
        if (evaluation == null)
        {
            throw new ServiceException("评价内容不能为空");
        }
        if (evaluation.getScore() == null || evaluation.getScore() < 1L || evaluation.getScore() > 5L)
        {
            throw new ServiceException("评分必须在 1-5 之间");
        }
        if (evaluation.getOrderId() == null)
        {
            throw new ServiceException("订单ID不能为空");
        }
        if (evaluation.getFromUserId() == null)
        {
            throw new ServiceException("评价人ID不能为空");
        }
        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderId(evaluation.getOrderId());
        if (order == null)
        {
            throw new ServiceException("订单不存在或已删除");
        }
        if (!ORDER_FINISHED.equals(order.getOrderStatus()))
        {
            throw new ServiceException("仅已完成的订单可以评价");
        }
        Long fromUserId = evaluation.getFromUserId();
        boolean isBuyer  = fromUserId.equals(order.getBuyerId());
        boolean isSeller = fromUserId.equals(order.getSellerId());
        if (!isBuyer && !isSeller)
        {
            throw new ServiceException("仅订单买卖双方可以评价");
        }
        // 推导被评价人 = 订单对方；忽略客户端传入的 toUserId，防止伪造
        evaluation.setToUserId(isBuyer ? order.getSellerId() : order.getBuyerId());
        if (trTradeEvaluationMapper.countByOrderAndFromUser(evaluation.getOrderId(), fromUserId) > 0)
        {
            throw new ServiceException("您已对该订单评价过，不能重复评价");
        }
    }

    /**
     * 修改交易评价
     * 
     * @param trTradeEvaluation 交易评价
     * @return 结果
     */
    @Override
    public int updateTrTradeEvaluation(TrTradeEvaluation trTradeEvaluation)
    {
        trTradeEvaluation.setUpdateBy(SecurityUtils.getUsername());
        trTradeEvaluation.setUpdateTime(DateUtils.getNowDate());
        return trTradeEvaluationMapper.updateTrTradeEvaluation(trTradeEvaluation);
    }

    /**
     * 批量删除交易评价
     * 
     * @param evaluationIds 需要删除的交易评价主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeEvaluationByEvaluationIds(Long[] evaluationIds)
    {
        return trTradeEvaluationMapper.deleteTrTradeEvaluationByEvaluationIds(evaluationIds);
    }

    /**
     * 删除交易评价信息
     * 
     * @param evaluationId 交易评价主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeEvaluationByEvaluationId(Long evaluationId)
    {
        return trTradeEvaluationMapper.deleteTrTradeEvaluationByEvaluationId(evaluationId);
    }
}
