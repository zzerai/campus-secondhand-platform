package com.ruoyi.trade.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.trade.domain.TrStudentUser;

/**
 * 学生用户Mapper接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface TrStudentUserMapper 
{
    /**
     * 查询学生用户
     * 
     * @param userId 学生用户主键
     * @return 学生用户
     */
    public TrStudentUser selectTrStudentUserByUserId(Long userId);

    /**
     * 根据学号查询学生用户
     * 
     * @param studentNo 学号
     * @return 学生用户
     */
    public TrStudentUser selectTrStudentUserByStudentNo(String studentNo);

    /**
     * 根据手机号查询学生用户
     * 
     * @param phone 手机号
     * @return 学生用户
     */
    public TrStudentUser selectTrStudentUserByPhone(String phone);

    /**
     * 校验学号是否唯一（仅查 del_flag='0' 活跃用户）
     *
     * @param studentNo 学号
     * @return 结果
     */
    public TrStudentUser checkStudentNoUnique(String studentNo);

    /**
     * 校验手机号是否唯一（仅查 del_flag='0' 活跃用户）
     *
     * @param phone 手机号
     * @return 结果
     */
    public TrStudentUser checkPhoneUnique(String phone);

    /**
     * 全表（含软删除行）查询学号匹配的记录。
     *
     * <p>DDL 的 uk_student_no / uk_phone 是全表唯一约束，软删除并不释放该约束 ——
     * 注册前必须用此方法识别"软删除占位行"，决定是直接 insert（无行）还是复活旧行
     * （把 del_flag 改回 '0' 并刷新业务字段），避免被 MySQL 抛 Duplicate entry。</p>
     *
     * @param studentNo 学号
     * @return 含 del_flag 字段的完整记录；不存在返回 null
     */
    public TrStudentUser selectAnyByStudentNo(@Param("studentNo") String studentNo);

    /**
     * 全表（含软删除行）查询手机号匹配的记录。同 {@link #selectAnyByStudentNo}。
     *
     * @param phone 手机号
     * @return 含 del_flag 字段的完整记录；不存在返回 null
     */
    public TrStudentUser selectAnyByPhone(@Param("phone") String phone);

    /**
     * 复活软删除的学生用户：把 del_flag 置 '0' 并重置业务字段。
     * 用于注册时学号/手机号命中软删除占位行的场景。
     *
     * @param userId      复活的目标用户ID
     * @param studentNo   新的学号（如果是按 phone 命中并复活，可能传入新学号）
     * @param phone       新的手机号
     * @param password    新密码（已加密）
     * @param nickname    昵称
     * @param updateBy    操作人（一般是学号）
     * @return 影响行数（1 表示复活成功）
     */
    public int reviveStudentUser(@Param("userId") Long userId,
                                  @Param("studentNo") String studentNo,
                                  @Param("phone") String phone,
                                  @Param("password") String password,
                                  @Param("nickname") String nickname,
                                  @Param("updateBy") String updateBy);

    /**
     * 查询学生用户列表
     * 
     * @param trStudentUser 学生用户
     * @return 学生用户集合
     */
    public List<TrStudentUser> selectTrStudentUserList(TrStudentUser trStudentUser);

    /**
     * Query total student user count.
     *
     * @return total count
     */
    public Long selectStudentUserTotalCount();

    /**
     * Query today's new user count.
     *
     * @return today count
     */
    public Long selectTodayNewUserCount();

    /**
     * 新增学生用户
     * 
     * @param trStudentUser 学生用户
     * @return 结果
     */
    public int insertTrStudentUser(TrStudentUser trStudentUser);

    /**
     * 修改学生用户
     * 
     * @param trStudentUser 学生用户
     * @return 结果
     */
    public int updateTrStudentUser(TrStudentUser trStudentUser);

    /**
     * 删除学生用户
     * 
     * @param userId 学生用户主键
     * @return 结果
     */
    public int deleteTrStudentUserByUserId(Long userId);

    /**
     * 批量删除学生用户
     *
     * @param userIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrStudentUserByUserIds(Long[] userIds);

    /**
     * 仅回写最后登录时间，不触发其它字段更新。
     *
     * @param userId        学生用户ID
     * @param lastLoginTime 登录时刻
     * @return 受影响行数
     */
    public int updateLastLoginTime(@Param("userId") Long userId,
                                   @Param("lastLoginTime") Date lastLoginTime);

    /**
     * 加行锁读取用户当前信用分与状态（供信用分变动在事务内串行化），仅取必要列。
     *
     * @param userId 学生用户ID
     * @return 含 creditScore / status 的用户行；不存在或已软删返回 null
     */
    public TrStudentUser selectStudentForUpdate(@Param("userId") Long userId);

    /**
     * 仅更新信用分（信用分变动用，原子写）。
     *
     * @param userId 学生用户ID
     * @param creditScore 新分值
     * @return 受影响行数
     */
    public int updateCreditScore(@Param("userId") Long userId,
                                 @Param("creditScore") int creditScore);

    /**
     * 仅更新账号状态（封禁 / 解禁用，0正常 1临时封禁 2永久封禁）。
     *
     * @param userId 学生用户ID
     * @param status 目标状态
     * @return 受影响行数
     */
    public int updateStatus(@Param("userId") Long userId,
                            @Param("status") String status);
}
