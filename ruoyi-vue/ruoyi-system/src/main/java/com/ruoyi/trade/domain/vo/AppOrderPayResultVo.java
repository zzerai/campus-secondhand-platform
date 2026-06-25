package com.ruoyi.trade.domain.vo;

import java.math.BigDecimal;

/**
 * Mobile order payment result.
 */
public class AppOrderPayResultVo
{
    private Long orderId;

    private String orderNo;

    private String orderStatus;

    private String paymentStatus;

    private String alipayTradeNo;

    private BigDecimal paymentAmount;

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
}
