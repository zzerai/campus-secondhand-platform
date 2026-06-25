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
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.service.ITrTradeOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 交易订单Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "交易订单")
@RestController
@RequestMapping("/trade/order")
public class TrTradeOrderController extends BaseController
{
    @Autowired
    private ITrTradeOrderService trTradeOrderService;

    /**
     * 查询交易订单列表
     */
    @PreAuthorize("@ss.hasPermi('trade:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeOrder trTradeOrder)
    {
        startPage();
        List<TrTradeOrder> list = trTradeOrderService.selectTrTradeOrderList(trTradeOrder);
        return getDataTable(list);
    }

    /**
     * 导出交易订单列表
     */
    @PreAuthorize("@ss.hasPermi('trade:order:export')")
    @Log(title = "交易订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeOrder trTradeOrder)
    {
        List<TrTradeOrder> list = trTradeOrderService.selectTrTradeOrderList(trTradeOrder);
        ExcelUtil<TrTradeOrder> util = new ExcelUtil<TrTradeOrder>(TrTradeOrder.class);
        util.exportExcel(response, list, "交易订单数据");
    }

    /**
     * 获取交易订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(trTradeOrderService.selectTrTradeOrderByOrderId(orderId));
    }

    /**
     * 新增交易订单
     */
    @PreAuthorize("@ss.hasPermi('trade:order:add')")
    @Log(title = "交易订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeOrder trTradeOrder)
    {
        return toAjax(trTradeOrderService.insertTrTradeOrder(trTradeOrder));
    }

    /**
     * 修改交易订单
     */
    @PreAuthorize("@ss.hasPermi('trade:order:edit')")
    @Log(title = "交易订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeOrder trTradeOrder)
    {
        return toAjax(trTradeOrderService.updateTrTradeOrder(trTradeOrder));
    }

    /**
     * 删除交易订单
     */
    @PreAuthorize("@ss.hasPermi('trade:order:remove')")
    @Log(title = "交易订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(trTradeOrderService.deleteTrTradeOrderByOrderIds(orderIds));
    }
}
