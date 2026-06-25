package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.dto.AppMessageSendDto;
import com.ruoyi.trade.domain.vo.AppConversationVo;
import com.ruoyi.trade.domain.vo.AppMessageVo;
import com.ruoyi.trade.domain.vo.AppAdminInfoVo;

/**
 * 移动端私信咨询 Service。
 *
 * <p>会话粒度为 (我, 对方, 商品)。所有方法均以"当前登录学生用户"视角执行，
 * Controller 层从 JWT 推导 meId 后传入；Service 不读 SecurityContext，便于单元测试。</p>
 */
public interface IAppMessageService
{
    /**
     * 发送一条私信。
     *
     * <p>校验：禁自发自 / 对方存在 / 商品存在 / 内容长度 1-1000 / orderId 若传则必须存在。
     * sender_id 强制为 meId，客户端无法越权指定。</p>
     *
     * @param meId       发送者ID（当前登录用户）
     * @param meUsername 发送者学号，写入 create_by / update_by
     * @param dto        客户端入参
     * @return 新建消息的主键 messageId
     */
    Long sendMessage(Long meId, String meUsername, AppMessageSendDto dto);

    /**
     * 查询当前用户的会话列表（按商品维度聚合）。
     *
     * <p>分页由调用方在 Controller 层用 {@code startPage()} 启动。</p>
     */
    List<AppConversationVo> listConversations(Long meId);

    /**
     * 查询某会话的消息历史。
     *
     * <p>分页由调用方在 Controller 层用 {@code startPage()} 启动；
     * Service 内为每条记录回填 {@link AppMessageVo#getMine()}。</p>
     *
     * @param meId    当前登录用户ID
     * @param peerId  对方ID
     * @param goodsId 关联商品ID
     */
    List<AppMessageVo> listMessages(Long meId, Long peerId, Long goodsId);

    /**
     * 标记某会话内"对方发给我"的未读消息为已读。
     *
     * @return 本次新标记的条数（已经全部读过则为 0）
     */
    int markConversationRead(Long meId, String meUsername, Long peerId, Long goodsId);

    /**
     * 当前用户未读消息总数（用于首页/底 tab 的 badge）。
     */
    long countUnread(Long meId);

    /**
     * 批量删除消息（仅发送者可删），同时清理图片文件。
     *
     * @return 实际删除的条数
     */
    int deleteMessages(Long meId, Long[] messageIds);

    /**
     * 获取管理员信息（用于移动端"联系管理员"功能）。
     *
     * @return 管理员学生用户信息（userId / nickname / avatar）
     */
    AppAdminInfoVo getAdminInfo();

    /**
     * 管理端：查看所有管理员咨询会话列表（goods_id=0）。
     *
     * @return 按用户聚合的会话列表
     */
    List<AppConversationVo> listAdminContactConversations();

    /**
     * 管理端：查看与某用户的管理员咨询消息历史（goods_id=0）。
     *
     * @param adminId 管理员学生用户ID
     * @param userId  对方学生用户ID
     */
    List<AppMessageVo> listAdminContactMessages(Long adminId, Long userId);

    /**
     * 管理端：管理员回复用户咨询。
     *
     * @param adminId      管理员学生用户ID
     * @param adminUsername 管理员用户名（sys_user 的 userName）
     * @param dto          消息内容（receiverId=学生用户ID, content=消息内容）
     * @return 新建消息的主键 messageId
     */
    Long sendAdminReply(Long adminId, String adminUsername, AppMessageSendDto dto);
}
