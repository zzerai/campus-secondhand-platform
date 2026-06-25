package com.ruoyi.trade.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.trade.mapper.TrTradeMessageMapper;
import com.ruoyi.trade.domain.TrTradeMessage;
import com.ruoyi.trade.service.ITrTradeMessageService;

/**
 * 私信消息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
@Service
public class TrTradeMessageServiceImpl implements ITrTradeMessageService 
{
    @Autowired
    private TrTradeMessageMapper trTradeMessageMapper;

    /**
     * 查询私信消息
     * 
     * @param messageId 私信消息主键
     * @return 私信消息
     */
    @Override
    public TrTradeMessage selectTrTradeMessageByMessageId(Long messageId)
    {
        return trTradeMessageMapper.selectTrTradeMessageByMessageId(messageId);
    }

    /**
     * 查询私信消息列表
     * 
     * @param trTradeMessage 私信消息
     * @return 私信消息
     */
    @Override
    public List<TrTradeMessage> selectTrTradeMessageList(TrTradeMessage trTradeMessage)
    {
        return trTradeMessageMapper.selectTrTradeMessageList(trTradeMessage);
    }

    /**
     * 新增私信消息
     * 
     * @param trTradeMessage 私信消息
     * @return 结果
     */
    @Override
    public int insertTrTradeMessage(TrTradeMessage trTradeMessage)
    {
        String username = SecurityUtils.getUsername();
        trTradeMessage.setCreateBy(username);
        trTradeMessage.setCreateTime(DateUtils.getNowDate());
        trTradeMessage.setUpdateBy(username);
        trTradeMessage.setUpdateTime(DateUtils.getNowDate());
        return trTradeMessageMapper.insertTrTradeMessage(trTradeMessage);
    }

    /**
     * 修改私信消息
     * 
     * @param trTradeMessage 私信消息
     * @return 结果
     */
    @Override
    public int updateTrTradeMessage(TrTradeMessage trTradeMessage)
    {
        trTradeMessage.setUpdateBy(SecurityUtils.getUsername());
        trTradeMessage.setUpdateTime(DateUtils.getNowDate());
        return trTradeMessageMapper.updateTrTradeMessage(trTradeMessage);
    }

    /**
     * 批量删除私信消息
     * 
     * @param messageIds 需要删除的私信消息主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeMessageByMessageIds(Long[] messageIds)
    {
        return trTradeMessageMapper.deleteTrTradeMessageByMessageIds(messageIds);
    }

    /**
     * 删除私信消息信息
     * 
     * @param messageId 私信消息主键
     * @return 结果
     */
    @Override
    public int deleteTrTradeMessageByMessageId(Long messageId)
    {
        return trTradeMessageMapper.deleteTrTradeMessageByMessageId(messageId);
    }
}
