package com.ruoyi.web.controller.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.dto.AppMessageSendDto;
import com.ruoyi.trade.domain.vo.AppConversationVo;
import com.ruoyi.trade.domain.vo.AppMessageVo;
import com.ruoyi.trade.service.IAppMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端买卖双方私信咨询接口。
 *
 * <p>会话粒度为 (我, 对方, 商品)。所有接口均需登录态，未在 {@code SecurityConfig} 放行。
 * 实时性走 HTTP 短轮询：聊天页 5s 拉 {@link #messages}，主页 10s 拉 {@link #unreadCount}。</p>
 */
@RestController
@RequestMapping("/app/message")
@Tag(name = "移动端私信咨询")
public class AppMessageController extends AppApiController
{
    @Autowired
    private IAppMessageService appMessageService;

    /**
     * 发送一条私信。
     */
    @Log(title = "移动端发送私信", businessType = BusinessType.INSERT)
    @Operation(summary = "发送私信",
               description = "向指定用户发送一条与某商品相关的私信；sender 由登录态推导，禁止自发自")
    @PostMapping("/send")
    public AjaxResult send(@RequestBody AppMessageSendDto dto)
    {
        Long messageId = appMessageService.sendMessage(getUserId(), getUsername(), dto);
        Map<String, Object> data = new HashMap<>();
        data.put("messageId", messageId);
        return success(data);
    }

    /**
     * 我的会话列表（按商品维度聚合）。
     */
    @Operation(summary = "我的会话列表",
               description = "按 (对方, 商品) 聚合的会话；按最后一条消息时间倒序；含未读数、商品摘要、对方昵称头像")
    @GetMapping("/conversations")
    public TableDataInfo conversations()
    {
        startPage();
        List<AppConversationVo> list = appMessageService.listConversations(getUserId());
        return getDataTable(list);
    }

    /**
     * 单会话消息历史。
     */
    @Operation(summary = "会话消息历史",
               description = "按 create_time 倒序返回 (我, 对方, 商品) 会话的消息；mine 字段已由后端计算")
    @GetMapping("/messages")
    public TableDataInfo messages(
            @Parameter(description = "对方用户ID", required = true) @RequestParam Long peerId,
            @Parameter(description = "关联商品ID", required = true) @RequestParam Long goodsId)
    {
        startPage();
        List<AppMessageVo> list = appMessageService.listMessages(getUserId(), peerId, goodsId);
        return getDataTable(list);
    }

    /**
     * 标记会话已读。
     */
    @Log(title = "移动端标记会话已读", businessType = BusinessType.UPDATE)
    @Operation(summary = "标记会话已读",
               description = "把对方在该会话中发给我的所有未读消息批量标记为已读；幂等")
    @PostMapping("/read")
    public AjaxResult markRead(
            @Parameter(description = "对方用户ID", required = true) @RequestParam Long peerId,
            @Parameter(description = "关联商品ID", required = true) @RequestParam Long goodsId)
    {
        int marked = appMessageService.markConversationRead(getUserId(), getUsername(), peerId, goodsId);
        Map<String, Object> data = new HashMap<>();
        data.put("markedCount", marked);
        return success(data);
    }

    /**
     * 批量删除消息（仅发送者可删），同时清理图片文件。
     */
    @Log(title = "移动端删除私信", businessType = BusinessType.DELETE)
    @Operation(summary = "批量删除消息",
               description = "仅消息发送者可删除自己的消息；若消息含图片则同时删除图片文件")
    @PostMapping("/delete/{messageIds}")
    public AjaxResult delete(
            @Parameter(description = "消息ID列表，逗号分隔", required = true) @PathVariable Long[] messageIds)
    {
        int deleted = appMessageService.deleteMessages(getUserId(), messageIds);
        Map<String, Object> data = new HashMap<>();
        data.put("deletedCount", deleted);
        return success(data);
    }

    /**
     * 未读消息总数。
     */
    @Operation(summary = "未读消息总数",
               description = "供首页/底 tab 的 badge 展示；前端按 10s 轮询调用")
    @GetMapping("/unread/count")
    public AjaxResult unreadCount()
    {
        long count = appMessageService.countUnread(getUserId());
        Map<String, Object> data = new HashMap<>();
        data.put("count", count);
        return success(data);
    }

    /**
     * 获取管理员信息（用于"联系管理员"功能）。
     */
    @Operation(summary = "管理员信息",
               description = "返回管理员的 userId / nickname / avatar，前端据此建立 goodsId=0 的管理员会话")
    @GetMapping("/admin/info")
    public AjaxResult adminInfo()
    {
        return success(appMessageService.getAdminInfo());
    }
}
