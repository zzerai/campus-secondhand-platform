package com.ruoyi.trade.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.storage.StorageService;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.domain.TrTradeGoods;
import com.ruoyi.trade.domain.TrTradeMessage;
import com.ruoyi.trade.domain.TrTradeOrder;
import com.ruoyi.trade.domain.dto.AppMessageSendDto;
import com.ruoyi.trade.domain.vo.AppAdminInfoVo;
import com.ruoyi.trade.domain.vo.AppConversationVo;
import com.ruoyi.trade.domain.vo.AppMessageVo;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.mapper.TrTradeGoodsMapper;
import com.ruoyi.trade.mapper.TrTradeMessageMapper;
import com.ruoyi.trade.mapper.TrTradeOrderMapper;
import com.ruoyi.trade.service.IAppMessageService;
import com.ruoyi.trade.service.IOnlinePresenceService;

/**
 * 移动端私信咨询 Service 实现。
 *
 * <p>校验链严格：sender 强制取自登录态、对方/商品必须存在、内容长度限制 1000、可选订单存在性校验。
 * 不维护单独的会话表，列表/未读数靠现有索引（idx_sender_id / idx_receiver_id / idx_goods_id）实时聚合，
 * 适配学校项目规模；后期如果消息量上来再考虑加 tr_trade_conversation 缓存。</p>
 */
@Service
public class AppMessageServiceImpl implements IAppMessageService
{
    private static final Logger log = LoggerFactory.getLogger(AppMessageServiceImpl.class);

    /** 消息内容最大长度，与 DDL VARCHAR(1000) 对齐 */
    private static final int MAX_CONTENT_LENGTH = 1000;

    /** 阅读状态：0 未读 */
    private static final String READ_STATUS_UNREAD = "0";

    @Autowired
    private TrTradeMessageMapper trTradeMessageMapper;

    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    @Autowired
    private TrTradeGoodsMapper trTradeGoodsMapper;

    @Autowired
    private TrTradeOrderMapper trTradeOrderMapper;

    @Autowired
    private IOnlinePresenceService onlinePresenceService;

    @Autowired
    private StorageService storageService;

    /** 管理员学生用户ID（来自 application.yml trade.admin-student-id），goodsId=0 时消息发给此用户 */
    @Value("${trade.admin-student-id:1}")
    private Long adminStudentId;

    @Override
    public Long sendMessage(Long meId, String meUsername, AppMessageSendDto dto)
    {
        if (meId == null)
        {
            throw new ServiceException("发送失败，登录态缺失");
        }
        if (dto == null)
        {
            throw new ServiceException("发送失败，参数不能为空");
        }
        if (dto.getReceiverId() == null)
        {
            throw new ServiceException("发送失败，接收人不能为空");
        }
        if (dto.getGoodsId() == null)
        {
            throw new ServiceException("发送失败，关联商品不能为空");
        }
        if (meId.equals(dto.getReceiverId()))
        {
            throw new ServiceException("不能给自己发送消息");
        }

        String content = dto.getContent() == null ? null : dto.getContent().trim();
        if (content == null || content.isEmpty())
        {
            throw new ServiceException("发送失败，消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH)
        {
            throw new ServiceException("消息内容不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }

        TrStudentUser receiver = trStudentUserMapper.selectTrStudentUserByUserId(dto.getReceiverId());
        if (receiver == null)
        {
            throw new ServiceException("发送失败，接收人不存在");
        }

        // goodsId == 0 为管理员会话，跳过商品存在性校验
        if (dto.getGoodsId() != 0)
        {
            TrTradeGoods goods = trTradeGoodsMapper.selectTrTradeGoodsByGoodsId(dto.getGoodsId());
            if (goods == null)
            {
                throw new ServiceException("发送失败，商品不存在或已删除");
            }
        }

        if (dto.getOrderId() != null)
        {
            TrTradeOrder order = trTradeOrderMapper.selectTrTradeOrderByOrderId(dto.getOrderId());
            if (order == null)
            {
                throw new ServiceException("发送失败，关联订单不存在");
            }
        }

        TrTradeMessage message = new TrTradeMessage();
        message.setSenderId(meId);
        message.setReceiverId(dto.getReceiverId());
        message.setGoodsId(dto.getGoodsId());
        message.setOrderId(dto.getOrderId());
        message.setContent(content);
        message.setReadStatus(READ_STATUS_UNREAD);
        message.setCreateBy(meUsername);
        message.setCreateTime(DateUtils.getNowDate());
        message.setUpdateBy(meUsername);
        message.setUpdateTime(DateUtils.getNowDate());
        trTradeMessageMapper.insertTrTradeMessage(message);
        return message.getMessageId();
    }

    @Override
    public List<AppConversationVo> listConversations(Long meId)
    {
        if (meId == null)
        {
            return Collections.emptyList();
        }
        return trTradeMessageMapper.selectConversations(meId);
    }

    @Override
    public List<AppMessageVo> listMessages(Long meId, Long peerId, Long goodsId)
    {
        if (meId == null)
        {
            throw new ServiceException("查询失败，登录态缺失");
        }
        if (peerId == null)
        {
            throw new ServiceException("查询失败，对方ID不能为空");
        }
        if (goodsId == null)
        {
            throw new ServiceException("查询失败，关联商品ID不能为空");
        }
        if (meId.equals(peerId))
        {
            throw new ServiceException("查询失败，对方ID不能是自己");
        }
        List<AppMessageVo> list = trTradeMessageMapper.selectConversationMessages(meId, peerId, goodsId);
        boolean peerOnline = onlinePresenceService.isOnline(peerId);
        for (AppMessageVo vo : list)
        {
            vo.setMine(Objects.equals(vo.getSenderId(), meId));
            vo.setPeerOnline(peerOnline);
        }
        return list;
    }

    @Override
    public int markConversationRead(Long meId, String meUsername, Long peerId, Long goodsId)
    {
        if (meId == null)
        {
            throw new ServiceException("操作失败，登录态缺失");
        }
        if (peerId == null || goodsId == null)
        {
            throw new ServiceException("操作失败，会话标识不完整");
        }
        if (meId.equals(peerId))
        {
            throw new ServiceException("操作失败，对方ID不能是自己");
        }
        return trTradeMessageMapper.markConversationAsRead(meId, peerId, goodsId, meUsername);
    }

    @Override
    public long countUnread(Long meId)
    {
        if (meId == null)
        {
            return 0L;
        }
        return trTradeMessageMapper.countUnread(meId);
    }

    @Override
    public int deleteMessages(Long meId, Long[] messageIds)
    {
        if (meId == null)
        {
            throw new ServiceException("操作失败，登录态缺失");
        }
        if (messageIds == null || messageIds.length == 0)
        {
            return 0;
        }
        List<TrTradeMessage> messages = trTradeMessageMapper.selectTrTradeMessageByMessageIds(messageIds);
        int deleted = 0;
        for (TrTradeMessage msg : messages)
        {
            if (!meId.equals(msg.getSenderId()))
            {
                continue;
            }
            deleteMessageImage(msg.getContent());
            trTradeMessageMapper.deleteTrTradeMessageByMessageId(msg.getMessageId());
            deleted++;
        }
        return deleted;
    }

    @Override
    public AppAdminInfoVo getAdminInfo()
    {
        TrStudentUser admin = trStudentUserMapper.selectTrStudentUserByUserId(adminStudentId);
        AppAdminInfoVo vo = new AppAdminInfoVo();
        if (admin != null)
        {
            vo.setUserId(admin.getUserId());
            vo.setNickname(admin.getNickname() != null ? admin.getNickname() : "管理员");
            vo.setAvatar(admin.getAvatar());
        }
        else
        {
            vo.setUserId(adminStudentId);
            vo.setNickname("管理员");
        }
        return vo;
    }

    @Override
    public List<AppConversationVo> listAdminContactConversations()
    {
        return trTradeMessageMapper.selectAdminContactConversations(adminStudentId);
    }

    @Override
    public List<AppMessageVo> listAdminContactMessages(Long adminId, Long userId)
    {
        if (adminId == null || userId == null)
        {
            return Collections.emptyList();
        }
        List<AppMessageVo> list = trTradeMessageMapper.selectConversationMessages(adminId, userId, 0L);
        for (AppMessageVo vo : list)
        {
            vo.setMine(Objects.equals(vo.getSenderId(), adminId));
        }
        return list;
    }

    @Override
    public Long sendAdminReply(Long adminId, String adminUsername, AppMessageSendDto dto)
    {
        if (adminId == null)
        {
            throw new ServiceException("发送失败，管理员身份缺失");
        }
        if (dto == null || dto.getReceiverId() == null)
        {
            throw new ServiceException("发送失败，接收人不能为空");
        }

        String content = dto.getContent() == null ? null : dto.getContent().trim();
        if (content == null || content.isEmpty())
        {
            throw new ServiceException("发送失败，消息内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH)
        {
            throw new ServiceException("消息内容不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }

        TrStudentUser receiver = trStudentUserMapper.selectTrStudentUserByUserId(dto.getReceiverId());
        if (receiver == null)
        {
            throw new ServiceException("发送失败，接收人不存在");
        }

        TrTradeMessage message = new TrTradeMessage();
        message.setSenderId(adminId);
        message.setReceiverId(dto.getReceiverId());
        message.setGoodsId(0L);
        message.setContent(content);
        message.setReadStatus(READ_STATUS_UNREAD);
        message.setCreateBy(adminUsername);
        message.setCreateTime(DateUtils.getNowDate());
        message.setUpdateBy(adminUsername);
        message.setUpdateTime(DateUtils.getNowDate());
        trTradeMessageMapper.insertTrTradeMessage(message);
        return message.getMessageId();
    }

    /**
     * 删除消息中关联的图片文件。仅处理以 /profile/ 开头的本地存储图片 URL，
     * OSS / CDN 图片跳过（不会被误删）。
     */
    private void deleteMessageImage(String content)
    {
        if (content == null || content.isEmpty())
        {
            return;
        }
        if (content.startsWith("/profile/"))
        {
            try
            {
                storageService.delete(content);
            }
            catch (Exception e)
            {
                log.warn("删除消息关联图片失败: {}", content, e);
            }
        }
    }
}
