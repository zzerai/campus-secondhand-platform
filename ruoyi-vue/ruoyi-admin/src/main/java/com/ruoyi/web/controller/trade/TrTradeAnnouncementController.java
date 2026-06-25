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
import com.ruoyi.trade.domain.TrTradeAnnouncement;
import com.ruoyi.trade.service.ITrTradeAnnouncementService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 交易公告Controller
 * 
 * @author ruoyi
 * @date 2026-05-25
 */
@RestController
@RequestMapping("/trade/announcement")
public class TrTradeAnnouncementController extends BaseController
{
    @Autowired
    private ITrTradeAnnouncementService trTradeAnnouncementService;

    /**
     * 查询交易公告列表
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeAnnouncement trTradeAnnouncement)
    {
        startPage();
        List<TrTradeAnnouncement> list = trTradeAnnouncementService.selectTrTradeAnnouncementList(trTradeAnnouncement);
        return getDataTable(list);
    }

    /**
     * 导出交易公告列表
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:export')")
    @Log(title = "交易公告", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeAnnouncement trTradeAnnouncement)
    {
        List<TrTradeAnnouncement> list = trTradeAnnouncementService.selectTrTradeAnnouncementList(trTradeAnnouncement);
        ExcelUtil<TrTradeAnnouncement> util = new ExcelUtil<TrTradeAnnouncement>(TrTradeAnnouncement.class);
        util.exportExcel(response, list, "交易公告数据");
    }

    /**
     * 获取交易公告详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:query')")
    @GetMapping(value = "/{announcementId}")
    public AjaxResult getInfo(@PathVariable("announcementId") Long announcementId)
    {
        return success(trTradeAnnouncementService.selectTrTradeAnnouncementByAnnouncementId(announcementId));
    }

    /**
     * 新增交易公告
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:add')")
    @Log(title = "交易公告", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeAnnouncement trTradeAnnouncement)
    {
        return toAjax(trTradeAnnouncementService.insertTrTradeAnnouncement(trTradeAnnouncement));
    }

    /**
     * 修改交易公告
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:edit')")
    @Log(title = "交易公告", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeAnnouncement trTradeAnnouncement)
    {
        return toAjax(trTradeAnnouncementService.updateTrTradeAnnouncement(trTradeAnnouncement));
    }

    /**
     * 删除交易公告
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:remove')")
    @Log(title = "交易公告", businessType = BusinessType.DELETE)
	@DeleteMapping("/{announcementIds}")
    public AjaxResult remove(@PathVariable Long[] announcementIds)
    {
        return toAjax(trTradeAnnouncementService.deleteTrTradeAnnouncementByAnnouncementIds(announcementIds));
    }

    /**
     * 发布公告
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:publish')")
    @Log(title = "发布公告", businessType = BusinessType.UPDATE)
    @PostMapping("/publish/{announcementId}")
    public AjaxResult publish(@PathVariable("announcementId") Long announcementId)
    {
        return toAjax(trTradeAnnouncementService.publishAnnouncement(announcementId));
    }

    /**
     * 撤回公告
     */
    @PreAuthorize("@ss.hasPermi('trade:announcement:edit')")
    @Log(title = "撤回公告", businessType = BusinessType.UPDATE)
    @PostMapping("/retract/{announcementId}")
    public AjaxResult retract(@PathVariable("announcementId") Long announcementId)
    {
        return toAjax(trTradeAnnouncementService.retractAnnouncement(announcementId));
    }
}
