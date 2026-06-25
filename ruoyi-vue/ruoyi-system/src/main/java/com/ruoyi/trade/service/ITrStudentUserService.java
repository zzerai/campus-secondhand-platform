package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrStudentUser;

/**
 * 学生用户Service接口
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public interface ITrStudentUserService 
{
    /**
     * 查询学生用户
     * 
     * @param userId 学生用户主键
     * @return 学生用户
     */
    public TrStudentUser selectTrStudentUserByUserId(Long userId);

    /**
     * 查询学生用户列表
     * 
     * @param trStudentUser 学生用户
     * @return 学生用户集合
     */
    public List<TrStudentUser> selectTrStudentUserList(TrStudentUser trStudentUser);

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
     * 批量删除学生用户
     * 
     * @param userIds 需要删除的学生用户主键集合
     * @return 结果
     */
    public int deleteTrStudentUserByUserIds(Long[] userIds);

    /**
     * 删除学生用户信息
     *
     * @param userId 学生用户主键
     * @return 结果
     */
    public int deleteTrStudentUserByUserId(Long userId);

    /**
     * 修改学生用户状态（启用/停用）
     *
     * @param user 仅需 userId 与 status；updateBy 由调用方填充
     * @return 结果
     */
    public int updateUserStatus(TrStudentUser user);

    /**
     * 重置学生用户密码（密码需调用方加密后传入）
     *
     * @param user 仅需 userId 与 password；updateBy 由调用方填充
     * @return 结果
     */
    public int resetPwd(TrStudentUser user);

    /**
     * 校验学号是否唯一
     *
     * @param user 学生用户（含 userId 用于排除自身）
     * @return true=唯一，false=已存在
     */
    public boolean checkStudentNoUnique(TrStudentUser user);

    /**
     * 校验手机号是否唯一
     *
     * @param user 学生用户（含 userId 用于排除自身）
     * @return true=唯一，false=已存在
     */
    public boolean checkPhoneUnique(TrStudentUser user);

    /**
     * 批量导入学生用户
     *
     * @param studentUserList 学生用户列表
     * @param updateSupport 是否支持更新（true=存在则更新，false=存在则跳过）
     * @return 导入结果（成功/失败数量及原因）
     */
    public com.ruoyi.trade.domain.vo.ImportResult importStudentUsers(List<TrStudentUser> studentUserList, boolean updateSupport);
}
