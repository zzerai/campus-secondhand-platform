package com.ruoyi.trade.service.impl;

import java.util.Date;
import java.util.List;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.event.DisputeSubmittedEvent;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.domain.TrTradeDispute;
import com.ruoyi.trade.service.IAppOrderService;
import com.ruoyi.trade.service.ITrTradeDisputeService;
import com.ruoyi.trade.utils.CreditConstants;

/**
 * 交易争议处理Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeDisputeServiceImpl implements ITrTradeDisputeService
{
    @Autowired
    private TrTradeDisputeMapper trTradeDisputeMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private IAppOrderService appOrderService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 查询交易争议处理
     * 
     * @param disputeId 交易争议处理主键
     * @return 交易争议处理
     */
    @Override
    public TrTradeDispute selectTrTradeDisputeByDisputeId(Long disputeId)
    {
        return trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(disputeId);
    }

    /**
     * 查询交易争议处理列表
     * 
     * @param trTradeDispute 交易争议处理
     * @return 交易争议处理
     */
    @Override
    public List<TrTradeDispute> selectTrTradeDisputeList(TrTradeDispute trTradeDispute)
    {
        return trTradeDisputeMapper.selectTrTradeDisputeList(trTradeDispute);
    }

    /**
     * 新增交易争议处理
     * 
     * @param trTradeDispute 交易争议处理
     * @return 结果
     */
    @Override
    public int insertTrTradeDispute(TrTradeDispute trTradeDispute)
    {
        String username = SecurityUtils.getUsername();
        trTradeDispute.setCreateBy(username);
        trTradeDispute.setCreateTime(DateUtils.getNowDate());
        trTradeDispute.setUpdateBy(username);
        trTradeDispute.setUpdateTime(DateUtils.getNowDate());
        return trTradeDisputeMapper.insertTrTradeDispute(trTradeDispute);
    }

    /**
     * 提交争议：先以条件更新将关联订单从“已完成”抢占为“争议中”，再插入争议记录，两步在同一事务内完成。
     *
     * <p>条件更新借助 InnoDB 行锁同时承担两项职责：① 校验订单存在、未逻辑删除且确处于“已完成”状态，
     * 避免对非法/已取消/已删除订单发起争议；② 并发或重复提交时，两个请求只有一个能命中
     * {@code order_status='3'}，另一个影响 0 行即抛异常回滚，从源头阻止同一订单产生重复争议。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitDispute(TrTradeDispute trTradeDispute)
    {
        // 闲鱼模式：允许"已完成(3)"或"待收货(2)+退款被卖家拒绝(4)"两种来源发起争议
        int updated = trTradeOrderMapper.updateOrderToDisputing(trTradeDispute.getOrderId());
        if (updated == 0)
        {
            throw new ServiceException("订单不存在或当前状态不可发起争议，请勿重复提交");
        }
        insertTrTradeDispute(trTradeDispute);
        // 通过事件 + @TransactionalEventListener(AFTER_COMMIT) 触发异步 AI 仲裁，
        // 确保异步线程读取争议记录时本事务已 commit，规避事务可见性竞态。
        eventPublisher.publishEvent(new DisputeSubmittedEvent(trTradeDispute.getDisputeId()));
    }

    /**
     * 修改交易争议处理
     * 
     * @param trTradeDispute 交易争议处理
     * @return 结果
     */
    @Override
    public int updateTrTradeDispute(TrTradeDispute trTradeDispute)
    {
        trTradeDispute.setUpdateBy(SecurityUtils.getUsername());
        trTradeDispute.setUpdateTime(DateUtils.getNowDate());
        return trTradeDisputeMapper.updateTrTradeDispute(trTradeDispute);
    }

    /** 争议处理状态：已处理（终态） */
    private static final String HANDLE_DONE = "3";

    /**
     * 人工仲裁单条争议（处理后置为 3已处理）。
     * 若 refundToBuyer 为真，则在同一事务内调用支付宝退款给买家（订单争议中→已退款）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleDispute(Long disputeId, String handleResult, String faultParty, boolean refundToBuyer)
    {
        if (disputeId == null)
        {
            throw new ServiceException("仲裁失败，争议ID不能为空");
        }
        if (StringUtils.isEmpty(handleResult))
        {
            throw new ServiceException("仲裁失败，必须填写仲裁结论");
        }
        TrTradeDispute exist = trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(disputeId);
        if (exist == null)
        {
            throw new ServiceException("仲裁失败，争议不存在或已删除");
        }
        if (HANDLE_DONE.equals(exist.getHandleStatus()))
        {
            throw new ServiceException("仲裁失败，该争议已处理完成");
        }
        Date now = DateUtils.getNowDate();
        int rows = trTradeDisputeMapper.updateDisputeHandle(
                disputeId, HANDLE_DONE, currentSysUserId(), now, handleResult,
                faultParty, SecurityUtils.getUsername(), now);
        if (rows > 0)
        {
            // 判退款给买家：调支付宝退款（失败抛异常 → 整个仲裁事务回滚，避免"标记已处理但没退款"）
            if (refundToBuyer)
            {
                appOrderService.refundByArbitration(exist.getOrderId(), SecurityUtils.getUsername());
            }
            // 管理员手动判责 → 扣责任方信用分；faultParty 为 null/none 不扣
            publishDisputeFault(exist, faultParty);
        }
        return rows;
    }

    /** 按管理员判定的责任方发布扣分事件（respondent/applicant/both）。 */
    private void publishDisputeFault(TrTradeDispute dispute, String faultParty)
    {
        if (StringUtils.isEmpty(faultParty) || CreditConstants.FAULT_NONE.equals(faultParty))
        {
            return;
        }
        boolean blameRespondent = CreditConstants.FAULT_RESPONDENT.equals(faultParty)
                || CreditConstants.FAULT_BOTH.equals(faultParty);
        boolean blameApplicant = CreditConstants.FAULT_APPLICANT.equals(faultParty)
                || CreditConstants.FAULT_BOTH.equals(faultParty);
        if (blameRespondent && dispute.getRespondentId() != null)
        {
            eventPublisher.publishEvent(new CreditChangeEvent(dispute.getRespondentId(),
                    CreditConstants.TYPE_DISPUTE_FAULT, CreditConstants.DELTA_DISPUTE_FAULT,
                    CreditConstants.BIZ_DISPUTE, dispute.getDisputeId(), "争议仲裁判定责任（被申诉人）"));
        }
        if (blameApplicant && dispute.getApplicantId() != null)
        {
            eventPublisher.publishEvent(new CreditChangeEvent(dispute.getApplicantId(),
                    CreditConstants.TYPE_DISPUTE_FAULT, CreditConstants.DELTA_DISPUTE_FAULT,
                    CreditConstants.BIZ_DISPUTE, dispute.getDisputeId(), "争议仲裁判定责任（发起人）"));
        }
    }

    /**
     * 批量人工仲裁争议（跳过已处理或不存在的）。批量不做信用判责（责任方需逐条人工判定）。
     */
    @Override
    public int batchHandleDispute(Long[] disputeIds, String handleResult)
    {
        if (disputeIds == null || disputeIds.length == 0)
        {
            throw new ServiceException("批量仲裁失败，争议ID列表不能为空");
        }
        int success = 0;
        for (Long id : disputeIds)
        {
            try
            {
                success += handleDispute(id, handleResult, null, false);
            }
            catch (ServiceException ignored)
            {
                // 跳过已处理或不存在的，继续处理其它
            }
        }
        return success;
    }

    /** 取当前登录管理员 userId（找不到则返回 null）。 */
    private Long currentSysUserId()
    {
        try
        {
            return SecurityUtils.getUserId();
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 批量删除交易争议处理
     *
     * @param disputeIds 需要删除的交易争议处理主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeDisputeByDisputeIds(Long[] disputeIds)
    {
        return trTradeDisputeMapper.deleteTrTradeDisputeByDisputeIds(disputeIds);
    }

    /**
     * 删除交易争议处理信息
     * 
     * @param disputeId 交易争议处理主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeDisputeByDisputeId(Long disputeId)
    {
        return trTradeDisputeMapper.deleteTrTradeDisputeByDisputeId(disputeId);
    }
}
