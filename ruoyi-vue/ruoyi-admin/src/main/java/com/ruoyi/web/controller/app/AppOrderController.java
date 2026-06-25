package com.ruoyi.web.controller.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppOrderCreateDto;
import com.ruoyi.trade.domain.dto.AppOrderRefundDto;
import com.ruoyi.trade.service.IAppOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Mobile order APIs.
 */
@RestController
@RequestMapping("/app/order")
@Tag(name = "移动端订单接口")
public class AppOrderController extends AppApiController
{
    @Autowired
    private IAppOrderService tradeOrderService;

    /**
     * Create an order for a goods item.
     */
    @Log(title = "移动端订单创建", businessType = BusinessType.INSERT)
    @Operation(summary = "创建订单", description = "当前登录用户对指定商品创建订单")
    @PostMapping("/create")
    public AjaxResult create(@RequestBody AppOrderCreateDto dto)
    {
        return success(tradeOrderService.createOrder(dto, getUserId(), getUsername()));
    }

    /**
     * Order detail visible to buyer or seller.
     */
    @Operation(summary = "订单详情", description = "买家或卖家查询订单详情")
    @GetMapping("/detail/{orderId}")
    public AjaxResult detail(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return success(tradeOrderService.selectTradeOrderById(orderId, getUserId()));
    }

    /**
     * Orders where current user is buyer.
     */
    @Operation(summary = "我买到的订单", description = "查询当前登录用户作为买家的订单列表")
    @GetMapping("/myBuy")
    public TableDataInfo myBuy(TrTradeOrder order)
    {
        startPage();
        order.setBuyerId(getUserId());
        List<TrTradeOrder> list = tradeOrderService.selectMyBuyOrderList(order);
        return getDataTable(list);
    }

    /**
     * Orders where current user is seller.
     */
    @Operation(summary = "我卖出的订单", description = "查询当前登录用户作为卖家的订单列表")
    @GetMapping("/mySell")
    public TableDataInfo mySell(TrTradeOrder order)
    {
        startPage();
        order.setSellerId(getUserId());
        List<TrTradeOrder> list = tradeOrderService.selectMySellOrderList(order);
        return getDataTable(list);
    }

    /**
     * Cancel pending order.
     */
    @Log(title = "移动端订单取消", businessType = BusinessType.UPDATE)
    @Operation(summary = "取消订单", description = "买家或卖家取消待处理订单")
    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return toAjax(tradeOrderService.cancelOrder(orderId, getUserId(), getUsername()));
    }

    /**
     * Buyer applies for a refund (only when awaiting receipt).
     */
    @Log(title = "移动端申请退款", businessType = BusinessType.UPDATE)
    @Operation(summary = "买家申请退款", description = "买家对待收货订单发起退款申请，等待卖家处理")
    @PutMapping("/refund/apply/{orderId}")
    public AjaxResult applyRefund(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId,
            @RequestBody(required = false) AppOrderRefundDto dto)
    {
        String reason = dto == null ? null : dto.getReason();
        return toAjax(tradeOrderService.applyRefund(orderId, getUserId(), reason, getUsername()));
    }

    /**
     * Seller agrees to the buyer's refund application and triggers Alipay refund.
     */
    @Log(title = "移动端同意退款", businessType = BusinessType.UPDATE)
    @Operation(summary = "卖家同意退款", description = "卖家同意买家退款申请，调用支付宝退款并将订单置为已退款")
    @PutMapping("/refund/agree/{orderId}")
    public AjaxResult agreeRefund(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return toAjax(tradeOrderService.agreeRefund(orderId, getUserId(), getUsername()));
    }

    /**
     * Seller rejects the buyer's refund application.
     */
    @Log(title = "移动端拒绝退款", businessType = BusinessType.UPDATE)
    @Operation(summary = "卖家拒绝退款", description = "卖家拒绝买家退款申请，订单退回待收货，买家可发起争议")
    @PutMapping("/refund/reject/{orderId}")
    public AjaxResult rejectRefund(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return toAjax(tradeOrderService.rejectRefund(orderId, getUserId(), getUsername()));
    }

    /**
     * Seller proactively refunds (only when awaiting receipt).
     */
    @Log(title = "移动端卖家主动退款", businessType = BusinessType.UPDATE)
    @Operation(summary = "卖家主动退款", description = "卖家对待收货订单主动发起退款，调用支付宝退款并将订单置为已退款")
    @PutMapping("/refund/seller/{orderId}")
    public AjaxResult sellerRefund(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId,
            @RequestBody(required = false) AppOrderRefundDto dto)
    {
        String reason = dto == null ? null : dto.getReason();
        return toAjax(tradeOrderService.sellerRefund(orderId, getUserId(), reason, getUsername()));
    }

    /**
     * Create Alipay sandbox payment form.
     */
    @Log(title = "移动端订单支付宝支付", businessType = BusinessType.UPDATE)
    @Operation(summary = "发起支付宝沙盒支付", description = "买家对已确认订单发起支付，返回可渲染的支付表单 HTML")
    @PostMapping("/pay/{orderId}")
    public AjaxResult pay(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return success(tradeOrderService.createAlipayPayForm(orderId, getUserId(), getUsername()));
    }

    /**
     * Alipay async notify callback. It must return plain text success/failure.
     */
    @Operation(summary = "支付宝异步通知", description = "支付宝服务器回调，验签成功后更新订单支付状态")
    @PostMapping(value = "/alipay/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String alipayNotify(HttpServletRequest request)
    {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, String.join(",", values)));
        return tradeOrderService.handleAlipayNotify(params) ? "success" : "failure";
    }

    /**
     * Payment result polling API.
     */
    @Operation(summary = "查询支付结果", description = "移动端支付后轮询订单支付状态")
    @GetMapping("/payResult/{orderId}")
    public AjaxResult payResult(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return success(tradeOrderService.getPayResult(orderId, getUserId()));
    }

    /**
     * Alipay sync return page. Process the return parameters to update order status
     * (fallback when async notify is unreachable, e.g. sandbox behind NAT).
     */
    @Operation(summary = "支付宝同步返回", description = "支付完成后支付宝浏览器跳回，验签并更新支付状态，返回自关闭页面")
    @GetMapping(value = "/payResult", produces = MediaType.TEXT_HTML_VALUE)
    public String payReturn(HttpServletRequest request)
    {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> params.put(key, String.join(",", values)));
        tradeOrderService.handleAlipayNotify(params);
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>"
                + "<p>支付完成，请返回应用查看订单状态</p>"
                + "<script>try{window.close();}catch(e){}</script>"
                + "</body></html>";
    }

    /**
     * Finish pending order.
     */
    @Log(title = "移动端订单完成", businessType = BusinessType.UPDATE)
    @Operation(summary = "完成订单", description = "买家或卖家确认完成待处理订单")
    @PutMapping("/finish/{orderId}")
    public AjaxResult finish(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        return toAjax(tradeOrderService.finishOrder(orderId, getUserId(), getUsername()));
    }
}
