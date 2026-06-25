package com.ruoyi.trade.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.trade.mapper.TrAppVersionMapper;
import com.ruoyi.trade.domain.TrAppVersion;
import com.ruoyi.trade.service.ITrAppVersionService;

/**
 * 移动端APK版本Service业务层处理
 *
 * @author trading
 * @date 2026-06-05
 */
@Service
public class TrAppVersionServiceImpl implements ITrAppVersionService
{
    @Autowired
    private TrAppVersionMapper trAppVersionMapper;

    /**
     * 查询移动端APK版本
     *
     * @param versionId 版本主键
     * @return 移动端APK版本
     */
    @Override
    public TrAppVersion selectTrAppVersionByVersionId(Long versionId)
    {
        return trAppVersionMapper.selectTrAppVersionByVersionId(versionId);
    }

    /**
     * 查询移动端APK版本列表
     *
     * @param trAppVersion 移动端APK版本
     * @return 移动端APK版本
     */
    @Override
    public List<TrAppVersion> selectTrAppVersionList(TrAppVersion trAppVersion)
    {
        return trAppVersionMapper.selectTrAppVersionList(trAppVersion);
    }

    @Override
    public TrAppVersion selectLatestEnabledVersion()
    {
        return trAppVersionMapper.selectLatestEnabledVersion();
    }

    /**
     * 新增移动端APK版本
     *
     * @param trAppVersion 移动端APK版本
     * @return 结果
     */
    @Override
    public int insertTrAppVersion(TrAppVersion trAppVersion)
    {
        String username = SecurityUtils.getUsername();
        trAppVersion.setCreateBy(username);
        trAppVersion.setUpdateBy(username);
        // createTime / updateTime 由 AuditTimeFillInterceptor 兜底
        return trAppVersionMapper.insertTrAppVersion(trAppVersion);
    }

    /**
     * 修改移动端APK版本
     *
     * @param trAppVersion 移动端APK版本
     * @return 结果
     */
    @Override
    public int updateTrAppVersion(TrAppVersion trAppVersion)
    {
        trAppVersion.setUpdateBy(SecurityUtils.getUsername());
        // updateTime 由 AuditTimeFillInterceptor 兜底
        return trAppVersionMapper.updateTrAppVersion(trAppVersion);
    }

    /**
     * 批量删除移动端APK版本（逻辑删除）
     *
     * @param versionIds 需要删除的版本主键
     * @return 结果
     */
    @Override
    public int deleteTrAppVersionByVersionIds(Long[] versionIds)
    {
        return trAppVersionMapper.deleteTrAppVersionByVersionIds(versionIds);
    }

    /**
     * 删除移动端APK版本信息（逻辑删除）
     *
     * @param versionId 版本主键
     * @return 结果
     */
    @Override
    public int deleteTrAppVersionByVersionId(Long versionId)
    {
        return trAppVersionMapper.deleteTrAppVersionByVersionId(versionId);
    }
}
