package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.dto.AppGoodsPublishDto;
import com.ruoyi.trade.domain.dto.AppGoodsUpdateDto;
import com.ruoyi.trade.service.IAppGoodsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Mobile goods APIs.
 */
@RestController
@RequestMapping("/app/goods")
@Tag(name = "移动端商品接口")
public class AppGoodsController extends AppApiController
{
    @Autowired
    private IAppGoodsService tradeGoodsService;

    /**
     * Public goods list.
     */
    @Anonymous
    @Operation(summary = "移动端商品列表", description = "匿名查询已上架商品，支持分类、关键字和排序筛选")
    @GetMapping("/list")
    public TableDataInfo list(TrTradeGoods goods)
    {
        startPage();
        List<TrTradeGoods> list = tradeGoodsService.selectAppGoodsList(goods);
        return getDataTable(list);
    }

    /**
     * Public goods detail.
     */
    @Anonymous
    @Operation(summary = "移动端商品详情", description = "查询商品详情：匿名及非卖家仅可查看已上架商品，卖家可查看自己任意状态的商品")
    @GetMapping("/detail/{goodsId}")
    public AjaxResult detail(@Parameter(description = "商品ID", required = true) @PathVariable Long goodsId)
    {
        TrTradeGoods goods = tradeGoodsService.selectTradeGoodsById(goodsId, currentUserIdOrNull());
        return goods == null ? error("商品不存在或已下架") : success(goods);
    }

    /**
     * My goods list.
     */
    @Operation(summary = "我的商品列表", description = "查询当前登录用户发布的商品")
    @GetMapping("/myList")
    public TableDataInfo myList(TrTradeGoods goods)
    {
        startPage();
        goods.setSellerId(getUserId());
        List<TrTradeGoods> list = tradeGoodsService.selectMyGoodsList(goods);
        return getDataTable(list);
    }

    /**
     * Publish goods.
     */
    @Log(title = "移动端商品发布", businessType = BusinessType.INSERT)
    @Operation(summary = "发布商品", description = "当前登录用户发布二手商品")
    @PostMapping("/publish")
    public AjaxResult publish(@RequestBody AppGoodsPublishDto dto)
    {
        return toAjax(tradeGoodsService.publishGoods(dto, getUserId(), getUsername()));
    }

    /**
     * Update my goods.
     */
    @Log(title = "移动端商品修改", businessType = BusinessType.UPDATE)
    @Operation(summary = "修改商品", description = "当前登录用户修改自己发布的商品")
    @PutMapping("/update")
    public AjaxResult update(@RequestBody AppGoodsUpdateDto dto)
    {
        return toAjax(tradeGoodsService.updateGoods(dto, getUserId(), getUsername()));
    }

    /**
     * Offline my goods.
     */
    @Log(title = "移动端商品下架", businessType = BusinessType.UPDATE)
    @Operation(summary = "下架商品", description = "当前登录用户下架自己发布的商品")
    @PutMapping("/offline/{goodsId}")
    public AjaxResult offline(@Parameter(description = "商品ID", required = true) @PathVariable Long goodsId)
    {
        return toAjax(tradeGoodsService.offlineGoods(goodsId, getUserId()));
    }

    /**
     * Delete my goods.
     */
    @Log(title = "移动端商品删除", businessType = BusinessType.DELETE)
    @Operation(summary = "删除商品", description = "当前登录用户删除自己发布且未售出的商品")
    @DeleteMapping("/{goodsId}")
    public AjaxResult remove(@Parameter(description = "商品ID", required = true) @PathVariable Long goodsId)
    {
        return toAjax(tradeGoodsService.deleteGoods(goodsId, getUserId()));
    }
}
