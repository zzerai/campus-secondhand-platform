package com.ruoyi.trade.mapper;

import java.util.List;
import com.ruoyi.trade.domain.TrAiAuditRecord;

/**
 * AI审核记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrAiAuditRecordMapper 
{
    /**
     * 查询AI审核记录
     * 
     * @param recordId AI审核记录主键
     * @return AI审核记录
     */
    public TrAiAuditRecord selectTrAiAuditRecordByRecordId(Long recordId);

    /**
     * 查询AI审核记录列表
     * 
     * @param trAiAuditRecord AI审核记录
     * @return AI审核记录集合
     */
    public List<TrAiAuditRecord> selectTrAiAuditRecordList(TrAiAuditRecord trAiAuditRecord);

    /**
     * 新增AI审核记录
     * 
     * @param trAiAuditRecord AI审核记录
     * @return 结果
     */
    public int insertTrAiAuditRecord(TrAiAuditRecord trAiAuditRecord);

    /**
     * 修改AI审核记录
     * 
     * @param trAiAuditRecord AI审核记录
     * @return 结果
     */
    public int updateTrAiAuditRecord(TrAiAuditRecord trAiAuditRecord);

    /**
     * 删除AI审核记录
     * 
     * @param recordId AI审核记录主键
     * @return 结果
     */
    public int deleteTrAiAuditRecordByRecordId(Long recordId);

    /**
     * 批量删除AI审核记录
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrAiAuditRecordByRecordIds(Long[] recordIds);
}
