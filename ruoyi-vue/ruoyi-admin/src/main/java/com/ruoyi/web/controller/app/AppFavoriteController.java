package com.ruoyi.web.controller.app;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.service.ITrTradeFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端商品收藏接口。
 *
 * <p>四个端点：添加 / 取消 / 我的收藏 / 是否已收藏。
 * 全部需要登录态，未在 {@code SecurityConfig} 放行。</p>
 */
@RestController
@RequestMapping("/app/favorite")
@Tag(name = "移动端商品收藏")
public class AppFavoriteController extends AppApiController
{
    @Autowired
    private ITrTradeFavoriteService favoriteService;

    /**
     * 收藏商品（幂等）。
     */
    @Log(title = "移动端商品收藏", businessType = BusinessType.INSERT)
    @Operation(summary = "添加收藏", description = "当前登录用户收藏指定商品；商品必须已上架且非本人发布")
    @PostMapping("/{goodsId}")
    public AjaxResult add(@Parameter(description = "商品ID", required = true) @PathVariable Long goodsId)
    {
        favoriteService.addFavorite(getUserId(), goodsId, getUsername());
        return success();
    }

    /**
     * 取消收藏（幂等）。
     */
    @Log(title = "移动端取消收藏", businessType = BusinessType.DELETE)
    @Operation(summary = "取消收藏", description = "当前登录用户取消对指定商品的收藏；幂等")
    @DeleteMapping("/{goodsId}")
    public AjaxResult remove(@Parameter(description = "商品ID", required = true) @PathVariable Long goodsId)
    {
        favoriteService.removeFavorite(getUserId(), goodsId, getUsername());
        return success();
    }

    /**
     * 我的收藏列表。
     */
    @Operation(summary = "我的收藏列表", description = "按收藏时间倒序返回当前登录用户收藏的商品；包含已下架/已售出商品以便前端显示状态提示")
    @GetMapping("/myList")
    public TableDataInfo myList()
    {
        startPage();
        List<TrTradeGoods> list = favoriteService.selectMyFavoriteGoods(getUserId());
        return getDataTable(list);
    }

    /**
     * 查询是否已收藏。
     */
    @Operation(summary = "是否已收藏", description = "返回当前登录用户是否已收藏该商品")
    @GetMapping("/check/{goodsId}")
    public AjaxResult check(@Parameter(description = "商品ID", required = true) @PathVariable Long goodsId)
    {
        return success(favoriteService.existsFavorite(getUserId(), goodsId));
    }
}
