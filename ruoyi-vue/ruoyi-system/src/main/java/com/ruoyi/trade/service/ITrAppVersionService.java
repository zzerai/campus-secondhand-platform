package com.ruoyi.trade.service;

import java.util.List;
import com.ruoyi.trade.domain.TrAppVersion;

/**
 * 移动端APK版本Service接口
 *
 * @author trading
 * @date 2026-06-05
 */
public interface ITrAppVersionService
{
    /**
     * 查询移动端APK版本
     *
     * @param versionId 版本主键
     * @return 移动端APK版本
     */
    public TrAppVersion selectTrAppVersionByVersionId(Long versionId);

    /**
     * 查询移动端APK版本列表
     *
     * @param trAppVersion 移动端APK版本
     * @return 移动端APK版本集合
     */
    public List<TrAppVersion> selectTrAppVersionList(TrAppVersion trAppVersion);

    /**
     * 查询当前启用的最新版本，供移动端检测更新（无则返回 null）。
     *
     * @return 最新启用版本
     */
    public TrAppVersion selectLatestEnabledVersion();

    /**
     * 新增移动端APK版本
     *
     * @param trAppVersion 移动端APK版本
     * @return 结果
     */
    public int insertTrAppVersion(TrAppVersion trAppVersion);

    /**
     * 修改移动端APK版本
     *
     * @param trAppVersion 移动端APK版本
     * @return 结果
     */
    public int updateTrAppVersion(TrAppVersion trAppVersion);

    /**
     * 批量删除移动端APK版本
     *
     * @param versionIds 需要删除的版本主键集合
     * @return 结果
     */
    public int deleteTrAppVersionByVersionIds(Long[] versionIds);

    /**
     * 删除移动端APK版本信息
     *
     * @param versionId 版本主键
     * @return 结果
     */
    public int deleteTrAppVersionByVersionId(Long versionId);
}
