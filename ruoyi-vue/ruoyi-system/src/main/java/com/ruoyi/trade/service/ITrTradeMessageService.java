package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrTradeMessage;

/**
 * 私信消息Service接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface ITrTradeMessageService 
{
    /**
     * 查询私信消息
     * 
     * @param messageId 私信消息主键
     * @return 私信消息
     */
    public TrTradeMessage selectTrTradeMessageByMessageId(Long messageId);

    /**
     * 查询私信消息列表
     * 
     * @param trTradeMessage 私信消息
     * @return 私信消息集合
     */
    public List<TrTradeMessage> selectTrTradeMessageList(TrTradeMessage trTradeMessage);

    /**
     * 新增私信消息
     * 
     * @param trTradeMessage 私信消息
     * @return 结果
     */
    public int insertTrTradeMessage(TrTradeMessage trTradeMessage);

    /**
     * 修改私信消息
     * 
     * @param trTradeMessage 私信消息
     * @return 结果
     */
    public int updateTrTradeMessage(TrTradeMessage trTradeMessage);

    /**
     * 批量删除私信消息
     * 
     * @param messageIds 需要删除的私信消息主键集合
     * @return 结果
     */
    public int deleteTrTradeMessageByMessageIds(Long[] messageIds);

    /**
     * 删除私信消息信息
     * 
     * @param messageId 私信消息主键
     * @return 结果
     */
    public int deleteTrTradeMessageByMessageId(Long messageId);
}
