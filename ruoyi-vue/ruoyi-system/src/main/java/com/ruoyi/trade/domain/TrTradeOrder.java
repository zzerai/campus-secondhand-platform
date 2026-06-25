package com.ruoyi.trade.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Trade order entity.
 *
 * @author lyl
 * @date 2026-05-12
 */
public class TrTradeOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** Order id. */
    private Long orderId;

    /** Order no. */
    @Excel(name = "订单编号")
    private String orderNo;

    /** Goods id. */
    @Excel(name = "商品ID")
    private Long goodsId;

    /** Buyer student user id. */
    @Excel(name = "买家学生用户ID")
    private Long buyerId;

    /** Seller student user id. */
    @Excel(name = "卖家学生用户ID")
    private Long sellerId;

    /** 下单时商品标题快照。 */
    @Excel(name = "商品标题快照")
    private String goodsTitle;

    /** 下单时商品图片快照（多图逗号分隔 URL）。 */
    @Excel(name = "商品图片快照")
    private String goodsImages;

    /** 交易方式：offline 线下交易。 */
    @Excel(name = "交易方式")
    private String tradeMethod;

    /** Trade price. */
    @Excel(name = "交易价格")
    private BigDecimal tradePrice;

    /** Trade place. */
    @Excel(name = "约定交易地点")
    private String tradePlace;

    /** Appointment time. */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @JSONField(format = "yyyy-MM-dd HH:mm")
    @Excel(name = "预约交易时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm")
    private Date appointmentTime;

    /** Order status: 0 pending, 1 confirmed/awaiting payment, 2 paid/awaiting handover, 3 completed, 4 cancelled, 5 dispute. */
    @Excel(name = "订单状态")
    private String orderStatus;

    /** Payment status: 0 unpaid, 1 paying, 2 paid, 3 failed, 4 refunded/cancelled. */
    @Excel(name = "支付状态")
    private String paymentStatus;

    /** Refund status: 0 none, 1 buyer applied/awaiting seller, 2 agreed/refunding, 3 refunded, 4 seller rejected. */
    @Excel(name = "退款状态")
    private String refundStatus;

    /** Refund amount. */
    @Excel(name = "退款金额")
    private BigDecimal refundAmount;

    /** Refund reason. */
    @Excel(name = "退款原因")
    private String refundReason;

    /** Refund apply time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退款申请时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date refundApplyTime;

    /** Refund success time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "退款成功时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date refundTime;

    /** Alipay refund no. */
    @Excel(name = "支付宝退款流水号")
    private String alipayRefundNo;

    /** Buyer remark. */
    @Excel(name = "买家备注")
    private String buyerRemark;

    /** Seller remark. */
    @Excel(name = "卖家备注")
    private String sellerRemark;

    /** Cancel reason. */
    @Excel(name = "取消原因")
    private String cancelReason;

    /** Seller confirm time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "卖家确认时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date confirmTime;

    /** Pay time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "支付时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date payTime;

    /** Alipay trade no. */
    @Excel(name = "支付宝交易流水号")
    private String alipayTradeNo;

    /** Actual payment amount. */
    @Excel(name = "实际支付金额")
    private BigDecimal paymentAmount;

    /** Payment success time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "支付成功时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date paymentTime;

    /** Buyer confirm completion time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "买家确认完成时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date completeTime;

    /** Cancel time. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "取消时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date cancelTime;

    /** Delete flag. */
    private String delFlag;

    /** Transient: seller nickname (joined from tr_student_user). */
    private String sellerNickname;

    /** Transient: buyer nickname (joined from tr_student_user). */
    private String buyerNickname;

    /** Transient: seller avatar (joined from tr_student_user). */
    private String sellerAvatar;

    /** Transient: buyer avatar (joined from tr_student_user). */
    private String buyerAvatar;

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public Long getBuyerId()
    {
        return buyerId;
    }

    public void setBuyerId(Long buyerId)
    {
        this.buyerId = buyerId;
    }

    public Long getSellerId()
    {
        return sellerId;
    }

    public void setSellerId(Long sellerId)
    {
        this.sellerId = sellerId;
    }

    public String getGoodsTitle()
    {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle)
    {
        this.goodsTitle = goodsTitle;
    }

    public String getGoodsImages()
    {
        return goodsImages;
    }

    public void setGoodsImages(String goodsImages)
    {
        this.goodsImages = goodsImages;
    }

    public String getTradeMethod()
    {
        return tradeMethod;
    }

    public void setTradeMethod(String tradeMethod)
    {
        this.tradeMethod = tradeMethod;
    }

    public BigDecimal getTradePrice()
    {
        return tradePrice;
    }

    public void setTradePrice(BigDecimal tradePrice)
    {
        this.tradePrice = tradePrice;
    }

    public String getTradePlace()
    {
        return tradePlace;
    }

    public void setTradePlace(String tradePlace)
    {
        this.tradePlace = tradePlace;
    }

    public Date getAppointmentTime()
    {
        return appointmentTime;
    }

    public void setAppointmentTime(Date appointmentTime)
    {
        this.appointmentTime = appointmentTime;
    }

    public String getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {
        this.orderStatus = orderStatus;
    }

    public String getPaymentStatus()
    {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus)
    {
        this.paymentStatus = paymentStatus;
    }

    public String getRefundStatus()
    {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus)
    {
        this.refundStatus = refundStatus;
    }

    public BigDecimal getRefundAmount()
    {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount)
    {
        this.refundAmount = refundAmount;
    }

    public String getRefundReason()
    {
        return refundReason;
    }

    public void setRefundReason(String refundReason)
    {
        this.refundReason = refundReason;
    }

    public Date getRefundApplyTime()
    {
        return refundApplyTime;
    }

    public void setRefundApplyTime(Date refundApplyTime)
    {
        this.refundApplyTime = refundApplyTime;
    }

    public Date getRefundTime()
    {
        return refundTime;
    }

    public void setRefundTime(Date refundTime)
    {
        this.refundTime = refundTime;
    }

    public String getAlipayRefundNo()
    {
        return alipayRefundNo;
    }

    public void setAlipayRefundNo(String alipayRefundNo)
    {
        this.alipayRefundNo = alipayRefundNo;
    }

    public String getBuyerRemark()
    {
        return buyerRemark;
    }

    public void setBuyerRemark(String buyerRemark)
    {
        this.buyerRemark = buyerRemark;
    }

    public String getSellerRemark()
    {
        return sellerRemark;
    }

    public void setSellerRemark(String sellerRemark)
    {
        this.sellerRemark = sellerRemark;
    }

    public String getCancelReason()
    {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason)
    {
        this.cancelReason = cancelReason;
    }

    public Date getConfirmTime()
    {
        return confirmTime;
    }

    public void setConfirmTime(Date confirmTime)
    {
        this.confirmTime = confirmTime;
    }

    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }

    public String getAlipayTradeNo()
    {
        return alipayTradeNo;
    }

    public void setAlipayTradeNo(String alipayTradeNo)
    {
        this.alipayTradeNo = alipayTradeNo;
    }

    public BigDecimal getPaymentAmount()
    {
        return paymentAmount;
    }

    public void setPaymentAmount(BigDecimal paymentAmount)
    {
        this.paymentAmount = paymentAmount;
    }

    public Date getPaymentTime()
    {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime)
    {
        this.paymentTime = paymentTime;
    }

    public Date getCompleteTime()
    {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime)
    {
        this.completeTime = completeTime;
    }

    public Date getCancelTime()
    {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime)
    {
        this.cancelTime = cancelTime;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getSellerNickname() { return sellerNickname; }
    public void setSellerNickname(String sellerNickname) { this.sellerNickname = sellerNickname; }
    public String getBuyerNickname() { return buyerNickname; }
    public void setBuyerNickname(String buyerNickname) { this.buyerNickname = buyerNickname; }
    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }
    public String getBuyerAvatar() { return buyerAvatar; }
    public void setBuyerAvatar(String buyerAvatar) { this.buyerAvatar = buyerAvatar; }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("goodsId", getGoodsId())
            .append("buyerId", getBuyerId())
            .append("sellerId", getSellerId())
            .append("goodsTitle", getGoodsTitle())
            .append("goodsImages", getGoodsImages())
            .append("tradeMethod", getTradeMethod())
            .append("tradePrice", getTradePrice())
            .append("tradePlace", getTradePlace())
            .append("appointmentTime", getAppointmentTime())
            .append("orderStatus", getOrderStatus())
            .append("paymentStatus", getPaymentStatus())
            .append("refundStatus", getRefundStatus())
            .append("refundAmount", getRefundAmount())
            .append("refundReason", getRefundReason())
            .append("refundApplyTime", getRefundApplyTime())
            .append("refundTime", getRefundTime())
            .append("alipayRefundNo", getAlipayRefundNo())
            .append("buyerRemark", getBuyerRemark())
            .append("sellerRemark", getSellerRemark())
            .append("cancelReason", getCancelReason())
            .append("confirmTime", getConfirmTime())
            .append("payTime", getPayTime())
            .append("alipayTradeNo", getAlipayTradeNo())
            .append("paymentAmount", getPaymentAmount())
            .append("paymentTime", getPaymentTime())
            .append("completeTime", getCompleteTime())
            .append("cancelTime", getCancelTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
