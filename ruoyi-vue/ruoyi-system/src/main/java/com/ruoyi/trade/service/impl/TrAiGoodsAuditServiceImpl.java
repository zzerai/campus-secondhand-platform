package com.ruoyi.trade.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.trade.domain.TrAiAuditRecord;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.vo.ai.GoodsAuditResult;
import com.ruoyi.trade.mapper.TrAiAuditRecordMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.service.ITrAiGoodsAuditService;
import com.ruoyi.trade.service.ai.GoodsAuditAi;
import com.ruoyi.trade.utils.AiCallExecutor;

/**
 * AI 商品审核 Service 业务层处理。
 *
 * <p>提示词与 AI 调用细节由 {@link GoodsAuditAi}（langchain4j AiServices 代理）承担；
 * 本类只负责：商品存在性校验 → 占位符值兜底 → AI 调用（带超时）→ POJO → Entity 映射 → 落库。</p>
 *
 * @author thr
 * @date 2026-05-20
 */
@Service
public class TrAiGoodsAuditServiceImpl implements ITrAiGoodsAuditService
{
    private static final Logger log = LoggerFactory.getLogger(TrAiGoodsAuditServiceImpl.class);

    /** AI 单次调用超时（秒）。DashScope starter 未暴露 timeout，本层强制兜底。 */
    private static final long AI_CALL_TIMEOUT_SECONDS = 30L;

    /** AI 字段缺省兜底：未明确风险等级时按 middle 处理，引导走人工 */
    private static final String DEFAULT_RISK_LEVEL = "middle";
    private static final String DEFAULT_SUGGESTION = "人工复核";
    private static final String DEFAULT_REASON     = "AI未提供明确风险原因";
    /** prompt 占位符必须非 null，业务字段缺失时塞这个 */
    private static final String NOT_PROVIDED = "未提供";

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrAiAuditRecordMapper trAiAuditRecordMapper;

    @Autowired
    private GoodsAuditAi goodsAuditAi;

    @Override
    public TrAiAuditRecord auditGoods(Long goodsId)
    {
        TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(goodsId);
        if (goods == null)
        {
            throw new ServiceException("商品不存在");
        }

        String inputDigest = buildInputDigest(goods);
        log.info("AI商品审核开始, goodsId={}, title={}", goodsId, goods.getTitle());

        GoodsAuditResult ai = AiCallExecutor.callWithTimeout(
                () -> goodsAuditAi.audit(
                        nullToText(goods.getTitle()),
                        nullToText(goods.getCategoryName()),
                        formatMoney(goods.getPrice()),
                        formatMoney(goods.getOriginalPrice()),
                        nullToText(goods.getQuality()),
                        nullToText(goods.getDescription()),
                        nullToText(goods.getTradePlace())
                ),
                AI_CALL_TIMEOUT_SECONDS,
                "AI审核");

        log.info("AI商品审核返回, goodsId={}, riskLevel={}, suggestion={}",
                goodsId, ai == null ? null : ai.getRiskLevel(), ai == null ? null : ai.getSuggestion());

        TrAiAuditRecord record = mapToRecord(goodsId, inputDigest, ai);
        Date now = DateUtils.getNowDate();
        record.setCreateTime(now);
        record.setUpdateTime(now);
        record.setDelFlag("0");

        trAiAuditRecordMapper.insertTrAiAuditRecord(record);
        return record;
    }

    /**
     * 把 AI 结构化返回映射到入库实体；任一字段缺失/为空时回填默认兜底，
     * 避免审核记录里出现 null 字段难以渲染。
     */
    private TrAiAuditRecord mapToRecord(Long goodsId, String inputDigest, GoodsAuditResult ai)
    {
        TrAiAuditRecord record = new TrAiAuditRecord();
        record.setBusinessId(goodsId);
        record.setBusinessType("goods");
        record.setInputContent(inputDigest);

        if (ai == null)
        {
            record.setAiResult(null);
            record.setRiskLevel(DEFAULT_RISK_LEVEL);
            record.setSuggestion(DEFAULT_SUGGESTION);
            record.setRiskReason("AI返回为空，需人工复核");
            return record;
        }

        record.setAiResult(serializeAiResult(ai));
        record.setRiskLevel(isBlank(ai.getRiskLevel())   ? DEFAULT_RISK_LEVEL : ai.getRiskLevel());
        record.setSuggestion(isBlank(ai.getSuggestion()) ? DEFAULT_SUGGESTION : ai.getSuggestion());
        record.setRiskReason(isBlank(ai.getRiskReason()) ? DEFAULT_REASON     : ai.getRiskReason());
        return record;
    }

    /** 业务字段简要快照写入 ai_audit_record.input_content，供管理员回溯 AI 看到的内容 */
    private String buildInputDigest(TrTradeGoods goods)
    {
        StringBuilder sb = new StringBuilder(256);
        sb.append("title=").append(nullToText(goods.getTitle())).append('\n');
        sb.append("category=").append(nullToText(goods.getCategoryName())).append('\n');
        sb.append("price=").append(formatMoney(goods.getPrice())).append('\n');
        sb.append("originalPrice=").append(formatMoney(goods.getOriginalPrice())).append('\n');
        sb.append("quality=").append(nullToText(goods.getQuality())).append('\n');
        sb.append("description=").append(nullToText(goods.getDescription())).append('\n');
        sb.append("tradePlace=").append(nullToText(goods.getTradePlace()));
        return sb.toString();
    }

    /** POJO → 简单 JSON 字符串，写入 ai_result 字段供调试 */
    private String serializeAiResult(GoodsAuditResult ai)
    {
        return "{\"riskLevel\":\"" + safe(ai.getRiskLevel())
                + "\",\"suggestion\":\"" + safe(ai.getSuggestion())
                + "\",\"riskReason\":\"" + safe(ai.getRiskReason()) + "\"}";
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

    /** 避免落库 JSON 含双引号导致破坏；POJO 字段值里出现 " 时简单转义 */
    private static String safe(String v)
    {
        return v == null ? "" : v.replace("\"", "\\\"");
    }
}
