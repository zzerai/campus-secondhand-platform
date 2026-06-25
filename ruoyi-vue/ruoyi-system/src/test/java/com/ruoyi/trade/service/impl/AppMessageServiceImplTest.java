package com.ruoyi.trade.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeMessage;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppMessageSendDto;
import com.ruoyi.trade.domain.vo.AppMessageVo;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeMessageMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;

/**
 * 移动端私信咨询业务单测。
 *
 * @author thr
 */
@ExtendWith(MockitoExtension.class)
class AppMessageServiceImplTest
{
    private static final Long ME_ID = 1L;
    private static final String ME_USERNAME = "20210001";
    private static final Long PEER_ID = 2L;
    private static final Long GOODS_ID = 100L;

    @Mock private TrTradeMessageMapper trTradeMessageMapper;
    @Mock private TrStudentUserMapper trStudentUserMapper;
    @Mock private TrTradeGoodsMapper trTradeGoodsMapper;
    @Mock private TrTradeOrderMapper trTradeOrderMapper;

    @InjectMocks
    private AppMessageServiceImpl service;

    // ---------- sendMessage ----------

    @Test
    void sendShouldRejectMissingLogin()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(null, ME_USERNAME, validDto()));
    }

    @Test
    void sendShouldRejectNullDto()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, null));
    }

    @Test
    void sendShouldRejectMissingReceiver()
    {
        AppMessageSendDto dto = validDto();
        dto.setReceiverId(null);
        Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, dto));
    }

    @Test
    void sendShouldRejectMissingGoods()
    {
        AppMessageSendDto dto = validDto();
        dto.setGoodsId(null);
        Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, dto));
    }

    @Test
    void sendShouldRejectSelfTalk()
    {
        AppMessageSendDto dto = validDto();
        dto.setReceiverId(ME_ID);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, dto));
        Assertions.assertTrue(ex.getMessage().contains("不能给自己"));
    }

    @Test
    void sendShouldRejectBlankContent()
    {
        AppMessageSendDto dto = validDto();
        dto.setContent("   ");
        Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, dto));
    }

    @Test
    void sendShouldRejectOversizeContent()
    {
        AppMessageSendDto dto = validDto();
        char[] arr = new char[1001];
        Arrays.fill(arr, 'x');
        dto.setContent(new String(arr));
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, dto));
        Assertions.assertTrue(ex.getMessage().contains("1000"));
    }

    @Test
    void sendShouldRejectWhenReceiverMissing()
    {
        when(trStudentUserMapper.selectTrStudentUserByUserId(PEER_ID)).thenReturn(null);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, validDto()));
        Assertions.assertTrue(ex.getMessage().contains("接收人不存在"));
        verify(trTradeMessageMapper, never()).insertTrTradeMessage(any());
    }

    @Test
    void sendShouldRejectWhenGoodsMissing()
    {
        when(trStudentUserMapper.selectTrStudentUserByUserId(PEER_ID)).thenReturn(new TrStudentUser());
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(GOODS_ID)).thenReturn(null);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, validDto()));
        Assertions.assertTrue(ex.getMessage().contains("商品不存在"));
        verify(trTradeMessageMapper, never()).insertTrTradeMessage(any());
    }

    @Test
    void sendShouldRejectWhenOrderMissing()
    {
        AppMessageSendDto dto = validDto();
        dto.setOrderId(999L);
        when(trStudentUserMapper.selectTrStudentUserByUserId(PEER_ID)).thenReturn(new TrStudentUser());
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(GOODS_ID)).thenReturn(new TrTradeGoods());
        when(trTradeOrderMapper.selectTrTradeOrderByOrderId(999L)).thenReturn(null);
        ServiceException ex = Assertions.assertThrows(ServiceException.class,
                () -> service.sendMessage(ME_ID, ME_USERNAME, dto));
        Assertions.assertTrue(ex.getMessage().contains("订单不存在"));
    }

    @Test
    void sendShouldPersistWithSenderFromLogin()
    {
        when(trStudentUserMapper.selectTrStudentUserByUserId(PEER_ID)).thenReturn(new TrStudentUser());
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(GOODS_ID)).thenReturn(new TrTradeGoods());
        when(trTradeMessageMapper.insertTrTradeMessage(any(TrTradeMessage.class))).thenAnswer(inv -> {
            TrTradeMessage m = inv.getArgument(0);
            m.setMessageId(555L);
            return 1;
        });

        Long id = service.sendMessage(ME_ID, ME_USERNAME, validDto());

        Assertions.assertEquals(555L, id);
        ArgumentCaptor<TrTradeMessage> captor = ArgumentCaptor.forClass(TrTradeMessage.class);
        verify(trTradeMessageMapper).insertTrTradeMessage(captor.capture());
        TrTradeMessage saved = captor.getValue();
        Assertions.assertEquals(ME_ID, saved.getSenderId(), "sender 必须强制取自登录态");
        Assertions.assertEquals(PEER_ID, saved.getReceiverId());
        Assertions.assertEquals(GOODS_ID, saved.getGoodsId());
        Assertions.assertEquals("0", saved.getReadStatus(), "新消息默认未读");
        Assertions.assertEquals(ME_USERNAME, saved.getCreateBy());
        Assertions.assertNotNull(saved.getCreateTime());
    }

    @Test
    void sendShouldTrimContent()
    {
        AppMessageSendDto dto = validDto();
        dto.setContent("  hello  ");
        when(trStudentUserMapper.selectTrStudentUserByUserId(PEER_ID)).thenReturn(new TrStudentUser());
        when(trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(GOODS_ID)).thenReturn(new TrTradeGoods());

        service.sendMessage(ME_ID, ME_USERNAME, dto);

        ArgumentCaptor<TrTradeMessage> captor = ArgumentCaptor.forClass(TrTradeMessage.class);
        verify(trTradeMessageMapper).insertTrTradeMessage(captor.capture());
        Assertions.assertEquals("hello", captor.getValue().getContent());
    }

    // ---------- listMessages ----------

    @Test
    void listMessagesShouldRejectMissingLogin()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.listMessages(null, PEER_ID, GOODS_ID));
    }

    @Test
    void listMessagesShouldRejectSelfPeer()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.listMessages(ME_ID, ME_ID, GOODS_ID));
    }

    @Test
    void listMessagesShouldRejectMissingGoods()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.listMessages(ME_ID, PEER_ID, null));
    }

    @Test
    void listMessagesShouldBackfillMineFlag()
    {
        AppMessageVo mineMsg = new AppMessageVo();
        mineMsg.setSenderId(ME_ID);
        AppMessageVo peerMsg = new AppMessageVo();
        peerMsg.setSenderId(PEER_ID);
        when(trTradeMessageMapper.selectConversationMessages(ME_ID, PEER_ID, GOODS_ID))
                .thenReturn(Arrays.asList(mineMsg, peerMsg));

        List<AppMessageVo> result = service.listMessages(ME_ID, PEER_ID, GOODS_ID);

        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.get(0).getMine(), "sender == me 应标记为 mine=true");
        Assertions.assertFalse(result.get(1).getMine(), "sender != me 应标记为 mine=false");
    }

    // ---------- markConversationRead ----------

    @Test
    void markReadShouldRejectMissingLogin()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.markConversationRead(null, ME_USERNAME, PEER_ID, GOODS_ID));
    }

    @Test
    void markReadShouldRejectIncompleteKey()
    {
        Assertions.assertThrows(ServiceException.class,
                () -> service.markConversationRead(ME_ID, ME_USERNAME, null, GOODS_ID));
        Assertions.assertThrows(ServiceException.class,
                () -> service.markConversationRead(ME_ID, ME_USERNAME, PEER_ID, null));
    }

    @Test
    void markReadShouldDelegateToMapper()
    {
        when(trTradeMessageMapper.markConversationAsRead(ME_ID, PEER_ID, GOODS_ID, ME_USERNAME))
                .thenReturn(3);
        int marked = service.markConversationRead(ME_ID, ME_USERNAME, PEER_ID, GOODS_ID);
        Assertions.assertEquals(3, marked);
        verify(trTradeMessageMapper, times(1))
                .markConversationAsRead(ME_ID, PEER_ID, GOODS_ID, ME_USERNAME);
    }

    // ---------- countUnread ----------

    @Test
    void countUnreadShouldReturnZeroWhenNotLoggedIn()
    {
        Assertions.assertEquals(0L, service.countUnread(null));
        verify(trTradeMessageMapper, never()).countUnread(anyLong());
    }

    @Test
    void countUnreadShouldDelegateToMapper()
    {
        when(trTradeMessageMapper.countUnread(ME_ID)).thenReturn(7L);
        Assertions.assertEquals(7L, service.countUnread(ME_ID));
    }

    // ---------- listConversations ----------

    @Test
    void listConversationsShouldReturnEmptyWhenNotLoggedIn()
    {
        List<?> list = service.listConversations(null);
        Assertions.assertTrue(list.isEmpty());
        verify(trTradeMessageMapper, never()).selectConversations(anyLong());
    }

    @Test
    void listConversationsShouldDelegateToMapper()
    {
        when(trTradeMessageMapper.selectConversations(ME_ID)).thenReturn(Collections.emptyList());
        Assertions.assertNotNull(service.listConversations(ME_ID));
        verify(trTradeMessageMapper).selectConversations(ME_ID);
    }

    private AppMessageSendDto validDto()
    {
        AppMessageSendDto dto = new AppMessageSendDto();
        dto.setReceiverId(PEER_ID);
        dto.setGoodsId(GOODS_ID);
        dto.setContent("你好，这件商品还在吗？");
        return dto;
    }
}
