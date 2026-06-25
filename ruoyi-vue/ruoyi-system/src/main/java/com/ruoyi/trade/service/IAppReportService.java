package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.dto.AppReportSubmitDto;
import com.ruoyi.trade.domain.vo.AppReportVo;

/**
 * 移动端举报业务接口。
 *
 * <p>从 {@code AppReportController} 抽出，统一承接：商品有效性校验、重复提交防护、
 * 举报人身份信息推导、写入。Controller 仅负责 token 解析与 DTO 字段校验。</p>
 *
 * @author thr
 */
public interface IAppReportService
{
    /**
     * 提交一条举报。
     *
     * @param userId 举报人ID（由 token 解析得到）
     * @param dto    举报内容
     * @return 新举报记录ID
     */
    Long submitReport(Long userId, AppReportSubmitDto dto);

    /**
     * 查询当前用户的举报记录列表。
     *
     * @param userId 举报人ID
     * @return 举报记录列表（含商品卡片信息，按创建时间倒序）
     */
    List<AppReportVo> getMyReports(Long userId);
}
