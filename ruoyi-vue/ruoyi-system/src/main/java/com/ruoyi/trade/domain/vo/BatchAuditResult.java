package com.ruoyi.trade.domain.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品批量审核结果：返回成功数 + 失败明细，避免部分失败被前端误判为全部成功。
 *
 * @author thr
 */
public class BatchAuditResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 成功条数 */
    private int success;

    /** 失败条数 */
    private int failure;

    /** 失败明细：商品ID + 失败原因 */
    private List<Item> errors = new ArrayList<>();

    public int getSuccess()
    {
        return success;
    }

    public void setSuccess(int success)
    {
        this.success = success;
    }

    public int getFailure()
    {
        return failure;
    }

    public void setFailure(int failure)
    {
        this.failure = failure;
    }

    public List<Item> getErrors()
    {
        return errors;
    }

    public void setErrors(List<Item> errors)
    {
        this.errors = errors;
    }

    public void addError(Long goodsId, String reason)
    {
        this.errors.add(new Item(goodsId, reason));
        this.failure++;
    }

    public void incSuccess()
    {
        this.success++;
    }

    public static class Item implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Long goodsId;
        private String reason;

        public Item()
        {
        }

        public Item(Long goodsId, String reason)
        {
            this.goodsId = goodsId;
            this.reason = reason;
        }

        public Long getGoodsId()
        {
            return goodsId;
        }

        public void setGoodsId(Long goodsId)
        {
            this.goodsId = goodsId;
        }

        public String getReason()
        {
            return reason;
        }

        public void setReason(String reason)
        {
            this.reason = reason;
        }
    }
}
