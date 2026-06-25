package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.mapper.TrAiAuditRecordMapper;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.service.ITrAiAuditRecordService;

/**
 * AI审核记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrAiAuditRecordServiceImpl implements ITrAiAuditRecordService 
{
    @Autowired
    private TrAiAuditRecordMapper trAiAuditRecordMapper;

    /**
     * 查询AI审核记录
     * 
     * @param recordId AI审核记录主键
     * @return AI审核记录
     */
    @Override
    public TrAiAuditRecord selectTrAiAuditRecordByRecordId(Long recordId)
    {
        return trAiAuditRecordMapper.selectTrAiAuditRecordByRecordId(recordId);
    }

    /**
     * 查询AI审核记录列表
     * 
     * @param trAiAuditRecord AI审核记录
     * @return AI审核记录
     */
    @Override
    public List<TrAiAuditRecord> selectTrAiAuditRecordList(TrAiAuditRecord trAiAuditRecord)
    {
        return trAiAuditRecordMapper.selectTrAiAuditRecordList(trAiAuditRecord);
    }

    /**
     * 新增AI审核记录
     * 
     * @param trAiAuditRecord AI审核记录
     * @return 结果
     */
    @Override
    public int insertTrAiAuditRecord(TrAiAuditRecord trAiAuditRecord)
    {
        String username = SecurityUtils.getUsername();
        trAiAuditRecord.setCreateBy(username);
        trAiAuditRecord.setCreateTime(DateUtils.getNowDate());
        trAiAuditRecord.setUpdateBy(username);
        trAiAuditRecord.setUpdateTime(DateUtils.getNowDate());
        return trAiAuditRecordMapper.insertTrAiAuditRecord(trAiAuditRecord);
    }

    /**
     * 修改AI审核记录
     * 
     * @param trAiAuditRecord AI审核记录
     * @return 结果
     */
    @Override
    public int updateTrAiAuditRecord(TrAiAuditRecord trAiAuditRecord)
    {
        trAiAuditRecord.setUpdateBy(SecurityUtils.getUsername());
        trAiAuditRecord.setUpdateTime(DateUtils.getNowDate());
        return trAiAuditRecordMapper.updateTrAiAuditRecord(trAiAuditRecord);
    }

    /**
     * 批量删除AI审核记录
     * 
     * @param recordIds 需要删除的AI审核记录主键
     * @return 结果
     */
    @Override
    public int deleteTrAiAuditRecordByRecordIds(Long[] recordIds)
    {
        return trAiAuditRecordMapper.deleteTrAiAuditRecordByRecordIds(recordIds);
    }

    /**
     * 删除AI审核记录信息
     * 
     * @param recordId AI审核记录主键
     * @return 结果
     */
    @Override
    public int deleteTrAiAuditRecordByRecordId(Long recordId)
    {
        return trAiAuditRecordMapper.deleteTrAiAuditRecordByRecordId(recordId);
    }
}
