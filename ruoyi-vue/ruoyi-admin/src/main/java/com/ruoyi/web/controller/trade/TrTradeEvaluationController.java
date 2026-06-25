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
import com.ruoyi.trade.domain.TrTradeEvaluation;
import com.ruoyi.trade.service.ITrTradeEvaluationService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 交易评价Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "交易评价")
@RestController
@RequestMapping("/trade/evaluation")
public class TrTradeEvaluationController extends BaseController
{
    @Autowired
    private ITrTradeEvaluationService trTradeEvaluationService;

    /**
     * 查询交易评价列表
     */
    @PreAuthorize("@ss.hasPermi('trade:evaluation:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeEvaluation trTradeEvaluation)
    {
        startPage();
        List<TrTradeEvaluation> list = trTradeEvaluationService.selectTrTradeEvaluationList(trTradeEvaluation);
        return getDataTable(list);
    }

    /**
     * 导出交易评价列表
     */
    @PreAuthorize("@ss.hasPermi('trade:evaluation:export')")
    @Log(title = "交易评价", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeEvaluation trTradeEvaluation)
    {
        List<TrTradeEvaluation> list = trTradeEvaluationService.selectTrTradeEvaluationList(trTradeEvaluation);
        ExcelUtil<TrTradeEvaluation> util = new ExcelUtil<TrTradeEvaluation>(TrTradeEvaluation.class);
        util.exportExcel(response, list, "交易评价数据");
    }

    /**
     * 获取交易评价详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:evaluation:query')")
    @GetMapping(value = "/{evaluationId}")
    public AjaxResult getInfo(@PathVariable("evaluationId") Long evaluationId)
    {
        return success(trTradeEvaluationService.selectTrTradeEvaluationByEvaluationId(evaluationId));
    }

    /**
     * 新增交易评价
     */
    @PreAuthorize("@ss.hasPermi('trade:evaluation:add')")
    @Log(title = "交易评价", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeEvaluation trTradeEvaluation)
    {
        return toAjax(trTradeEvaluationService.insertTrTradeEvaluation(trTradeEvaluation));
    }

    /**
     * 修改交易评价
     */
    @PreAuthorize("@ss.hasPermi('trade:evaluation:edit')")
    @Log(title = "交易评价", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeEvaluation trTradeEvaluation)
    {
        return toAjax(trTradeEvaluationService.updateTrTradeEvaluation(trTradeEvaluation));
    }

    /**
     * 删除交易评价
     */
    @PreAuthorize("@ss.hasPermi('trade:evaluation:remove')")
    @Log(title = "交易评价", businessType = BusinessType.DELETE)
	@DeleteMapping("/{evaluationIds}")
    public AjaxResult remove(@PathVariable Long[] evaluationIds)
    {
        return toAjax(trTradeEvaluationService.deleteTrTradeEvaluationByEvaluationIds(evaluationIds));
    }
}
