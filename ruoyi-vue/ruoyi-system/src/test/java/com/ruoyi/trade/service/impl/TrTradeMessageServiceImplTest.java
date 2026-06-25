package com.ruoyi.trade.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.domain.TrTradeMessage;
import com.ruoyi.trade.mapper.TrTradeMessageMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TrTradeMessageServiceImpl 单元测试
 */
class TrTradeMessageServiceImplTest {

    @Mock
    private TrTradeMessageMapper trTradeMessageMapper;

    @InjectMocks
    private TrTradeMessageServiceImpl trTradeMessageService;

    private MockedStatic<SecurityUtils> securityUtilsMock;
    private MockedStatic<DateUtils> dateUtilsMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityUtilsMock = mockStatic(SecurityUtils.class);
        securityUtilsMock.when(SecurityUtils::getUsername).thenReturn("testUser");
        dateUtilsMock = mockStatic(DateUtils.class);
        dateUtilsMock.when(DateUtils::getNowDate).thenReturn(new Date());
    }

    @AfterEach
    void tearDown() {
        securityUtilsMock.close();
        dateUtilsMock.close();
    }

    @Test
    void testSelectTrTradeMessageByMessageId() {
        TrTradeMessage message = new TrTradeMessage();
        message.setMessageId(1L);
        when(trTradeMessageMapper.selectTrTradeMessageByMessageId(1L)).thenReturn(message);

        TrTradeMessage result = trTradeMessageService.selectTrTradeMessageByMessageId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getMessageId());
    }

    @Test
    void testSelectTrTradeMessageList() {
        TrTradeMessage queryParam = new TrTradeMessage();
        TrTradeMessage message1 = new TrTradeMessage();
        message1.setMessageId(1L);
        TrTradeMessage message2 = new TrTradeMessage();
        message2.setMessageId(2L);

        when(trTradeMessageMapper.selectTrTradeMessageList(queryParam))
                .thenReturn(Arrays.asList(message1, message2));

        List<TrTradeMessage> result = trTradeMessageService.selectTrTradeMessageList(queryParam);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testInsertTrTradeMessage() {
        TrTradeMessage message = new TrTradeMessage();
        when(trTradeMessageMapper.insertTrTradeMessage(any(TrTradeMessage.class))).thenReturn(1);

        int result = trTradeMessageService.insertTrTradeMessage(message);

        assertEquals(1, result);
        assertEquals("testUser", message.getCreateBy());
        assertEquals("testUser", message.getUpdateBy());
        assertNotNull(message.getCreateTime());
        assertNotNull(message.getUpdateTime());
    }

    @Test
    void testUpdateTrTradeMessage() {
        TrTradeMessage message = new TrTradeMessage();
        message.setMessageId(1L);
        when(trTradeMessageMapper.updateTrTradeMessage(any(TrTradeMessage.class))).thenReturn(1);

        int result = trTradeMessageService.updateTrTradeMessage(message);

        assertEquals(1, result);
        assertEquals("testUser", message.getUpdateBy());
        assertNotNull(message.getUpdateTime());
    }

    @Test
    void testDeleteTrTradeMessageByMessageIds() {
        Long[] messageIds = {1L, 2L, 3L};
        when(trTradeMessageMapper.deleteTrTradeMessageByMessageIds(messageIds)).thenReturn(3);

        int result = trTradeMessageService.deleteTrTradeMessageByMessageIds(messageIds);

        assertEquals(3, result);
    }

    @Test
    void testDeleteTrTradeMessageByMessageId() {
        when(trTradeMessageMapper.deleteTrTradeMessageByMessageId(1L)).thenReturn(1);

        int result = trTradeMessageService.deleteTrTradeMessageByMessageId(1L);

        assertEquals(1, result);
    }

    @Test
    void testSelectTrTradeMessageList_Empty() {
        TrTradeMessage queryParam = new TrTradeMessage();
        when(trTradeMessageMapper.selectTrTradeMessageList(queryParam))
                .thenReturn(Collections.emptyList());

        List<TrTradeMessage> result = trTradeMessageService.selectTrTradeMessageList(queryParam);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectTrTradeMessageByMessageId_NotFound() {
        when(trTradeMessageMapper.selectTrTradeMessageByMessageId(999L)).thenReturn(null);

        TrTradeMessage result = trTradeMessageService.selectTrTradeMessageByMessageId(999L);

        assertNull(result);
    }
}