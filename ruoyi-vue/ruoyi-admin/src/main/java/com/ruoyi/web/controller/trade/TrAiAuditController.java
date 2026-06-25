package com.ruoyi.web.controller.trade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.service.ITrAiDisputeArbitrationService;
import com.ruoyi.trade.service.ITrAiGoodsAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * AI审核控制器
 *
 * @author daj
 * @date 2026-05-20
 */
@Tag(name = "AI审核", description = "AI商品审核、AI争议仲裁等")
@RestController
@RequestMapping("/trade/ai/audit")
public class TrAiAuditController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(TrAiAuditController.class);

    @Autowired
    private ITrAiGoodsAuditService trAiGoodsAuditService;

    @Autowired
    private ITrAiDisputeArbitrationService trAiDisputeArbitrationService;

    /**
     * AI商品审核
     */
    @Operation(summary = "AI商品审核", description = "调用AI大模型对指定商品进行风险审核，返回风险等级、审核建议、风险原因")
    @Log(title = "AI商品审核", businessType = BusinessType.OTHER)
    @PostMapping("/goods")
    @PreAuthorize("@ss.hasPermi('trade:goods:audit')")
    public AjaxResult auditGoods(
            @Parameter(description = "商品ID", required = true) @RequestParam("goodsId") Long goodsId)
    {
        try
        {
            TrAiAuditRecord record = trAiGoodsAuditService.auditGoods(goodsId);
            AjaxResult ajax = success("AI审核完成");
            ajax.put("recordId", record.getRecordId());
            ajax.put("riskLevel", record.getRiskLevel());
            ajax.put("suggestion", record.getSuggestion());
            ajax.put("riskReason", record.getRiskReason());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("AI商品审核失败, goodsId={}", goodsId, e);
            return error("AI审核失败: " + e.getMessage());
        }
    }

    /**
     * AI争议仲裁
     */
    @Operation(summary = "AI争议仲裁", description = "调用AI大模型对指定争议进行仲裁分析，返回仲裁倾向、建议、原因。也作为 AI 失败后的人工'重试 AI'入口。")
    @Log(title = "AI争议仲裁", businessType = BusinessType.OTHER)
    @PostMapping("/dispute")
    @PreAuthorize("@ss.hasPermi('trade:dispute:handle')")
    public AjaxResult arbitrateDispute(
            @Parameter(description = "争议ID", required = true) @RequestParam("disputeId") Long disputeId)
    {
        try
        {
            TrAiAuditRecord record = trAiDisputeArbitrationService.arbitrateDispute(disputeId);
            AjaxResult ajax = success("AI仲裁完成");
            ajax.put("recordId", record.getRecordId());
            ajax.put("arbitrateLevel", record.getRiskLevel());
            ajax.put("suggestion", record.getSuggestion());
            ajax.put("reason", record.getRiskReason());
            return ajax;
        }
        catch (Exception e)
        {
            log.error("AI争议仲裁失败, disputeId={}", disputeId, e);
            return error("AI仲裁失败: " + e.getMessage());
        }
    }
}
