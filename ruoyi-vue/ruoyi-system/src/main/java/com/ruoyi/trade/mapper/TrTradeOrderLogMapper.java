package com.ruoyi.trade.mapper;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeOrderLog;

/**
 * 订单操作日志Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeOrderLogMapper 
{
    /**
     * 查询订单操作日志
     * 
     * @param logId 订单操作日志主键
     * @return 订单操作日志
     */
    public TrTradeOrderLog selectTrTradeOrderLogByLogId(Long logId);

    /**
     * 查询订单操作日志列表
     * 
     * @param trTradeOrderLog 订单操作日志
     * @return 订单操作日志集合
     */
    public List<TrTradeOrderLog> selectTrTradeOrderLogList(TrTradeOrderLog trTradeOrderLog);

    /**
     * 新增订单操作日志
     * 
     * @param trTradeOrderLog 订单操作日志
     * @return 结果
     */
    public int insertTrTradeOrderLog(TrTradeOrderLog trTradeOrderLog);

    /**
     * 修改订单操作日志
     * 
     * @param trTradeOrderLog 订单操作日志
     * @return 结果
     */
    public int updateTrTradeOrderLog(TrTradeOrderLog trTradeOrderLog);

    /**
     * 删除订单操作日志
     * 
     * @param logId 订单操作日志主键
     * @return 结果
     */
    public int deleteTrTradeOrderLogByLogId(Long logId);

    /**
     * 批量删除订单操作日志
     * 
     * @param logIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeOrderLogByLogIds(Long[] logIds);
}
