package com.ruoyi.trade.domain.dto;

/**
 * 移动端退款入参（买家申请退款 / 卖家主动退款）。
 *
 * <p>仅接收退款原因；订单、参与方、金额等由服务端依据订单和登录信息推导。</p>
 */
public class AppOrderRefundDto
{
    /** 退款原因。 */
    private String reason;

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }
}
