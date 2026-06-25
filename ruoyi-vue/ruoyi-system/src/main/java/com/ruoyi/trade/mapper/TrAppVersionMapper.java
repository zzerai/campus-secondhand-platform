package com.ruoyi.trade.mapper;

import java.util.List;
import com.ruoyi.trade.domain.TrAppVersion;

/**
 * 移动端APK版本Mapper接口
 *
 * @author trading
 * @date 2026-06-05
 */
public interface TrAppVersionMapper
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
     * 查询当前启用的最新版本（status='0' 中 version_code 最大的一条），供移动端检测更新。
     *
     * @return 最新启用版本，无则返回 null
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
     * 删除移动端APK版本
     *
     * @param versionId 版本主键
     * @return 结果
     */
    public int deleteTrAppVersionByVersionId(Long versionId);

    /**
     * 批量删除移动端APK版本
     *
     * @param versionIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteTrAppVersionByVersionIds(Long[] versionIds);
}
