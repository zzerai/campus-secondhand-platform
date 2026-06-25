package com.ruoyi.trade.domain.dto;

/**
 * 移动端发送私信入参。
 *
 * <p>仅接收用户可填写的字段：对方ID / 关联商品ID / 可选关联订单ID / 消息内容。
 * sender 由服务端从登录态推导，禁止客户端注入；read_status / del_flag 等系统字段
 * 也不接收，避免越权修改。</p>
 */
public class AppMessageSendDto
{
    /** 接收人学生用户ID（必填）。 */
    private Long receiverId;

    /** 关联商品ID（必填）。按商品维度划分会话，每个商品对应一个独立会话。 */
    private Long goodsId;

    /** 关联订单ID（可选）。订单咨询场景填写，普通商品咨询不填。 */
    private Long orderId;

    /** 消息内容（必填，1-1000 字符）。 */
    private String content;

    public Long getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(Long receiverId)
    {
        this.receiverId = receiverId;
    }

    public Long getGoodsId()
    {
        return goodsId;
    }

    public void setGoodsId(Long goodsId)
    {
        this.goodsId = goodsId;
    }

    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
