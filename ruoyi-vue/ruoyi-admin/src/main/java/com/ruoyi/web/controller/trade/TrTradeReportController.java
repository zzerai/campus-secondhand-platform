package com.ruoyi.web.controller.trade;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.TrTradeReport;
import com.ruoyi.trade.domain.vo.BatchHandleResult;
import com.ruoyi.trade.service.ITrTradeReportService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 举报信息Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "举报信息")
@RestController
@RequestMapping("/trade/report")
public class TrTradeReportController extends BaseController
{
    @Autowired
    private ITrTradeReportService trTradeReportService;

    /**
     * 查询举报信息列表
     */
    @PreAuthorize("@ss.hasPermi('trade:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeReport trTradeReport)
    {
        startPage();
        List<TrTradeReport> list = trTradeReportService.selectTrTradeReportList(trTradeReport);
        return getDataTable(list);
    }

    /**
     * 导出举报信息列表
     */
    @PreAuthorize("@ss.hasPermi('trade:report:export')")
    @Log(title = "举报信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeReport trTradeReport)
    {
        List<TrTradeReport> list = trTradeReportService.selectTrTradeReportList(trTradeReport);
        ExcelUtil<TrTradeReport> util = new ExcelUtil<TrTradeReport>(TrTradeReport.class);
        util.exportExcel(response, list, "举报信息数据");
    }

    /**
     * 获取举报信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId)
    {
        return success(trTradeReportService.selectTrTradeReportByReportId(reportId));
    }

    /**
     * 新增举报信息
     */
    @PreAuthorize("@ss.hasPermi('trade:report:add')")
    @Log(title = "举报信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeReport trTradeReport)
    {
        return toAjax(trTradeReportService.insertTrTradeReport(trTradeReport));
    }

    /**
     * 修改举报信息
     */
    @PreAuthorize("@ss.hasPermi('trade:report:edit')")
    @Log(title = "举报信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeReport trTradeReport)
    {
        return toAjax(trTradeReportService.updateTrTradeReport(trTradeReport));
    }

    /**
     * 处理举报（已处理/已驳回，可单条/批量）。
     * 统一返回 BatchHandleResult；单条处理失败抛 ServiceException 由全局异常处理。
     */
    @PreAuthorize("@ss.hasPermi('trade:report:handle')")
    @Log(title = "举报信息-处理", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody HandleRequest body)
    {
        if (body == null || body.getHandleStatus() == null)
        {
            return error("缺少处理状态");
        }
        BatchHandleResult result;
        if (body.getReportIds() != null && body.getReportIds().length > 0)
        {
            result = trTradeReportService.batchHandleReport(
                    body.getReportIds(), body.getHandleStatus(), body.getHandleResult());
        }
        else if (body.getReportId() != null)
        {
            int rows = trTradeReportService.handleReport(
                    body.getReportId(), body.getHandleStatus(), body.getHandleResult());
            result = new BatchHandleResult();
            if (rows > 0)
            {
                result.incSuccess();
            }
            else
            {
                result.addError(body.getReportId(), "数据库未更新");
            }
        }
        else
        {
            return error("缺少举报ID");
        }
        return AjaxResult.success(result);
    }

    /**
     * 删除举报信息
     */
    @PreAuthorize("@ss.hasPermi('trade:report:remove')")
    @Log(title = "举报信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(trTradeReportService.deleteTrTradeReportByReportIds(reportIds));
    }

    /** 举报处理请求体（单条传 reportId，批量传 reportIds）。 */
    public static class HandleRequest
    {
        private Long reportId;
        private Long[] reportIds;
        /** 目标状态：'1' 已处理、'2' 已驳回 */
        private String handleStatus;
        private String handleResult;

        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        public Long[] getReportIds() { return reportIds; }
        public void setReportIds(Long[] reportIds) { this.reportIds = reportIds; }
        public String getHandleStatus() { return handleStatus; }
        public void setHandleStatus(String handleStatus) { this.handleStatus = handleStatus; }
        public String getHandleResult() { return handleResult; }
        public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
    }
}
