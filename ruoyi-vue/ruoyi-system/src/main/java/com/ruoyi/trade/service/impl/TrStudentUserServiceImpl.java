package com.ruoyi.trade.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.trade.mapper.TrStudentUserMapper;
import com.ruoyi.trade.domain.TrStudentUser;
import com.ruoyi.trade.service.ITrStudentUserService;
import com.ruoyi.trade.domain.vo.ImportResult;
import com.ruoyi.trade.domain.vo.ImportError;

/**
 * 学生用户Service业务层处理
 *
 * @author ruoyi
 * @date 2026-05-11
 */
@Service
public class TrStudentUserServiceImpl implements ITrStudentUserService
{
    @Autowired
    private TrStudentUserMapper trStudentUserMapper;

    /**
     * 查询学生用户
     *
     * @param userId 学生用户主键
     * @return 学生用户
     */
    @Override
    public TrStudentUser selectTrStudentUserByUserId(Long userId)
    {
        return trStudentUserMapper.selectTrStudentUserByUserId(userId);
    }

    /**
     * 查询学生用户列表
     *
     * @param trStudentUser 学生用户
     * @return 学生用户
     */
    @Override
    public List<TrStudentUser> selectTrStudentUserList(TrStudentUser trStudentUser)
    {
        return trStudentUserMapper.selectTrStudentUserList(trStudentUser);
    }

    /**
     * 新增学生用户
     *
     * @param trStudentUser 学生用户
     * @return 结果
     */
    @Override
    public int insertTrStudentUser(TrStudentUser trStudentUser)
    {
        if (!checkStudentNoUnique(trStudentUser))
        {
            throw new ServiceException("新增学生用户'" + trStudentUser.getStudentNo() + "'失败，学号已存在");
        }
        if (StringUtils.isNotEmpty(trStudentUser.getPhone()) && !checkPhoneUnique(trStudentUser))
        {
            throw new ServiceException("新增学生用户'" + trStudentUser.getStudentNo() + "'失败，手机号已存在");
        }
        if (StringUtils.isNotEmpty(trStudentUser.getPassword()))
        {
            trStudentUser.setPassword(SecurityUtils.encryptPassword(trStudentUser.getPassword()));
        }
        trStudentUser.setCreateBy(SecurityUtils.getUsername());
        trStudentUser.setCreateTime(DateUtils.getNowDate());
        return trStudentUserMapper.insertTrStudentUser(trStudentUser);
    }

    /**
     * 修改学生用户
     *
     * @param trStudentUser 学生用户
     * @return 结果
     */
    @Override
    public int updateTrStudentUser(TrStudentUser trStudentUser)
    {
        if (!checkStudentNoUnique(trStudentUser))
        {
            throw new ServiceException("修改学生用户'" + trStudentUser.getStudentNo() + "'失败，学号已存在");
        }
        if (StringUtils.isNotEmpty(trStudentUser.getPhone()) && !checkPhoneUnique(trStudentUser))
        {
            throw new ServiceException("修改学生用户'" + trStudentUser.getStudentNo() + "'失败，手机号已存在");
        }
        // 修改接口不允许直接改密码，避免明文/二次加密；密码请走 resetPwd
        trStudentUser.setPassword(null);
        trStudentUser.setUpdateBy(SecurityUtils.getUsername());
        trStudentUser.setUpdateTime(DateUtils.getNowDate());
        return trStudentUserMapper.updateTrStudentUser(trStudentUser);
    }

    /**
     * 批量删除学生用户
     *
     * @param userIds 需要删除的学生用户主键
     * @return 结果
     */
    @Override
    public int deleteTrStudentUserByUserIds(Long[] userIds)
    {
        return trStudentUserMapper.deleteTrStudentUserByUserIds(userIds);
    }

    /**
     * 删除学生用户信息
     *
     * @param userId 学生用户主键
     * @return 结果
     */
    @Override
    public int deleteTrStudentUserByUserId(Long userId)
    {
        return trStudentUserMapper.deleteTrStudentUserByUserId(userId);
    }

    /**
     * 修改学生用户状态
     */
    @Override
    public int updateUserStatus(TrStudentUser user)
    {
        TrStudentUser update = new TrStudentUser();
        update.setUserId(user.getUserId());
        update.setStatus(user.getStatus());
        update.setUpdateBy(StringUtils.isNotEmpty(user.getUpdateBy()) ? user.getUpdateBy() : SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return trStudentUserMapper.updateTrStudentUser(update);
    }

    /**
     * 重置学生用户密码（密码需调用方加密后传入）
     */
    @Override
    public int resetPwd(TrStudentUser user)
    {
        TrStudentUser update = new TrStudentUser();
        update.setUserId(user.getUserId());
        update.setPassword(user.getPassword());
        update.setUpdateBy(StringUtils.isNotEmpty(user.getUpdateBy()) ? user.getUpdateBy() : SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return trStudentUserMapper.updateTrStudentUser(update);
    }

    /**
     * 校验学号是否唯一。
     *
     * <p>uk_student_no 是全表唯一索引（不区分 del_flag），软删除占位行同样占用唯一键。
     * 使用 selectAnyByStudentNo 全表查避免管理端 insert 时被 MySQL 抛 Duplicate entry —— 改在
     * service 层显式提示"学号已存在（含历史账户）"，更利于排查。</p>
     */
    @Override
    public boolean checkStudentNoUnique(TrStudentUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        TrStudentUser info = trStudentUserMapper.selectAnyByStudentNo(user.getStudentNo());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 校验手机号是否唯一。同 {@link #checkStudentNoUnique}。
     */
    @Override
    public boolean checkPhoneUnique(TrStudentUser user)
    {
        Long userId = StringUtils.isNull(user.getUserId()) ? -1L : user.getUserId();
        TrStudentUser info = trStudentUserMapper.selectAnyByPhone(user.getPhone());
        if (StringUtils.isNotNull(info) && info.getUserId().longValue() != userId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 批量导入学生用户
     *
     * @param studentUserList 学生用户列表
     * @param updateSupport 是否支持更新（true=存在则更新，false=存在则跳过）
     * @return 导入结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResult importStudentUsers(List<TrStudentUser> studentUserList, boolean updateSupport)
    {
        ImportResult result = new ImportResult();
        result.setTotal(studentUserList.size());

        int successCount = 0;
        List<ImportError> errors = new ArrayList<>();

        String currentUser = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();

        for (int i = 0; i < studentUserList.size(); i++)
        {
            TrStudentUser user = studentUserList.get(i);
            int rowIndex = i + 2; // Excel行号（表头行+1）

            try
            {
                // 1. 基础校验
                if (StringUtils.isEmpty(user.getStudentNo()))
                {
                    errors.add(new ImportError(rowIndex, "学号不能为空"));
                    continue;
                }
                if (StringUtils.isEmpty(user.getPhone()))
                {
                    errors.add(new ImportError(rowIndex, "手机号不能为空"));
                    continue;
                }

                // 2. 查重：根据学号查询是否存在
                TrStudentUser existing = trStudentUserMapper.selectTrStudentUserByStudentNo(user.getStudentNo());

                if (existing != null)
                {
                    if (!updateSupport)
                    {
                        errors.add(new ImportError(rowIndex, "学号已存在，请检查或启用更新支持"));
                        continue;
                    }
                    // 更新模式
                    user.setUserId(existing.getUserId());
                    // 保留原有密码（不更新）
                    user.setPassword(null);
                    user.setUpdateBy(currentUser);
                    user.setUpdateTime(now);
                    trStudentUserMapper.updateTrStudentUser(user);
                }
                else
                {
                    // 新增模式
                    if (StringUtils.isNotEmpty(user.getPassword()))
                    {
                        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
                    }
                    else
                    {
                        // 默认密码：123456
                        user.setPassword(SecurityUtils.encryptPassword("123456"));
                    }
                    user.setCreateBy(currentUser);
                    user.setCreateTime(now);
                    trStudentUserMapper.insertTrStudentUser(user);
                }

                successCount++;
            }
            catch (Exception e)
            {
                errors.add(new ImportError(rowIndex, "处理失败：" + e.getMessage()));
            }
        }

        result.setSuccess(successCount);
        result.setFailure(errors.size());
        result.setErrors(errors);

        return result;
    }
}
