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
import com.ruoyi.trade.domain.TrTradeCategory;
import com.ruoyi.trade.service.ITrTradeCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 商品分类Controller
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
@Tag(name = "商品分类管理")
@RestController
@RequestMapping("/trade/category")
public class TrTradeCategoryController extends BaseController
{
    @Autowired
    private ITrTradeCategoryService trTradeCategoryService;

    /**
     * 查询商品分类列表
     */
    @PreAuthorize("@ss.hasPermi('trade:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeCategory trTradeCategory)
    {
        startPage();
        List<TrTradeCategory> list = trTradeCategoryService.selectTrTradeCategoryList(trTradeCategory);
        return getDataTable(list);
    }

    /**
     * 导出商品分类列表
     */
    @PreAuthorize("@ss.hasPermi('trade:category:export')")
    @Log(title = "商品分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeCategory trTradeCategory)
    {
        List<TrTradeCategory> list = trTradeCategoryService.selectTrTradeCategoryList(trTradeCategory);
        ExcelUtil<TrTradeCategory> util = new ExcelUtil<TrTradeCategory>(TrTradeCategory.class);
        util.exportExcel(response, list, "商品分类数据");
    }

    /**
     * 获取商品分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(trTradeCategoryService.selectTrTradeCategoryByCategoryId(categoryId));
    }

    /**
     * 新增商品分类
     */
    @PreAuthorize("@ss.hasPermi('trade:category:add')")
    @Log(title = "商品分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeCategory trTradeCategory)
    {
        return toAjax(trTradeCategoryService.insertTrTradeCategory(trTradeCategory));
    }

    /**
     * 修改商品分类
     */
    @PreAuthorize("@ss.hasPermi('trade:category:edit')")
    @Log(title = "商品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeCategory trTradeCategory)
    {
        return toAjax(trTradeCategoryService.updateTrTradeCategory(trTradeCategory));
    }

    /**
     * 删除商品分类
     */
    @PreAuthorize("@ss.hasPermi('trade:category:remove')")
    @Log(title = "商品分类", businessType = BusinessType.DELETE)
	@DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(trTradeCategoryService.deleteTrTradeCategoryByCategoryIds(categoryIds));
    }

    /**
     * 分类状态修改（启用/停用）
     */
    @PreAuthorize("@ss.hasPermi('trade:category:edit')")
    @Log(title = "商品分类", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody TrTradeCategory category)
    {
        category.setUpdateBy(getUsername());
        return toAjax(trTradeCategoryService.updateCategoryStatus(category));
    }

    /**
     * 查询所有启用分类（前端下拉/级联选择用，不分页）
     */
    @PreAuthorize("@ss.hasPermi('trade:category:list')")
    @GetMapping("/listAll")
    public AjaxResult listAll()
    {
        return success(trTradeCategoryService.selectAllActiveCategories());
    }
}
