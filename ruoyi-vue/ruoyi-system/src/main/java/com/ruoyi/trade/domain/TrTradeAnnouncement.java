package com.ruoyi.trade.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 交易公告对象 tr_trade_announcement
 * 
 * @author ruoyi
 * @date 2026-05-25
 */
public class TrTradeAnnouncement extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 公告ID */
    private Long announcementId;

    /** 公告标题 */
    @Excel(name = "公告标题")
    private String title;

    /** 公告内容（支持富文本/HTML） */
    @Excel(name = "公告内容", readConverterExp = "支=持富文本/HTML")
    private String content;

    /** 公告类型：1活动通知，2规则变更，3维护提醒 */
    @Excel(name = "公告类型：1活动通知，2规则变更，3维护提醒")
    private String type;

    /** 是否置顶：0否，1是 */
    @Excel(name = "是否置顶：0否，1是")
    private String isTop;

    /** 发布状态：0草稿，1已发布 */
    @Excel(name = "发布状态：0草稿，1已发布")
    private String publishStatus;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date publishTime;

    /** 封面图地址 */
    @Excel(name = "封面图地址")
    private String coverImage;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setAnnouncementId(Long announcementId) 
    {
        this.announcementId = announcementId;
    }

    public Long getAnnouncementId() 
    {
        return announcementId;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }

    public void setType(String type) 
    {
        this.type = type;
    }

    public String getType() 
    {
        return type;
    }

    public void setIsTop(String isTop) 
    {
        this.isTop = isTop;
    }

    public String getIsTop() 
    {
        return isTop;
    }

    public void setPublishStatus(String publishStatus) 
    {
        this.publishStatus = publishStatus;
    }

    public String getPublishStatus() 
    {
        return publishStatus;
    }

    public void setPublishTime(Date publishTime) 
    {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() 
    {
        return publishTime;
    }

    public void setCoverImage(String coverImage) 
    {
        this.coverImage = coverImage;
    }

    public String getCoverImage() 
    {
        return coverImage;
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
            .append("announcementId", getAnnouncementId())
            .append("title", getTitle())
            .append("content", getContent())
            .append("type", getType())
            .append("isTop", getIsTop())
            .append("publishStatus", getPublishStatus())
            .append("publishTime", getPublishTime())
            .append("coverImage", getCoverImage())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
