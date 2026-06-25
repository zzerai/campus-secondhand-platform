package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.trade.domain.TrTradeAnnouncement;
import com.ruoyi.trade.service.ITrTradeAnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端公告控制器
 *
 * @author ruoyi
 */
@Tag(name = "移动端公告", description = "获取公告列表")
@RestController
@RequestMapping("/app/notice")
public class AppNoticeController extends BaseController
{
    @Autowired
    private ITrTradeAnnouncementService announcementService;

    /**
     * 获取公告列表
     */
    @Operation(summary = "获取公告列表", description = "获取所有已发布的公告，按置顶优先、发布时间倒序排列")
    @GetMapping("/list")
    public AjaxResult getNoticeList()
    {
        List<TrTradeAnnouncement> list = announcementService.selectAppNoticeList();
        return success(list);
    }

    /**
     * 获取公告详情
     */
    @Operation(summary = "获取公告详情", description = "根据公告ID查看完整公告内容")
    @GetMapping("/detail/{announcementId}")
    public AjaxResult getNoticeDetail(@PathVariable("announcementId") Long announcementId)
    {
        TrTradeAnnouncement announcement = announcementService.selectTrTradeAnnouncementByAnnouncementId(announcementId);
        if (announcement == null || !"0".equals(announcement.getDelFlag()) || !"1".equals(announcement.getPublishStatus()))
        {
            return error("公告不存在");
        }
        return success(announcement);
    }
}
