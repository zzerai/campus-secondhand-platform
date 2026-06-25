package com.ruoyi.web.controller.ai;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.service.ITrAiAuditRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 争议仲裁 AI 记录Controller
 *
 * <p>AI 记录统一存于 tr_ai_audit_record，本接口在通用查询基础上强制 businessType=dispute，
 * 仅返回争议仲裁相关的 AI 调用记录。</p>
 *
 * @author ruoyi
 * @date 2026-05-20
 */
@Tag(name = "争议仲裁 AI 记录")
@RestController
@RequestMapping("/ai/dispute/record")
public class AiDisputeRecordController extends BaseController
{
    /** 业务类型：争议 */
    private static final String BUSINESS_TYPE_DISPUTE = "dispute";

    @Autowired
    private ITrAiAuditRecordService trAiAuditRecordService;

    @Operation(summary = "分页查询争议仲裁 AI 记录列表")
    @PreAuthorize("@ss.hasPermi('trade:aiAuditRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrAiAuditRecord trAiAuditRecord)
    {
        trAiAuditRecord.setBusinessType(BUSINESS_TYPE_DISPUTE);
        startPage();
        List<TrAiAuditRecord> list = trAiAuditRecordService.selectTrAiAuditRecordList(trAiAuditRecord);
        return getDataTable(list);
    }
}
