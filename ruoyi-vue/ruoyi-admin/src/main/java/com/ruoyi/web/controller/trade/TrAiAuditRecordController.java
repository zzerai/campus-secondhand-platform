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
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.service.ITrAiAuditRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AI审核记录Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "AI 审核记录")
@RestController
@RequestMapping("/trade/aiAuditRecord")
public class TrAiAuditRecordController extends BaseController
{
    @Autowired
    private ITrAiAuditRecordService trAiAuditRecordService;

    /**
     * 查询AI审核记录列表
     */
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrAiAuditRecord trAiAuditRecord)
    {
        startPage();
        List<TrAiAuditRecord> list = trAiAuditRecordService.selectTrAiAuditRecordList(trAiAuditRecord);
        return getDataTable(list);
    }

    /**
     * 导出AI审核记录列表
     */
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:export')")
    @Log(title = "AI审核记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrAiAuditRecord trAiAuditRecord)
    {
        List<TrAiAuditRecord> list = trAiAuditRecordService.selectTrAiAuditRecordList(trAiAuditRecord);
        ExcelUtil<TrAiAuditRecord> util = new ExcelUtil<TrAiAuditRecord>(TrAiAuditRecord.class);
        util.exportExcel(response, list, "AI审核记录数据");
    }

    /**
     * 获取AI审核记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(trAiAuditRecordService.selectTrAiAuditRecordByRecordId(recordId));
    }

    /**
     * 新增AI审核记录
     */
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:add')")
    @Log(title = "AI审核记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrAiAuditRecord trAiAuditRecord)
    {
        return toAjax(trAiAuditRecordService.insertTrAiAuditRecord(trAiAuditRecord));
    }

    /**
     * 修改AI审核记录
     */
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:edit')")
    @Log(title = "AI审核记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrAiAuditRecord trAiAuditRecord)
    {
        return toAjax(trAiAuditRecordService.updateTrAiAuditRecord(trAiAuditRecord));
    }

    /**
     * 删除AI审核记录
     */
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:remove')")
    @Log(title = "AI审核记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(trAiAuditRecordService.deleteTrAiAuditRecordByRecordIds(recordIds));
    }
}
