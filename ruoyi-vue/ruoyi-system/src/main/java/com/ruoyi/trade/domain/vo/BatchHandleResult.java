package com.ruoyi.trade.domain.vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用批量处理结果：返回成功数 + 失败明细，避免部分失败被前端误判为全部成功。
 *
 * <p>结构与 {@link BatchAuditResult} 同形，但语义聚焦"举报处理"等非审核类批量操作，
 * 字段名 {@code reportId / orderId} 由调用方按业务装载。</p>
 *
 * @author thr
 */
public class BatchHandleResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 成功条数 */
    private int success;

    /** 失败条数 */
    private int failure;

    /** 失败明细 */
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

    public void incSuccess()
    {
        this.success++;
    }

    public void addError(Long id, String reason)
    {
        this.errors.add(new Item(id, reason));
        this.failure++;
    }

    public static class Item implements Serializable
    {
        private static final long serialVersionUID = 1L;

        /** 业务ID（举报ID / 订单ID 等，由调用方语境决定） */
        private Long id;

        private String reason;

        public Item()
        {
        }

        public Item(Long id, String reason)
        {
            this.id = id;
            this.reason = reason;
        }

        public Long getId()
        {
            return id;
        }

        public void setId(Long id)
        {
            this.id = id;
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
