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
import com.ruoyi.trade.domain.TrTradeGoodsImage;
import com.ruoyi.trade.service.ITrTradeGoodsImageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 商品图片Controller
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "商品图片")
@RestController
@RequestMapping("/trade/image")
public class TrTradeGoodsImageController extends BaseController
{
    @Autowired
    private ITrTradeGoodsImageService trTradeGoodsImageService;

    /**
     * 查询商品图片列表
     */
    @PreAuthorize("@ss.hasPermi('trade:image:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeGoodsImage trTradeGoodsImage)
    {
        startPage();
        List<TrTradeGoodsImage> list = trTradeGoodsImageService.selectTrTradeGoodsImageList(trTradeGoodsImage);
        return getDataTable(list);
    }

    /**
     * 导出商品图片列表
     */
    @PreAuthorize("@ss.hasPermi('trade:image:export')")
    @Log(title = "商品图片", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeGoodsImage trTradeGoodsImage)
    {
        List<TrTradeGoodsImage> list = trTradeGoodsImageService.selectTrTradeGoodsImageList(trTradeGoodsImage);
        ExcelUtil<TrTradeGoodsImage> util = new ExcelUtil<TrTradeGoodsImage>(TrTradeGoodsImage.class);
        util.exportExcel(response, list, "商品图片数据");
    }

    /**
     * 获取商品图片详细信息
     */
    @PreAuthorize("@ss.hasPermi('trade:image:query')")
    @GetMapping(value = "/{imageId}")
    public AjaxResult getInfo(@PathVariable("imageId") Long imageId)
    {
        return success(trTradeGoodsImageService.selectTrTradeGoodsImageByImageId(imageId));
    }

    /**
     * 新增商品图片
     */
    @PreAuthorize("@ss.hasPermi('trade:image:add')")
    @Log(title = "商品图片", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeGoodsImage trTradeGoodsImage)
    {
        return toAjax(trTradeGoodsImageService.insertTrTradeGoodsImage(trTradeGoodsImage));
    }

    /**
     * 修改商品图片
     */
    @PreAuthorize("@ss.hasPermi('trade:image:edit')")
    @Log(title = "商品图片", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeGoodsImage trTradeGoodsImage)
    {
        return toAjax(trTradeGoodsImageService.updateTrTradeGoodsImage(trTradeGoodsImage));
    }

    /**
     * 删除商品图片
     */
    @PreAuthorize("@ss.hasPermi('trade:image:remove')")
    @Log(title = "商品图片", businessType = BusinessType.DELETE)
	@DeleteMapping("/{imageIds}")
    public AjaxResult remove(@PathVariable Long[] imageIds)
    {
        return toAjax(trTradeGoodsImageService.deleteTrTradeGoodsImageByImageIds(imageIds));
    }
}
