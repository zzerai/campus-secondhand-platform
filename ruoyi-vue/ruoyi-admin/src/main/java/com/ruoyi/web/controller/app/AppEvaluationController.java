package com.ruoyi.web.controller.app;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.trade.domain.TrTradeEvaluation;
import com.ruoyi.trade.domain.vo.AppEvaluationVo;
import com.ruoyi.trade.mapper.TrTradeEvaluationMapper;
import com.ruoyi.trade.service.ITrTradeEvaluationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 移动端评价接口
 */
@RestController
@RequestMapping("/app/evaluation")
@Tag(name = "移动端评价接口")
public class AppEvaluationController extends AppApiController
{
    @Autowired
    private ITrTradeEvaluationService evaluationService;

    @Autowired
    private TrTradeEvaluationMapper evaluationMapper;

    /**
     * 当前用户评价概览（综合均分 + 各星级数量分布 + 收到/发出总数）。
     */
    @Operation(summary = "评价概览", description = "获取当前用户的评价均分、各星级分布、收到和发出的评价总数")
    @GetMapping("/myScore")
    public AjaxResult myScore()
    {
        Long userId = getUserId();
        BigDecimal avg = evaluationMapper.avgScoreByUserId(userId);
        List<Map<String, Object>> dist = evaluationMapper.scoreDistributionByUserId(userId);

        Map<Integer, Integer> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0);
        int totalReceived = 0;
        if (dist != null)
        {
            for (Map<String, Object> row : dist)
            {
                Object scoreObj = row.get("score");
                Object cntObj = row.get("cnt");
                if (scoreObj != null && cntObj != null)
                {
                    int star = ((Number) scoreObj).intValue();
                    int cnt = ((Number) cntObj).intValue();
                    distribution.put(star, cnt);
                    totalReceived += cnt;
                }
            }
        }

        TrTradeEvaluation sentQuery = new TrTradeEvaluation();
        sentQuery.setFromUserId(userId);
        List<TrTradeEvaluation> sentList = evaluationService.selectTrTradeEvaluationList(sentQuery);
        int totalSent = sentList != null ? sentList.size() : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("averageScore", avg != null ? avg.doubleValue() : 0.0);
        result.put("totalReceived", totalReceived);
        result.put("totalSent", totalSent);
        result.put("distribution", distribution);
        return success(result);
    }

    /**
     * 收到的评价列表（分页）。
     */
    @Operation(summary = "收到的评价", description = "分页查询当前用户收到的评价列表")
    @GetMapping("/received")
    public TableDataInfo received()
    {
        startPage();
        List<AppEvaluationVo> list = evaluationMapper.selectReceivedByUserId(getUserId());
        return getDataTable(list);
    }

    /**
     * 发出的评价列表（分页）。
     */
    @Operation(summary = "发出的评价", description = "分页查询当前用户发出的评价列表")
    @GetMapping("/sent")
    public TableDataInfo sent()
    {
        startPage();
        List<AppEvaluationVo> list = evaluationMapper.selectSentByUserId(getUserId());
        return getDataTable(list);
    }

    /**
     * 提交评价。
     */
    @Log(title = "移动端评价提交", businessType = BusinessType.INSERT)
    @Operation(summary = "提交评价", description = "对已完成的订单提交评价（含评分1-5和评价内容）")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody TrTradeEvaluation evaluation)
    {
        evaluation.setFromUserId(getUserId());
        return toAjax(evaluationService.insertTrTradeEvaluation(evaluation));
    }

    /**
     * 检查当前用户是否已对指定订单评价过。
     */
    @Operation(summary = "检查是否已评价", description = "检查当前用户是否已对指定订单评价过，返回 { evaluated: true/false }")
    @GetMapping("/check/{orderId}")
    public AjaxResult check(@Parameter(description = "订单ID", required = true) @PathVariable Long orderId)
    {
        int count = evaluationMapper.countByOrderAndFromUser(orderId, getUserId());
        Map<String, Boolean> result = new HashMap<>();
        result.put("evaluated", count > 0);
        return success(result);
    }
}
