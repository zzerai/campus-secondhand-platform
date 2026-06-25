package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.domain.TrTradeDispute;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.vo.ai.DisputeArbitrationResult;
import com.ruoyi.trade.mapper.TrTradeDisputeMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.ITrAiDisputeArbitrationService;
import com.ruoyi.trade.service.ai.DisputeArbitrationAi;
import com.ruoyi.trade.utils.AiCallExecutor;

/**
 * AI 争议仲裁 Service 业务层处理。
 *
 * <p>提示词与 AI 调用细节由 {@link DisputeArbitrationAi}（langchain4j AiServices 代理）承担；
 * 本类负责：争议状态机校验 → 关联数据装载 → 占位符值兜底（含 goods 可空） →
 * AI 调用（带超时）→ POJO → Entity 映射 → 通过 {@link DisputePersistService} 事务落库。
 * 异步入口失败时回写 {@code [AI仲裁失败]} 到 {@code ai_analysis}，供管理员重试。</p>
 *
 * @author thr
 * @date 2026-05-20
 */
@Service
public class TrAiDisputeArbitrationServiceImpl implements ITrAiDisputeArbitrationService
{
    private static final Logger log = LoggerFactory.getLogger(TrAiDisputeArbitrationServiceImpl.class);

    private static final long AI_CALL_TIMEOUT_SECONDS = 30L;

    /** 争议状态：待AI分析（仅该状态允许进入 AI 仲裁，包括首次与"重试 AI"两种入口） */
    private static final String HANDLE_STATUS_PENDING = "0";

    /** ai_analysis 失败标记前缀，前端据此识别"AI 跑过但失败"并展示"重试 AI"按钮 */
    private static final String AI_FAILURE_PREFIX = "[AI仲裁失败] ";

    /** 失败信息写入 ai_analysis 时截断长度（DDL ai_analysis 限 500，留 20 字符给前缀） */
    private static final int AI_FAILURE_MSG_MAX = 480;

    /** AI 字段缺省兜底 */
    private static final String DEFAULT_ARBITRATE_LEVEL = "双方责任";
    private static final String DEFAULT_SUGGESTION      = "人工介入";
    private static final String DEFAULT_REASON          = "AI未提供明确仲裁理由";
    private static final String NOT_PROVIDED = "未提供";

    @Autowired
    private TrTradeDisputeMapper trTradeDisputeMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private DisputeArbitrationAi disputeArbitrationAi;

    @Autowired
    private DisputePersistService disputePersistService;

    @Override
    public TrAiAuditRecord arbitrateDispute(Long disputeId)
    {
        TrTradeDispute dispute = trTradeDisputeMapper.selectTrTradeDisputeByDisputeId(disputeId);
        if (dispute == null)
        {
            throw new ServiceException("争议记录不存在");
        }
        if (!HANDLE_STATUS_PENDING.equals(dispute.getHandleStatus()))
        {
            throw new ServiceException(
                    "争议当前状态不可AI仲裁，handleStatus=" + dispute.getHandleStatus());
        }

        TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderId(dispute.getOrderId());
        if (order == null)
        {
            throw new ServiceException("关联订单不存在");
        }

        TrTradeGoods goods = null;
        if (dispute.getGoodsId() != null)
        {
            goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(dispute.getGoodsId());
        }

        String inputDigest = buildInputDigest(dispute, order, goods);
        String goodsInfo = buildGoodsInfoBlock(goods);
        log.info("AI争议仲裁开始, disputeId={}", disputeId);

        // AI 调用放在事务外（本方法本身无 @Transactional），避免 30s 长耗时占住 DB 连接
        TrTradeGoods goodsRef = goods;
        TrTradeOrder orderRef = order;
        TrTradeDispute disputeRef = dispute;
        DisputeArbitrationResult ai = AiCallExecutor.callWithTimeout(
                () -> disputeArbitrationAi.arbitrate(
                        nullToText(disputeRef.getDisputeType()),
                        nullToText(disputeRef.getDisputeContent()),
                        orderRef.getOrderId() == null ? NOT_PROVIDED : orderRef.getOrderId().toString(),
                        formatMoney(orderRef.getTradePrice()),
                        nullToText(orderRef.getOrderStatus()),
                        goodsInfo
                ),
                AI_CALL_TIMEOUT_SECONDS,
                "AI仲裁");

        log.info("AI争议仲裁返回, disputeId={}, arbitrateLevel={}, suggestion={}",
                disputeId, ai == null ? null : ai.getArbitrateLevel(),
                ai == null ? null : ai.getSuggestion());

        TrAiAuditRecord record = mapToRecord(disputeId, inputDigest, ai);
        Date now = DateUtils.getNowDate();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setDelFlag("0");

        // 落库委托给独立 Bean，确保 @Transactional 走 Spring 代理生效
        disputePersistService.persistArbitrationSuccess(record, disputeId, record.getAiResult());
        return record;
    }

    @Async("threadPoolTaskExecutor")
    @Override
    public void arbitrateDisputeAsync(Long disputeId)
    {
        try
        {
            arbitrateDispute(disputeId);
        }
        catch (Exception e)
        {
            log.error("AI争议仲裁自动分析失败, disputeId={}", disputeId, e);
            // 失败兜底：状态保持 '0'，仅把错误信息写入 ai_analysis，方便管理员识别并通过
            // /trade/ai/audit/dispute 接口手动重试。仅当 handle_status 仍为 '0' 时才生效。
            try
            {
                String raw = e.getMessage() == null ? "未知错误" : e.getMessage();
                String msg = AI_FAILURE_PREFIX + StringUtils.substring(raw, 0, AI_FAILURE_MSG_MAX);
                trTradeDisputeMapper.updateAiAnalysisIfPending(disputeId, msg, null);
            }
            catch (Exception ex)
            {
                log.error("回写AI失败状态异常, disputeId={}", disputeId, ex);
            }
        }
    }

    /** AI 结构化结果 → 入库实体，字段缺失走兜底默认值 */
    private TrAiAuditRecord mapToRecord(Long disputeId, String inputDigest, DisputeArbitrationResult ai)
    {
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setBusinessId(disputeId);
        record.setBusinessType("dispute");
        record.setInputContent(inputDigest);

        if (ai == null)
        {
            record.setAiResult(null);
            record.setRiskLevel(DEFAULT_ARBITRATE_LEVEL);
            record.setSuggestion(DEFAULT_SUGGESTION);
            record.setRiskReason("AI返回为空，需人工介入");
            return record;
        }

        record.setAiResult(serializeAiResult(ai));
        // arbitrateLevel → riskLevel；reason → riskReason（沿用 TrAiAuditRecord 通用字段语义）
        record.setRiskLevel(isBlank(ai.getArbitrateLevel()) ? DEFAULT_ARBITRATE_LEVEL : ai.getArbitrateLevel());
        record.setSuggestion(isBlank(ai.getSuggestion())    ? DEFAULT_SUGGESTION      : ai.getSuggestion());
        record.setRiskReason(isBlank(ai.getReason())        ? DEFAULT_REASON          : ai.getReason());
        return record;
    }

    /** 业务字段快照写入 ai_audit_record.input_content，供管理员回溯 AI 看到的内容 */
    private String buildInputDigest(TrTradeDispute dispute, TrTradeOrder order, TrTradeGoods goods)
    {
        StringBuilder sb = new StringBuilder(384);
        sb.append("disputeType=").append(nullToText(dispute.getDisputeType())).append('\n');
        sb.append("disputeContent=").append(nullToText(dispute.getDisputeContent())).append('\n');
        sb.append("orderId=").append(order.getOrderId()).append('\n');
        sb.append("tradePrice=").append(formatMoney(order.getTradePrice())).append('\n');
        sb.append("orderStatus=").append(nullToText(order.getOrderStatus())).append('\n');
        sb.append("goodsInfo=").append(buildGoodsInfoBlock(goods));
        return sb.toString();
    }

    /** 拼装 prompt 中 {{goodsInfo}} 占位符的多行文本（goods 可能为 null） */
    private String buildGoodsInfoBlock(TrTradeGoods goods)
    {
        if (goods == null)
        {
            return "无关联商品信息";
        }
        StringBuilder sb = new StringBuilder(192);
        sb.append("商品标题：").append(nullToText(goods.getTitle())).append('\n');
        sb.append("商品价格：").append(formatMoney(goods.getPrice())).append('\n');
        if (goods.getOriginalPrice() != null)
        {
            sb.append("原价：").append(formatMoney(goods.getOriginalPrice())).append('\n');
        }
        sb.append("新旧程度：").append(nullToText(goods.getQuality()));
        if (goods.getDescription() != null)
        {
            sb.append('\n').append("商品描述：").append(goods.getDescription());
        }
        if (goods.getTradePlace() != null)
        {
            sb.append('\n').append("交易地点：").append(goods.getTradePlace());
        }
        return sb.toString();
    }

    /** POJO → 简单 JSON 字符串，写入 ai_result 字段供调试 */
    private String serializeAiResult(DisputeArbitrationResult ai)
    {
        return "{\"arbitrateLevel\":\"" + safe(ai.getArbitrateLevel())
                + "\",\"suggestion\":\"" + safe(ai.getSuggestion())
                + "\",\"reason\":\"" + safe(ai.getReason()) + "\"}";
    }

    private static String nullToText(String v)
    {
        return v == null || v.isEmpty() ? NOT_PROVIDED : v;
    }

    private static String formatMoney(BigDecimal v)
    {
        return v == null ? NOT_PROVIDED : v.toPlainString() + " 元";
    }

    private static boolean isBlank(String v)
    {
        return v == null || v.trim().isEmpty();
    }

    private static String safe(String v)
    {
        return v == null ? "" : v.replace("\"", "\\\"");
    }
}
