package com.ruoyi.web.controller.trade;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.trade.domain.TrCreditLog;
import com.ruoyi.trade.domain.vo.CreditApplyResult;
import com.ruoyi.trade.service.ICreditScoreService;
import com.ruoyi.trade.utils.CreditConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 学生信用分管理 Controller。
 *
 * @author thr
 */
@Tag(name = "学生信用分", description = "管理员手动调整信用分 + 信用流水查询")
@RestController
@RequestMapping("/trade/credit")
public class TrCreditController extends BaseController
{
    @Autowired
    private ICreditScoreService creditScoreService;

    /**
     * 管理员手动调整信用分（统一走信用引擎：自动写流水、下穿阈值同样触发封禁）。
     */
    @Operation(summary = "手动调整信用分")
    @PreAuthorize("@ss.hasPermi('trade:credit:adjust')")
    @Log(title = "信用分手动调整", businessType = BusinessType.UPDATE)
    @PostMapping("/adjust")
    public AjaxResult adjust(@RequestBody AdjustRequest body)
    {
        if (body == null || body.getUserId() == null || body.getChangeValue() == null)
        {
            return error("缺少参数：userId / changeValue 必填");
        }
        if (body.getChangeValue() == 0)
        {
            return error("调整分值不能为 0");
        }
        if (StringUtils.isEmpty(body.getReason()))
        {
            return error("必须填写调整原因");
        }
        CreditApplyResult result = creditScoreService.applyChange(
                body.getUserId(), CreditConstants.TYPE_ADMIN_ADJUST, body.getChangeValue(),
                CreditConstants.BIZ_ADMIN, null, body.getReason());
        return success(result);
    }

    /**
     * 查询信用流水列表。
     */
    @Operation(summary = "信用流水列表")
    @PreAuthorize("@ss.hasPermi('trade:credit:list')")
    @GetMapping("/log/list")
    public TableDataInfo logList(TrCreditLog trCreditLog)
    {
        startPage();
        List<TrCreditLog> list = creditScoreService.selectCreditLogList(trCreditLog);
        return getDataTable(list);
    }

    /** 手动调整请求体。 */
    public static class AdjustRequest
    {
        private Long userId;
        /** 增减分值（可负，不可为 0） */
        private Integer changeValue;
        /** 调整原因（必填） */
        private String reason;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Integer getChangeValue() { return changeValue; }
        public void setChangeValue(Integer changeValue) { this.changeValue = changeValue; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
