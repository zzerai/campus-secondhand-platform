package com.ruoyi.trade.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 移动端APK版本对象 tr_app_version
 *
 * @author trading
 * @date 2026-06-05
 */
public class TrAppVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 版本ID */
    private Long versionId;

    /** 版本名（展示用，如 1.1.0） */
    @Excel(name = "版本名")
    private String versionName;

    /** 版本号（比对键，单调递增，对应 pubspec 构建号） */
    @Excel(name = "版本号")
    private Integer versionCode;

    /** APK 下载地址（完整 URL） */
    @Excel(name = "下载地址")
    private String downloadUrl;

    /** APK 字节大小 */
    @Excel(name = "文件大小")
    private Long fileSize;

    /** APK 文件 SHA-256 */
    private String fileSha256;

    /** 是否强制更新：0否，1是 */
    @Excel(name = "强制更新", readConverterExp = "0=否,1=是")
    private String forceUpdate;

    /** 更新日志 */
    @Excel(name = "更新日志")
    private String updateLog;

    /** 状态：0启用，1停用 */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String status;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setVersionId(Long versionId)
    {
        this.versionId = versionId;
    }

    public Long getVersionId()
    {
        return versionId;
    }

    public void setVersionName(String versionName)
    {
        this.versionName = versionName;
    }

    public String getVersionName()
    {
        return versionName;
    }

    public void setVersionCode(Integer versionCode)
    {
        this.versionCode = versionCode;
    }

    public Integer getVersionCode()
    {
        return versionCode;
    }

    public void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public String getDownloadUrl()
    {
        return downloadUrl;
    }

    public void setFileSize(Long fileSize)
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize()
    {
        return fileSize;
    }

    public void setFileSha256(String fileSha256)
    {
        this.fileSha256 = fileSha256;
    }

    public String getFileSha256()
    {
        return fileSha256;
    }

    public void setForceUpdate(String forceUpdate)
    {
        this.forceUpdate = forceUpdate;
    }

    public String getForceUpdate()
    {
        return forceUpdate;
    }

    public void setUpdateLog(String updateLog)
    {
        this.updateLog = updateLog;
    }

    public String getUpdateLog()
    {
        return updateLog;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("versionId", getVersionId())
            .append("versionName", getVersionName())
            .append("versionCode", getVersionCode())
            .append("downloadUrl", getDownloadUrl())
            .append("fileSize", getFileSize())
            .append("fileSha256", getFileSha256())
            .append("forceUpdate", getForceUpdate())
            .append("updateLog", getUpdateLog())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
