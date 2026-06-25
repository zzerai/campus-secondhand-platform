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
import com.ruoyi.trade.mapper.TrTradeReportMapper;
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.vo.BatchHandleResult;
import com.ruoyi.trade.event.CreditChangeEvent;
import com.ruoyi.trade.service.ITrTradeReportService;
import com.ruoyi.trade.utils.CreditConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * 举报信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeReportServiceImpl implements ITrTradeReportService 
{
    @Autowired
    private TrTradeReportMapper trTradeReportMapper;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 查询举报信息
     * 
     * @param reportId 举报信息主键
     * @return 举报信息
     */
    @Override
    public TrTradeReport selectTrTradeReportByReportId(Long reportId)
    {
        return trTradeReportMapper.selectTrTradeReportByReportId(reportId);
    }

    /**
     * 查询举报信息列表
     * 
     * @param trTradeReport 举报信息
     * @return 举报信息
     */
    @Override
    public List<TrTradeReport> selectTrTradeReportList(TrTradeReport trTradeReport)
    {
        return trTradeReportMapper.selectTrTradeReportList(trTradeReport);
    }

    /**
     * 新增举报信息
     * 
     * @param trTradeReport 举报信息
     * @return 结果
     */
    @Override
    public int insertTrTradeReport(TrTradeReport trTradeReport)
    {
        String username = SecurityUtils.getUsername();
        trTradeReport.setCreateBy(username);
        trTradeReport.setCreateTime(DateUtils.getNowDate());
        trTradeReport.setUpdateBy(username);
        trTradeReport.setUpdateTime(DateUtils.getNowDate());
        return trTradeReportMapper.insertTrTradeReport(trTradeReport);
    }

    /**
     * 修改举报信息
     * 
     * @param trTradeReport 举报信息
     * @return 结果
     */
    @Override
    public int updateTrTradeReport(TrTradeReport trTradeReport)
    {
        trTradeReport.setUpdateBy(SecurityUtils.getUsername());
        trTradeReport.setUpdateTime(DateUtils.getNowDate());
        return trTradeReportMapper.updateTrTradeReport(trTradeReport);
    }

    /** 举报处理状态：待处理 */
    private static final String HANDLE_PENDING  = "0";
    /** 举报处理状态：已处理 */
    private static final String HANDLE_DONE     = "1";
    /** 举报处理状态：已驳回 */
    private static final String HANDLE_REJECTED = "2";

    /**
     * 处理单条举报
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleReport(Long reportId, String handleStatus, String handleResult)
    {
        if (reportId == null)
        {
            throw new ServiceException("处理失败，举报ID不能为空");
        }
        if (!HANDLE_DONE.equals(handleStatus) && !HANDLE_REJECTED.equals(handleStatus))
        {
            throw new ServiceException("处理失败，目标状态只能是 1(已处理) 或 2(已驳回)");
        }
        if (StringUtils.isEmpty(handleResult))
        {
            throw new ServiceException("处理失败，必须填写处理结果");
        }
        TrTradeReport exist = trTradeReportMapper.selectTrTradeReportByReportId(reportId);
        if (exist == null)
        {
            throw new ServiceException("处理失败，举报不存在或已删除");
        }
        if (!HANDLE_PENDING.equals(exist.getHandleStatus()))
        {
            throw new ServiceException("处理失败，仅待处理状态的举报可以执行处理操作");
        }
        Date now = DateUtils.getNowDate();
        int rows = trTradeReportMapper.updateReportHandle(
                reportId, handleStatus, currentSysUserId(), now, handleResult,
                SecurityUtils.getUsername(), now);
        // 举报判定成立（已处理）→ 扣被举报人信用分；驳回不扣
        if (rows > 0 && HANDLE_DONE.equals(handleStatus) && exist.getReportedUserId() != null)
        {
            eventPublisher.publishEvent(new CreditChangeEvent(exist.getReportedUserId(),
                    CreditConstants.TYPE_REPORT_VALID, CreditConstants.DELTA_REPORT_VALID,
                    CreditConstants.BIZ_REPORT, reportId, "举报判定成立"));
        }
        return rows;
    }

    /**
     * 批量处理举报。
     * 单条 handleReport 自带 @Transactional 独立提交，本方法不加外层事务 —— 一条失败不影响其它。
     * 改返回结构化 BatchHandleResult 暴露每条失败原因，避免吞异常前端无感。
     */
    @Override
    public BatchHandleResult batchHandleReport(Long[] reportIds, String handleStatus, String handleResult)
    {
        if (reportIds == null || reportIds.length == 0)
        {
            throw new ServiceException("批量处理失败，举报ID列表不能为空");
        }
        BatchHandleResult result = new BatchHandleResult();
        for (Long id : reportIds)
        {
            try
            {
                if (handleReport(id, handleStatus, handleResult) > 0)
                {
                    result.incSuccess();
                }
                else
                {
                    result.addError(id, "数据库未更新（可能并发已被改）");
                }
            }
            catch (ServiceException ex)
            {
                result.addError(id, ex.getMessage());
            }
        }
        return result;
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
     * 批量删除举报信息
     *
     * @param reportIds 需要删除的举报信息主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeReportByReportIds(Long[] reportIds)
    {
        return trTradeReportMapper.deleteTrTradeReportByReportIds(reportIds);
    }

    /**
     * 删除举报信息信息
     * 
     * @param reportId 举报信息主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeReportByReportId(Long reportId)
    {
        return trTradeReportMapper.deleteTrTradeReportByReportId(reportId);
    }
}
