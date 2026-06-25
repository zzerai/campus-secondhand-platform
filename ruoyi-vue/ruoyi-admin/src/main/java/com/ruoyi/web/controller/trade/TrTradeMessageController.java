package com.ruoyi.web.controller.trade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrTradeMessage;
import com.ruoyi.trade.domain.dto.AppMessageSendDto;
import com.ruoyi.trade.domain.vo.AppConversationVo;
import com.ruoyi.trade.domain.vo.AppMessageVo;
import com.ruoyi.trade.service.IAppMessageService;
import com.ruoyi.trade.service.ITrTradeMessageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 私信消息Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "私信消息")
@RestController
@RequestMapping("/trade/message")
public class TrTradeMessageController extends BaseController
{
    @Autowired
    private ITrTradeMessageService trTradeMessageService;

    @Autowired
    private IAppMessageService appMessageService;

    @Value("${trade.admin-student-id:1}")
    private Long adminStudentId;

    /**
     * 查询私信消息列表
     */
    @PreAuthorize("@ss.hasPermi('trade:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeMessage trTradeMessage)
    {
        startPage();
        List<TrTradeMessage> list = trTradeMessageService.selectTrTradeMessageList(trTradeMessage);
        return getDataTable(list);
    }

    /**
     * 导出私信消息列表
     */
    @PreAuthorize("@ss.hasPermi('trade:message:export')")
    @Log(title = "私信消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeMessage trTradeMessage)
    {
        List<TrTradeMessage> list = trTradeMessageService.selectTrTradeMessageList(trTradeMessage);
        ExcelUtil<TrTradeMessage> util = new ExcelUtil<TrTradeMessage>(TrTradeMessage.class);
        util.exportExcel(response, list, "私信消息数据");
    }

    /**
     * 获取私信消息详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:message:query')")
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId)
    {
        return success(trTradeMessageService.selectTrTradeMessageByMessageId(messageId));
    }

    /**
     * 新增私信消息
     */
    @PreAuthorize("@ss.hasPermi('trade:message:add')")
    @Log(title = "私信消息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeMessage trTradeMessage)
    {
        return toAjax(trTradeMessageService.insertTrTradeMessage(trTradeMessage));
    }

    /**
     * 修改私信消息
     */
    @PreAuthorize("@ss.hasPermi('trade:message:edit')")
    @Log(title = "私信消息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeMessage trTradeMessage)
    {
        return toAjax(trTradeMessageService.updateTrTradeMessage(trTradeMessage));
    }

    /**
     * 删除私信消息
     */
    @PreAuthorize("@ss.hasPermi('trade:message:remove')")
    @Log(title = "私信消息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(trTradeMessageService.deleteTrTradeMessageByMessageIds(messageIds));
    }

    // ============================================================
    // 用户咨询（管理员联系）接口
    // ============================================================

    /**
     * 获取管理员学生用户ID。
     */
    @PreAuthorize("@ss.hasPermi('trade:message:list')")
    @GetMapping("/admin/info")
    public AjaxResult adminInfo()
    {
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", adminStudentId);
        return success(data);
    }

    /**
     * 查询管理员咨询会话列表（goods_id=0）。
     */
    @PreAuthorize("@ss.hasPermi('trade:message:list')")
    @GetMapping("/admin/conversations")
    public TableDataInfo adminConversations()
    {
        startPage();
        List<AppConversationVo> list = appMessageService.listAdminContactConversations();
        return getDataTable(list);
    }

    /**
     * 查询某用户的管理员咨询消息历史（goods_id=0）。
     */
    @PreAuthorize("@ss.hasPermi('trade:message:list')")
    @GetMapping("/admin/messages")
    public TableDataInfo adminMessages(
            @RequestParam Long userId,
            @RequestParam Long adminId)
    {
        startPage();
        List<AppMessageVo> list = appMessageService.listAdminContactMessages(adminId, userId);
        return getDataTable(list);
    }

    /**
     * 标记管理员咨询会话已读。
     */
    @PreAuthorize("@ss.hasPermi('trade:message:edit')")
    @PostMapping("/admin/read")
    public AjaxResult adminMarkRead(@RequestParam Long userId)
    {
        String username = SecurityUtils.getUsername();
        int marked = appMessageService.markConversationRead(adminStudentId, username, userId, 0L);
        Map<String, Object> data = new HashMap<>();
        data.put("markedCount", marked);
        return success(data);
    }

    /**
     * 管理员回复用户咨询。
     */
    @PreAuthorize("@ss.hasPermi('trade:message:add')")
    @Log(title = "用户咨询回复", businessType = BusinessType.INSERT)
    @PostMapping("/admin/reply")
    public AjaxResult adminReply(@RequestBody AppMessageSendDto dto)
    {
        String username = SecurityUtils.getUsername();
        Long messageId = appMessageService.sendAdminReply(adminStudentId, username, dto);
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        return success(data);
    }
}
