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
import com.ruoyi.trade.domain.TrTradeOrderLog;
import com.ruoyi.trade.service.ITrTradeOrderLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 订单操作日志Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "订单操作日志")
@RestController
@RequestMapping("/trade/log")
public class TrTradeOrderLogController extends BaseController
{
    @Autowired
    private ITrTradeOrderLogService trTradeOrderLogService;

    /**
     * 查询订单操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('trade:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeOrderLog trTradeOrderLog)
    {
        startPage();
        List<TrTradeOrderLog> list = trTradeOrderLogService.selectTrTradeOrderLogList(trTradeOrderLog);
        return getDataTable(list);
    }

    /**
     * 导出订单操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('trade:log:export')")
    @Log(title = "订单操作日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeOrderLog trTradeOrderLog)
    {
        List<TrTradeOrderLog> list = trTradeOrderLogService.selectTrTradeOrderLogList(trTradeOrderLog);
        ExcelUtil<TrTradeOrderLog> util = new ExcelUtil<TrTradeOrderLog>(TrTradeOrderLog.class);
        util.exportExcel(response, list, "订单操作日志数据");
    }

    /**
     * 获取订单操作日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:log:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(trTradeOrderLogService.selectTrTradeOrderLogByLogId(logId));
    }

    /**
     * 新增订单操作日志
     */
    @PreAuthorize("@ss.hasPermi('trade:log:add')")
    @Log(title = "订单操作日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeOrderLog trTradeOrderLog)
    {
        return toAjax(trTradeOrderLogService.insertTrTradeOrderLog(trTradeOrderLog));
    }

    /**
     * 修改订单操作日志
     */
    @PreAuthorize("@ss.hasPermi('trade:log:edit')")
    @Log(title = "订单操作日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeOrderLog trTradeOrderLog)
    {
        return toAjax(trTradeOrderLogService.updateTrTradeOrderLog(trTradeOrderLog));
    }

    /**
     * 删除订单操作日志
     */
    @PreAuthorize("@ss.hasPermi('trade:log:remove')")
    @Log(title = "订单操作日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(trTradeOrderLogService.deleteTrTradeOrderLogByLogIds(logIds));
    }
}
