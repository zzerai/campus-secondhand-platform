package com.ruoyi.trade.mapper;

import java.util.List;
import com.ruoyi.trade.domain.TrAiPriceRecord;

/**
 * AI估价记录Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrAiPriceRecordMapper 
{
    /**
     * 查询AI估价记录
     * 
     * @param recordId AI估价记录主键
     * @return AI估价记录
     */
    public TrAiPriceRecord selectTrAiPriceRecordByRecordId(Long recordId);

    /**
     * 查询AI估价记录列表
     * 
     * @param trAiPriceRecord AI估价记录
     * @return AI估价记录集合
     */
    public List<TrAiPriceRecord> selectTrAiPriceRecordList(TrAiPriceRecord trAiPriceRecord);

    /**
     * 新增AI估价记录
     * 
     * @param trAiPriceRecord AI估价记录
     * @return 结果
     */
    public int insertTrAiPriceRecord(TrAiPriceRecord trAiPriceRecord);

    /**
     * 修改AI估价记录
     * 
     * @param trAiPriceRecord AI估价记录
     * @return 结果
     */
    public int updateTrAiPriceRecord(TrAiPriceRecord trAiPriceRecord);

    /**
     * 删除AI估价记录
     * 
     * @param recordId AI估价记录主键
     * @return 结果
     */
    public int deleteTrAiPriceRecordByRecordId(Long recordId);

    /**
     * 批量删除AI估价记录
     * 
     * @param recordIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrAiPriceRecordByRecordIds(Long[] recordIds);
}
