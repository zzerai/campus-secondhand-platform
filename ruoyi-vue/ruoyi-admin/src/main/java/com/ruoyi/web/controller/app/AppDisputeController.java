package com.ruoyi.web.controller.app;

import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.framework.web.service.AppAuthService;
import com.ruoyi.trade.domain.TrTradeDispute;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppDisputeSubmitDto;
import com.ruoyi.trade.domain.vo.AppDisputeVo;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.ITrTradeDisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端争议控制器
 *
 * @author daj
 * @date 2026-05-20
 */
@Tag(name = "移动端争议", description = "学生提交争议申请、查看争议记录")
@RestController
@RequestMapping("/app/dispute")
public class AppDisputeController extends AppApiController
{
    /** 可发起争议的订单状态：已完成 */
    private static final String ORDER_STATUS_FINISHED = "3";

    /** 订单状态：待收货 */
    private static final String ORDER_STATUS_AWAITING_RECEIPT = "2";

    /** 订单状态：争议中 */
    private static final String ORDER_STATUS_DISPUTING = "5";

    /** 退款状态：卖家拒绝（待收货阶段退款被拒后可发起争议） */
    private static final String REFUND_STATUS_REJECTED = "4";

    @Autowired
    private AppAuthService appAuthService;

    @Autowired
    private ITrTradeDisputeService trTradeDisputeService;

    @Autowired
    private TrTradeDisputeMapper trTradeDisputeMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    /**
     * 提交争议申请
     */
    @Operation(summary = "提交争议申请", description = "学生对已完成的订单提交争议，填写争议类型、描述、证据图片")
    @PostMapping("/submit")
    public AjaxResult submit(HttpServletRequest request, @RequestBody AppDisputeSubmitDto submitDto)
    {
        Long userId = appAuthService.getUserIdFromRequest(request);
        if (userId == null)
        {
            return AjaxResult.error("未登录或登录已过期");
        }

        if (submitDto.getOrderId() == null)
        {
            return AjaxResult.error("订单ID不能为空");
        }
        if (submitDto.getDisputeType() == null || submitDto.getDisputeType().isEmpty())
        {
            return AjaxResult.error("争议类型不能为空");
        }
        if (submitDto.getDisputeContent() == null || submitDto.getDisputeContent().isEmpty())
        {
            return AjaxResult.error("争议描述不能为空");
        }

        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderId(submitDto.getOrderId());
        if (order == null)
        {
            return AjaxResult.error("订单不存在");
        }
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId()))
        {
            return AjaxResult.error("只能对自己参与的订单发起争议");
        }
        if (ORDER_STATUS_DISPUTING.equals(order.getOrderStatus()))
        {
            return AjaxResult.error("该订单已存在争议，请勿重复提交");
        }
        // 闲鱼模式：已完成订单，或待收货阶段退款被卖家拒绝的订单，可发起争议（平台介入）
        boolean canDispute = ORDER_STATUS_FINISHED.equals(order.getOrderStatus())
                || (ORDER_STATUS_AWAITING_RECEIPT.equals(order.getOrderStatus())
                        && REFUND_STATUS_REJECTED.equals(order.getRefundStatus()));
        if (!canDispute)
        {
            return AjaxResult.error("仅已完成、或退款被拒的订单可发起争议");
        }

        // 仅从 DTO 取用户可填字段，发起人/被申诉人/处理状态等由服务端推导
        TrTradeDispute dispute = new TrTradeDispute();
        dispute.setOrderId(submitDto.getOrderId());
        dispute.setDisputeType(submitDto.getDisputeType());
        dispute.setDisputeContent(submitDto.getDisputeContent());
        dispute.setEvidenceImages(submitDto.getEvidenceImages());
        dispute.setApplicantId(userId);
        dispute.setGoodsId(order.getGoodsId());
        dispute.setRespondentId(userId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId());
        dispute.setHandleStatus("0");

        // 插入争议并将订单转入“争议中”，两步在同一事务内完成。
        // AI 仲裁由 submitDispute 内 publishEvent + @TransactionalEventListener(AFTER_COMMIT) 在事务
        // 提交后自动触发，无需在此显式调用 async（避免事务可见性竞态）。
        trTradeDisputeService.submitDispute(dispute);

        AjaxResult ajax = AjaxResult.success("争议提交成功");
        ajax.put("disputeId", dispute.getDisputeId());
        return ajax;
    }

    /**
     * 我的争议列表
     */
    @Operation(summary = "我的争议列表", description = "查看当前学生相关的争议记录（发起或被申诉）")
    @GetMapping("/myList")
    public AjaxResult myList(HttpServletRequest request)
    {
        Long userId = appAuthService.getUserIdFromRequest(request);
        if (userId == null)
        {
            return AjaxResult.error("未登录或登录已过期");
        }

        List<TrTradeDispute> list = trTradeDisputeMapper.selectTrTradeDisputeByUserId(userId);
        List<AppDisputeVo> voList = new ArrayList<>(list.size());
        for (TrTradeDispute dispute : list)
        {
            AppDisputeVo vo = AppDisputeVo.from(dispute);
            if (dispute.getOrderId() != null)
            {
                TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderId(dispute.getOrderId());
                if (order != null)
                {
                    vo.setOrderNo(order.getOrderNo());
                    vo.setOrderCreateTime(order.getCreateTime());
                    vo.setRefundStatus(order.getRefundStatus());
                    vo.setRefundAmount(order.getRefundAmount());
                }
            }
            voList.add(vo);
        }
        return AjaxResult.success(voList);
    }
}
