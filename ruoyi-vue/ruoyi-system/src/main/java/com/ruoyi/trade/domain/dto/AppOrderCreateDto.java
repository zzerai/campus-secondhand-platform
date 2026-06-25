package com.ruoyi.trade.domain.dto;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 移动端订单创建入参。
 *
 * <p>仅接收用户可填写的字段，buyer/seller、订单号、价格、状态、支付/取消/完成时间戳等
 * 一律由服务端依据商品和登录信息推导，避免客户端注入 paymentStatus / paymentAmount /
 * alipayTradeNo / delFlag / completeTime 等敏感字段。</p>
 */
public class AppOrderCreateDto
{
    /** 商品 ID（必填）。 */
    private Long goodsId;

    /** 交易方式：当前只支持 offline，未传时服务端默认 offline。 */
    private String tradeMethod;

    /** 约定交易地点（不传则沿用商品发布时的 tradePlace）。 */
    private String tradePlace;

    /** 预约交易时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date appointmentTime;

    /** 买家备注。 */
    private String buyerRemark;

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public String getTradeMethod()
    {
        return tradeMethod;
    }

    public void setTradeMethod(String tradeMethod)
    {
        this.tradeMethod = tradeMethod;
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

    public String getBuyerRemark()
    {
        return buyerRemark;
    }

    public void setBuyerRemark(String buyerRemark)
    {
        this.buyerRemark = buyerRemark;
    }
}
