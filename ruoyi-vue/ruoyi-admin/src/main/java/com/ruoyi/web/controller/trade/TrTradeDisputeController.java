package com.ruoyi.web.controller.trade;

import java.util.List;
import java.util.Map;
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
import com.ruoyi.trade.domain.TrTradeDispute;
import com.ruoyi.trade.service.ITrTradeDisputeService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 交易争议处理Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "交易争议处理")
@RestController
@RequestMapping("/trade/dispute")
public class TrTradeDisputeController extends BaseController
{
    @Autowired
    private ITrTradeDisputeService trTradeDisputeService;

    /**
     * 查询交易争议处理列表
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeDispute trTradeDispute)
    {
        startPage();
        List<TrTradeDispute> list = trTradeDisputeService.selectTrTradeDisputeList(trTradeDispute);
        return getDataTable(list);
    }

    /**
     * 导出交易争议处理列表
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:export')")
    @Log(title = "交易争议处理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeDispute trTradeDispute)
    {
        List<TrTradeDispute> list = trTradeDisputeService.selectTrTradeDisputeList(trTradeDispute);
        ExcelUtil<TrTradeDispute> util = new ExcelUtil<TrTradeDispute>(TrTradeDispute.class);
        util.exportExcel(response, list, "交易争议处理数据");
    }

    /**
     * 获取交易争议处理详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:query')")
    @GetMapping(value = "/{disputeId}")
    public AjaxResult getInfo(@PathVariable("disputeId") Long disputeId)
    {
        return success(trTradeDisputeService.selectTrTradeDisputeByDisputeId(disputeId));
    }

    /**
     * 新增交易争议处理
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:add')")
    @Log(title = "交易争议处理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeDispute trTradeDispute)
    {
        return toAjax(trTradeDisputeService.insertTrTradeDispute(trTradeDispute));
    }

    /**
     * 修改交易争议处理
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:edit')")
    @Log(title = "交易争议处理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeDispute trTradeDispute)
    {
        return toAjax(trTradeDisputeService.updateTrTradeDispute(trTradeDispute));
    }

    /**
     * 人工仲裁争议（处理后置为已处理，可单条/批量）
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:handle')")
    @Log(title = "交易争议处理-人工仲裁", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody HandleRequest body)
    {
        if (body == null)
        {
            return error("缺少请求参数");
        }
        int rows;
        if (body.getDisputeIds() != null && body.getDisputeIds().length > 0)
        {
            rows = trTradeDisputeService.batchHandleDispute(body.getDisputeIds(), body.getHandleResult());
        }
        else if (body.getDisputeId() != null)
        {
            rows = trTradeDisputeService.handleDispute(body.getDisputeId(), body.getHandleResult(),
                    body.getFaultParty(), body.isRefundToBuyer());
        }
        else
        {
            return error("缺少争议ID");
        }
        return AjaxResult.success(Map.of("affected", rows));
    }

    /**
     * 删除交易争议处理
     */
    @PreAuthorize("@ss.hasPermi('trade:dispute:remove')")
    @Log(title = "交易争议处理", businessType = BusinessType.DELETE)
	@DeleteMapping("/{disputeIds}")
    public AjaxResult remove(@PathVariable Long[] disputeIds)
    {
        return toAjax(trTradeDisputeService.deleteTrTradeDisputeByDisputeIds(disputeIds));
    }

    /** 人工仲裁请求体（单条传 disputeId，批量传 disputeIds）。 */
    public static class HandleRequest
    {
        private Long disputeId;
        private Long[] disputeIds;
        /** 仲裁结论（必填） */
        private String handleResult;
        /** 责任方（单条仲裁用）：respondent/applicant/both/none，决定信用扣分；批量不支持判责 */
        private String faultParty;
        /** 是否判退款给买家（单条仲裁用）：为真则调支付宝退款，订单争议中→已退款；批量不支持 */
        private boolean refundToBuyer;

        public Long getDisputeId() { return disputeId; }
        public void setDisputeId(Long disputeId) { this.disputeId = disputeId; }
        public Long[] getDisputeIds() { return disputeIds; }
        public void setDisputeIds(Long[] disputeIds) { this.disputeIds = disputeIds; }
        public String getHandleResult() { return handleResult; }
        public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
        public String getFaultParty() { return faultParty; }
        public void setFaultParty(String faultParty) { this.faultParty = faultParty; }
        public boolean isRefundToBuyer() { return refundToBuyer; }
        public void setRefundToBuyer(boolean refundToBuyer) { this.refundToBuyer = refundToBuyer; }
    }
}
