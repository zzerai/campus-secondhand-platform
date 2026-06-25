package com.ruoyi.web.controller.trade;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
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
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.service.ITrTradeGoodsService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.ruoyi.trade.domain.vo.BatchAuditResult;
import com.ruoyi.trade.domain.vo.ImportResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 闲置商品Controller
 *
 * @author ruoyi
 * @date 2026-05-12
 */
@Tag(name = "闲置商品管理")
@RestController
@RequestMapping("/trade/goods")
@Slf4j
public class TrTradeGoodsController extends BaseController
{
    @Autowired
    private ITrTradeGoodsService trTradeGoodsService;

    @Operation(summary = "分页查询商品列表（含分类/卖家联表信息）")
    @PreAuthorize("@ss.hasPermi('trade:goods:list')")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeGoods trTradeGoods)
    {
        startPage();
        List<TrTradeGoods> list = trTradeGoodsService.selectTrTradeGoodsList(trTradeGoods);
        return getDataTable(list);
    }

    @Operation(summary = "导出商品列表")
    @PreAuthorize("@ss.hasPermi('trade:goods:export')")
    @Log(title = "闲置商品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TrTradeGoods trTradeGoods)
    {
        List<TrTradeGoods> list = trTradeGoodsService.selectTrTradeGoodsList(trTradeGoods);
        ExcelUtil<TrTradeGoods> util = new ExcelUtil<TrTradeGoods>(TrTradeGoods.class);
        util.exportExcel(response, list, "闲置商品数据");
    }

    @Operation(summary = "查询商品详情（含图片列表）")
    @PreAuthorize("@ss.hasPermi('trade:goods:query')")
    @GetMapping(value = "/{goodsId}")
    public AjaxResult getInfo(@PathVariable("goodsId") Long goodsId)
    {
        return success(trTradeGoodsService.selectTrTradeGoodsByGoodsId(goodsId));
    }

    @Operation(summary = "新增商品")
    @PreAuthorize("@ss.hasPermi('trade:goods:add')")
    @Log(title = "闲置商品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TrTradeGoods trTradeGoods)
    {
        return toAjax(trTradeGoodsService.insertTrTradeGoods(trTradeGoods));
    }

    @Operation(summary = "修改商品")
    @PreAuthorize("@ss.hasPermi('trade:goods:edit')")
    @Log(title = "闲置商品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TrTradeGoods trTradeGoods)
    {
        return toAjax(trTradeGoodsService.updateTrTradeGoods(trTradeGoods));
    }

    @Operation(summary = "审核商品（通过/拒绝，可单条/批量）",
            description = "返回 BatchAuditResult：success/failure/errors，单条审核失败会抛异常由全局异常处理")
    @PreAuthorize("@ss.hasPermi('trade:goods:audit')")
    @Log(title = "闲置商品-审核", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody AuditRequest body)
    {
        if (body == null || body.getGoodsStatus() == null)
        {
            return error("缺少审核状态");
        }
        BatchAuditResult result;
        if (body.getGoodsIds() != null && body.getGoodsIds().length > 0)
        {
            result = trTradeGoodsService.batchAuditGoods(
                    body.getGoodsIds(), body.getGoodsStatus(), body.getAuditRemark());
        }
        else if (body.getGoodsId() != null)
        {
            // 单条审核：失败抛异常由全局处理；成功统一包成 BatchAuditResult 便于前端复用解析逻辑
            int rows = trTradeGoodsService.auditGoods(
                    body.getGoodsId(), body.getGoodsStatus(), body.getAuditRemark());
            result = new BatchAuditResult();
            if (rows > 0)
            {
                result.incSuccess();
            }
            else
            {
                result.addError(body.getGoodsId(), "数据库未更新");
            }
        }
        else
        {
            return error("缺少商品ID");
        }
        return AjaxResult.success(result);
    }

    @Operation(summary = "强制下架商品")
    @PreAuthorize("@ss.hasPermi('trade:goods:offline')")
    @Log(title = "闲置商品-下架", businessType = BusinessType.UPDATE)
    @PutMapping("/offline/{goodsId}")
    public AjaxResult offline(@PathVariable("goodsId") Long goodsId)
    {
        return toAjax(trTradeGoodsService.offlineGoods(goodsId));
    }

    @Operation(summary = "恢复上架商品（仅已下架的可恢复）")
    @PreAuthorize("@ss.hasPermi('trade:goods:offline')")
    @Log(title = "闲置商品-上架", businessType = BusinessType.UPDATE)
    @PutMapping("/online/{goodsId}")
    public AjaxResult online(@PathVariable("goodsId") Long goodsId)
    {
        return toAjax(trTradeGoodsService.onlineGoods(goodsId));
    }

    @Operation(summary = "删除商品（逻辑删除）")
    @PreAuthorize("@ss.hasPermi('trade:goods:remove')")
    @Log(title = "闲置商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{goodsIds}")
    public AjaxResult remove(@PathVariable Long[] goodsIds)
    {
        return toAjax(trTradeGoodsService.deleteTrTradeGoodsByGoodsIds(goodsIds));
    }

    /**
     * 下载商品导入模板
     */
    @PreAuthorize("@ss.hasPermi('trade:goods:import')")
    @GetMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<TrTradeGoods> util = new ExcelUtil<>(TrTradeGoods.class);
        util.importTemplateExcel(response, "商品导入模板");
    }

    /**
     * 导入商品（支持新增/更新）
     *
     * @param file Excel文件
     * @param updateSupport 是否支持更新（true=存在则更新，false=存在则跳过）
     * @return 导入结果
     */
    @PreAuthorize("@ss.hasPermi('trade:goods:import')")
    @Log(title = "闲置商品", businessType = BusinessType.IMPORT)
    @PostMapping("/import")
    public AjaxResult importData(@RequestParam("file") MultipartFile file, @RequestParam(value = "updateSupport", required = false, defaultValue = "false") boolean updateSupport)
    {
        if (file == null || file.isEmpty())
        {
            return error("上传文件为空，请选择Excel文件");
        }
        ExcelUtil<TrTradeGoods> util = new ExcelUtil<>(TrTradeGoods.class);
        try
        {
            List<TrTradeGoods> goodsList = util.importExcel(file.getInputStream());
            ImportResult result = trTradeGoodsService.importGoods(goodsList, updateSupport);
            return success(result);
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message != null && (message.contains("OLE2") || message.contains("OOXML")))
            {
                return error("文件格式错误，请上传.xlsx或.xls格式的Excel文件");
            }
            log.error("导入商品失败", e);
            return error("导入失败：" + e.getMessage());
        }
    }

    /** 审核请求体（单条传 goodsId，批量传 goodsIds）。 */
    public static class AuditRequest
    {
        private Long goodsId;
        private Long[] goodsIds;
        /** 目标状态：'1' 通过、'2' 拒绝 */
        private String goodsStatus;
        private String auditRemark;

        public Long getGoodsId() { return goodsId; }
        public void setGoodsId(Long goodsId) { this.goodsId = goodsId; }
        public Long[] getGoodsIds() { return goodsIds; }
        public void setGoodsIds(Long[] goodsIds) { this.goodsIds = goodsIds; }
        public String getGoodsStatus() { return goodsStatus; }
        public void setGoodsStatus(String goodsStatus) { this.goodsStatus = goodsStatus; }
        public String getAuditRemark() { return auditRemark; }
        public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
    }
}
