package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.mapper.TrAiPriceRecordMapper;
import com.ruoyi.trade.domain.TrAiPriceRecord;
import com.ruoyi.trade.service.ITrAiPriceRecordService;

/**
 * AI估价记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrAiPriceRecordServiceImpl implements ITrAiPriceRecordService 
{
    @Autowired
    private TrAiPriceRecordMapper trAiPriceRecordMapper;

    /**
     * 查询AI估价记录
     * 
     * @param recordId AI估价记录主键
     * @return AI估价记录
     */
    @Override
    public TrAiPriceRecord selectTrAiPriceRecordByRecordId(Long recordId)
    {
        return trAiPriceRecordMapper.selectTrAiPriceRecordByRecordId(recordId);
    }

    /**
     * 查询AI估价记录列表
     * 
     * @param trAiPriceRecord AI估价记录
     * @return AI估价记录
     */
    @Override
    public List<TrAiPriceRecord> selectTrAiPriceRecordList(TrAiPriceRecord trAiPriceRecord)
    {
        return trAiPriceRecordMapper.selectTrAiPriceRecordList(trAiPriceRecord);
    }

    /**
     * 新增AI估价记录
     * 
     * @param trAiPriceRecord AI估价记录
     * @return 结果
     */
    @Override
    public int insertTrAiPriceRecord(TrAiPriceRecord trAiPriceRecord)
    {
        String username = SecurityUtils.getUsername();
        trAiPriceRecord.setCreateBy(username);
        trAiPriceRecord.setCreateTime(DateUtils.getNowDate());
        trAiPriceRecord.setUpdateBy(username);
        trAiPriceRecord.setUpdateTime(DateUtils.getNowDate());
        return trAiPriceRecordMapper.insertTrAiPriceRecord(trAiPriceRecord);
    }

    /**
     * 修改AI估价记录
     * 
     * @param trAiPriceRecord AI估价记录
     * @return 结果
     */
    @Override
    public int updateTrAiPriceRecord(TrAiPriceRecord trAiPriceRecord)
    {
        trAiPriceRecord.setUpdateBy(SecurityUtils.getUsername());
        trAiPriceRecord.setUpdateTime(DateUtils.getNowDate());
        return trAiPriceRecordMapper.updateTrAiPriceRecord(trAiPriceRecord);
    }

    /**
     * 批量删除AI估价记录
     * 
     * @param recordIds 需要删除的AI估价记录主键
     * @return 结果
     */
    @Override
    public int deleteTrAiPriceRecordByRecordIds(Long[] recordIds)
    {
        return trAiPriceRecordMapper.deleteTrAiPriceRecordByRecordIds(recordIds);
    }

    /**
     * 删除AI估价记录信息
     * 
     * @param recordId AI估价记录主键
     * @return 结果
     */
    @Override
    public int deleteTrAiPriceRecordByRecordId(Long recordId)
    {
        return trAiPriceRecordMapper.deleteTrAiPriceRecordByRecordId(recordId);
    }
}
