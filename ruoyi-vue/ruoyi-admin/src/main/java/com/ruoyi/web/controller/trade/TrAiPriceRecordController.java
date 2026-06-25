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
import com.ruoyi.trade.domain.TrAiPriceRecord;
import com.ruoyi.trade.service.ITrAiPriceRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AI估价记录Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "AI 估价记录")
@RestController
@RequestMapping("/trade/aiPriceRecord")
public class TrAiPriceRecordController extends BaseController
{
    @Autowired
    private ITrAiPriceRecordService trAiPriceRecordService;

    /**
     * 查询AI估价记录列表
     */
    @PreAuthorize("@ss.hasPermi('trade:aiPriceRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrAiPriceRecord trAiPriceRecord)
    {
        startPage();
        List<TrAiPriceRecord> list = trAiPriceRecordService.selectTrAiPriceRecordList(trAiPriceRecord);
        return getDataTable(list);
    }

    /**
     * 导出AI估价记录列表
     */
    @PreAuthorize("@ss.hasPermi('trade:aiPriceRecord:export')")
    @Log(title = "AI估价记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrAiPriceRecord trAiPriceRecord)
    {
        List<TrAiPriceRecord> list = trAiPriceRecordService.selectTrAiPriceRecordList(trAiPriceRecord);
        ExcelUtil<TrAiPriceRecord> util = new ExcelUtil<TrAiPriceRecord>(TrAiPriceRecord.class);
        util.exportExcel(response, list, "AI估价记录数据");
    }

    /**
     * 获取AI估价记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:aiPriceRecord:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return success(trAiPriceRecordService.selectTrAiPriceRecordByRecordId(recordId));
    }

    /**
     * 新增AI估价记录
     */
    @PreAuthorize("@ss.hasPermi('trade:aiPriceRecord:add')")
    @Log(title = "AI估价记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrAiPriceRecord trAiPriceRecord)
    {
        return toAjax(trAiPriceRecordService.insertTrAiPriceRecord(trAiPriceRecord));
    }

    /**
     * 修改AI估价记录
     */
    @PreAuthorize("@ss.hasPermi('trade:aiPriceRecord:edit')")
    @Log(title = "AI估价记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrAiPriceRecord trAiPriceRecord)
    {
        return toAjax(trAiPriceRecordService.updateTrAiPriceRecord(trAiPriceRecord));
    }

    /**
     * 删除AI估价记录
     */
    @PreAuthorize("@ss.hasPermi('trade:aiPriceRecord:remove')")
    @Log(title = "AI估价记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(trAiPriceRecordService.deleteTrAiPriceRecordByRecordIds(recordIds));
    }
}
