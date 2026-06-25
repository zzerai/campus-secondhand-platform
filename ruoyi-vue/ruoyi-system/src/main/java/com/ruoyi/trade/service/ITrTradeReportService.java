package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.vo.BatchHandleResult;

/**
 * 举报信息Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeReportService 
{
    /**
     * 查询举报信息
     * 
     * @param reportId 举报信息主键
     * @return 举报信息
     */
    public TrTradeReport selectTrTradeReportByReportId(Long reportId);

    /**
     * 查询举报信息列表
     * 
     * @param trTradeReport 举报信息
     * @return 举报信息集合
     */
    public List<TrTradeReport> selectTrTradeReportList(TrTradeReport trTradeReport);

    /**
     * 新增举报信息
     * 
     * @param trTradeReport 举报信息
     * @return 结果
     */
    public int insertTrTradeReport(TrTradeReport trTradeReport);

    /**
     * 修改举报信息
     * 
     * @param trTradeReport 举报信息
     * @return 结果
     */
    public int updateTrTradeReport(TrTradeReport trTradeReport);

    /**
     * 处理单条举报
     *
     * @param reportId 举报ID
     * @param handleStatus 处理状态：1已处理，2已驳回
     * @param handleResult 处理结果
     * @return 结果
     */
    public int handleReport(Long reportId, String handleStatus, String handleResult);

    /**
     * 批量处理举报。返回结构化结果（success/failure/errors），便于前端逐条提示部分失败。
     *
     * @param reportIds 举报ID集合
     * @param handleStatus 处理状态：1已处理，2已驳回
     * @param handleResult 处理结果
     * @return 批量处理结果
     */
    public BatchHandleResult batchHandleReport(Long[] reportIds, String handleStatus, String handleResult);

    /**
     * 批量删除举报信息
     *
     * @param reportIds 需要删除的举报信息主键集合
     * @return 结果
     */
    public int deleteTrTradeReportByReportIds(Long[] reportIds);

    /**
     * 删除举报信息信息
     * 
     * @param reportId 举报信息主键
     * @return 结果
     */
    public int deleteTrTradeReportByReportId(Long reportId);
}
