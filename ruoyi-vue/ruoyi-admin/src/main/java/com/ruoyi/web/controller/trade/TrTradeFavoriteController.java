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
import com.ruoyi.trade.domain.TrTradeFavorite;
import com.ruoyi.trade.service.ITrTradeFavoriteService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 商品收藏Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "商品收藏")
@RestController
@RequestMapping("/trade/favorite")
public class TrTradeFavoriteController extends BaseController
{
    @Autowired
    private ITrTradeFavoriteService trTradeFavoriteService;

    /**
     * 查询商品收藏列表
     */
    @PreAuthorize("@ss.hasPermi('trade:favorite:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeFavorite trTradeFavorite)
    {
        startPage();
        List<TrTradeFavorite> list = trTradeFavoriteService.selectTrTradeFavoriteList(trTradeFavorite);
        return getDataTable(list);
    }

    /**
     * 导出商品收藏列表
     */
    @PreAuthorize("@ss.hasPermi('trade:favorite:export')")
    @Log(title = "商品收藏", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeFavorite trTradeFavorite)
    {
        List<TrTradeFavorite> list = trTradeFavoriteService.selectTrTradeFavoriteList(trTradeFavorite);
        ExcelUtil<TrTradeFavorite> util = new ExcelUtil<TrTradeFavorite>(TrTradeFavorite.class);
        util.exportExcel(response, list, "商品收藏数据");
    }

    /**
     * 获取商品收藏详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:favorite:query')")
    @GetMapping(value = "/{favoriteId}")
    public AjaxResult getInfo(@PathVariable("favoriteId") Long favoriteId)
    {
        return success(trTradeFavoriteService.selectTrTradeFavoriteByFavoriteId(favoriteId));
    }

    /**
     * 新增商品收藏
     */
    @PreAuthorize("@ss.hasPermi('trade:favorite:add')")
    @Log(title = "商品收藏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeFavorite trTradeFavorite)
    {
        return toAjax(trTradeFavoriteService.insertTrTradeFavorite(trTradeFavorite));
    }

    /**
     * 修改商品收藏
     */
    @PreAuthorize("@ss.hasPermi('trade:favorite:edit')")
    @Log(title = "商品收藏", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeFavorite trTradeFavorite)
    {
        return toAjax(trTradeFavoriteService.updateTrTradeFavorite(trTradeFavorite));
    }

    /**
     * 删除商品收藏
     */
    @PreAuthorize("@ss.hasPermi('trade:favorite:remove')")
    @Log(title = "商品收藏", businessType = BusinessType.DELETE)
	@DeleteMapping("/{favoriteIds}")
    public AjaxResult remove(@PathVariable Long[] favoriteIds)
    {
        return toAjax(trTradeFavoriteService.deleteTrTradeFavoriteByFavoriteIds(favoriteIds));
    }
}
