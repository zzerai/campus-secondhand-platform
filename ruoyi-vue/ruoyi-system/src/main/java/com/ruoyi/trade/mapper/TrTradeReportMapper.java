package com.ruoyi.trade.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.vo.AppReportVo;

/**
 * 举报信息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeReportMapper 
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
     * 查询某用户的举报记录列表（联表回填商品标题/价格/封面/状态），按创建时间倒序。
     *
     * @param reportUserId 举报人ID
     * @return 移动端举报出参列表
     */
    public List<AppReportVo> selectAppReportVoListByUser(@Param("reportUserId") Long reportUserId);

    /**
     * Query pending report count.
     *
     * @return pending count
     */
    public Long selectPendingReportCount();

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
     * 校验同一用户对同一商品是否存在待处理（handle_status='0'）的举报。
     * 用于移动端举报提交时的重复提交防护：已处理 / 已驳回的举报不阻塞用户重新举报（业务上可改进证据后再举报）。
     *
     * @param reportUserId 举报人ID
     * @param goodsId      被举报商品ID
     * @return 命中行数（&gt;0 表示已有待处理举报）
     */
    public int countPendingByUserAndGoods(@Param("reportUserId") Long reportUserId,
                                          @Param("goodsId") Long goodsId);

    /**
     * 处理举报（更新处理状态、处理人、处理时间、处理结果）
     *
     * @param reportId 举报ID
     * @param handleStatus 处理状态：1已处理，2已驳回
     * @param handleUserId 处理管理员ID
     * @param handleTime 处理时间
     * @param handleResult 处理结果
     * @param updateBy 更新者
     * @param updateTime 更新时间
     * @return 结果
     */
    public int updateReportHandle(@Param("reportId") Long reportId,
                                  @Param("handleStatus") String handleStatus,
                                  @Param("handleUserId") Long handleUserId,
                                  @Param("handleTime") Date handleTime,
                                  @Param("handleResult") String handleResult,
                                  @Param("updateBy") String updateBy,
                                  @Param("updateTime") Date updateTime);

    /**
     * 删除举报信息
     * 
     * @param reportId 举报信息主键
     * @return 结果
     */
    public int deleteTrTradeReportByReportId(Long reportId);

    /**
     * 批量删除举报信息
     * 
     * @param reportIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeReportByReportIds(Long[] reportIds);
}
