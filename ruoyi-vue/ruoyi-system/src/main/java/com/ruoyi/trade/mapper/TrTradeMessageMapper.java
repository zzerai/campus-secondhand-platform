package com.ruoyi.trade.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrTradeMessage;
import com.ruoyi.trade.domain.vo.AppConversationVo;
import com.ruoyi.trade.domain.vo.AppMessageVo;

/**
 * 私信消息Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-12
 */
public interface TrTradeMessageMapper 
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
     * 删除私信消息
     * 
     * @param messageId 私信消息主键
     * @return 结果
     */
    public int deleteTrTradeMessageByMessageId(Long messageId);

    /**
     * 批量删除私信消息
     *
     * @param messageIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrTradeMessageByMessageIds(Long[] messageIds);

    /**
     * 查询当前用户的会话列表（按商品维度聚合）。
     *
     * <p>聚合规则：以 (peerId, goodsId) 为会话 key，peerId = 消息里我不是的那一方。
     * 仅纳入 goods_id 不为空的消息（不支持"纯人对人"会话）。
     * 取每会话最新一条消息，并 LEFT JOIN 计算 receiver=me 的未读数。</p>
     *
     * @param meId 当前登录学生用户ID
     * @return 会话列表，按 lastTime 倒序；分页由调用方 PageHelper 控制
     */
    public List<AppConversationVo> selectConversations(@Param("meId") Long meId);

    /**
     * 查询某会话内的消息历史（按商品维度的双向消息）。
     *
     * <p>返回 (sender=me,receiver=peer) ∪ (sender=peer,receiver=me) 且 goods_id 相同的活跃消息。
     * mine 字段由后端按 senderId == meId 计算。</p>
     *
     * @param meId    当前登录学生用户ID
     * @param peerId  对方ID
     * @param goodsId 关联商品ID
     * @return 消息列表，按 create_time 倒序；分页由调用方 PageHelper 控制
     */
    public List<AppMessageVo> selectConversationMessages(@Param("meId") Long meId,
                                                         @Param("peerId") Long peerId,
                                                         @Param("goodsId") Long goodsId);

    /**
     * 将某会话中"对方发给我且未读"的消息批量标记为已读。
     *
     * @param meId        当前登录学生用户ID（receiver 必须等于它）
     * @param peerId      对方ID（sender 必须等于它）
     * @param goodsId     关联商品ID
     * @param updateBy    更新者（学号），写入 update_by 审计字段
     * @return 受影响行数 = 本次新标记的消息数
     */
    public int markConversationAsRead(@Param("meId") Long meId,
                                      @Param("peerId") Long peerId,
                                      @Param("goodsId") Long goodsId,
                                      @Param("updateBy") String updateBy);

    /**
     * 统计当前用户全部未读消息数（receiver=me 且 read_status='0' 且 del_flag='0'）。
     *
     * @param meId 当前登录学生用户ID
     * @return 未读总数
     */
    public long countUnread(@Param("meId") Long meId);

    /**
     * 批量按主键查询消息（仅 del_flag='0'）。
     */
    public List<TrTradeMessage> selectTrTradeMessageByMessageIds(@Param("messageIds") Long[] messageIds);

    /**
     * 管理员会话列表（仅 goods_id=0 的管理员咨询）。
     *
     * @param adminId 管理员学生用户ID
     * @return 按用户聚合的会话列表，按 lastTime 倒序
     */
    public List<AppConversationVo> selectAdminContactConversations(@Param("adminId") Long adminId);
}
