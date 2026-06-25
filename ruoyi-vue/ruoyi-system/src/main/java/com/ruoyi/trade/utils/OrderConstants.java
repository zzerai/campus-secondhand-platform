package com.ruoyi.trade.utils;

/**
 * 订单交易模块常量（闲鱼模式）。
 *
 * <p>订单状态 / 退款状态 / 支付状态取值与 {@code tr_trade_order} 的
 * order_status / refund_status / payment_status 列对应。设计规格见
 * {@code docs/订单流程改造方案-闲鱼模式.md}。</p>
 *
 * @author thr
 */
public class OrderConstants
{
    private OrderConstants() {}

    /* ---------- order_status 订单状态 ---------- */
    /** 待支付（下单即此状态，无需卖家确认）。 */
    public static final String ORDER_PENDING_PAY = "0";
    /** 待收货/待交割（支付成功后）。 */
    public static final String ORDER_AWAITING_RECEIPT = "2";
    /** 已完成（买家确认或超时自动确认）。 */
    public static final String ORDER_FINISHED = "3";
    /** 已取消（待支付阶段取消/超时）。 */
    public static final String ORDER_CANCELED = "4";
    /** 争议中。 */
    public static final String ORDER_DISPUTING = "5";
    /** 退款中。 */
    public static final String ORDER_REFUNDING = "6";
    /** 已退款（终态）。 */
    public static final String ORDER_REFUNDED = "7";

    /* ---------- refund_status 退款子状态（仅 order_status=6 有意义） ---------- */
    /** 无退款。 */
    public static final String REFUND_NONE = "0";
    /** 买家申请中，待卖家处理。 */
    public static final String REFUND_APPLYING = "1";
    /** 卖家已同意，支付宝退款执行中。 */
    public static final String REFUND_AGREED = "2";
    /** 退款成功。 */
    public static final String REFUND_DONE = "3";
    /** 卖家拒绝。 */
    public static final String REFUND_REJECTED = "4";

    /* ---------- payment_status 支付状态 ---------- */
    /** 未支付。 */
    public static final String PAYMENT_UNPAID = "0";
    /** 支付中。 */
    public static final String PAYMENT_PAYING = "1";
    /** 支付成功。 */
    public static final String PAYMENT_PAID = "2";
    /** 支付失败。 */
    public static final String PAYMENT_FAILED = "3";
    /** 已取消/已退款。 */
    public static final String PAYMENT_CANCELED = "4";

    /* ---------- operator_type 订单日志操作人类型 ---------- */
    /** 买家。 */
    public static final String OPERATOR_BUYER = "1";
    /** 卖家。 */
    public static final String OPERATOR_SELLER = "2";
    /** 管理员/系统。 */
    public static final String OPERATOR_ADMIN = "3";

    /* ---------- trade_method 交易方式 ---------- */
    /** 线下交易。 */
    public static final String TRADE_METHOD_OFFLINE = "offline";

    /* ---------- 超时阈值配置项 key（application.yml / sys_config，缺失用下方默认值） ---------- */
    /** 待支付超时自动取消（分钟）。 */
    public static final int DEFAULT_PAY_TIMEOUT_MINUTES = 30;
    /** 待收货超时自动确认收货（天）。 */
    public static final int DEFAULT_AUTO_CONFIRM_DAYS = 7;
}
