package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.mapper.TrTradeOrderLogMapper;
import com.ruoyi.trade.domain.TrTradeOrderLog;
import com.ruoyi.trade.service.ITrTradeOrderLogService;

/**
 * 订单操作日志Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeOrderLogServiceImpl implements ITrTradeOrderLogService 
{
    @Autowired
    private TrTradeOrderLogMapper trTradeOrderLogMapper;

    /**
     * 查询订单操作日志
     * 
     * @param logId 订单操作日志主键
     * @return 订单操作日志
     */
    @Override
    public TrTradeOrderLog selectTrTradeOrderLogByLogId(Long logId)
    {
        return trTradeOrderLogMapper.selectTrTradeOrderLogByLogId(logId);
    }

    /**
     * 查询订单操作日志列表
     * 
     * @param trTradeOrderLog 订单操作日志
     * @return 订单操作日志
     */
    @Override
    public List<TrTradeOrderLog> selectTrTradeOrderLogList(TrTradeOrderLog trTradeOrderLog)
    {
        return trTradeOrderLogMapper.selectTrTradeOrderLogList(trTradeOrderLog);
    }

    /**
     * 新增订单操作日志
     * 
     * @param trTradeOrderLog 订单操作日志
     * @return 结果
     */
    @Override
    public int insertTrTradeOrderLog(TrTradeOrderLog trTradeOrderLog)
    {
        String username = SecurityUtils.getUsername();
        trTradeOrderLog.setCreateBy(username);
        trTradeOrderLog.setCreateTime(DateUtils.getNowDate());
        trTradeOrderLog.setUpdateBy(username);
        trTradeOrderLog.setUpdateTime(DateUtils.getNowDate());
        return trTradeOrderLogMapper.insertTrTradeOrderLog(trTradeOrderLog);
    }

    /**
     * 修改订单操作日志
     * 
     * @param trTradeOrderLog 订单操作日志
     * @return 结果
     */
    @Override
    public int updateTrTradeOrderLog(TrTradeOrderLog trTradeOrderLog)
    {
        trTradeOrderLog.setUpdateBy(SecurityUtils.getUsername());
        trTradeOrderLog.setUpdateTime(DateUtils.getNowDate());
        return trTradeOrderLogMapper.updateTrTradeOrderLog(trTradeOrderLog);
    }

    /**
     * 批量删除订单操作日志
     * 
     * @param logIds 需要删除的订单操作日志主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeOrderLogByLogIds(Long[] logIds)
    {
        return trTradeOrderLogMapper.deleteTrTradeOrderLogByLogIds(logIds);
    }

    /**
     * 删除订单操作日志信息
     * 
     * @param logId 订单操作日志主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeOrderLogByLogId(Long logId)
    {
        return trTradeOrderLogMapper.deleteTrTradeOrderLogByLogId(logId);
    }
}
