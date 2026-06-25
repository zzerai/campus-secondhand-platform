package com.ruoyi.trade.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 学生用户对象 tr_student_user
 * 
 * @author ruoyi
 * @date 2026-05-11
 */
public class TrStudentUser extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生用户ID */
    private Long userId;

    /** 学号 */
    @Excel(name = "学号")
    private String studentNo;

    /** 手机号 */
    @Excel(name = "手机号")
    private String phone;

    /** 密码（出参不暴露；入参允许接收明文，由 Service 层加密入库） */
    private String password;

    /** 昵称 */
    @Excel(name = "昵称")
    private String nickname;

    /** 头像 */
    @Excel(name = "头像")
    private String avatar;

    /** 联系方式 */
    @Excel(name = "联系方式")
    private String contactWay;

    /** 信用分 */
    @Excel(name = "信用分")
    private Long creditScore;

    /** 账号状态：0正常，1禁用 */
    @Excel(name = "账号状态：0正常，1禁用")
    private String status;

    /** 最后登录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastLoginTime;

    /** 删除标志：0存在，2删除 */
    private String delFlag;

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setStudentNo(String studentNo) 
    {
        this.studentNo = studentNo;
    }

    public String getStudentNo() 
    {
        return studentNo;
    }

    public void setPhone(String phone) 
    {
        this.phone = phone;
    }

    public String getPhone() 
    {
        return phone;
    }

    @JsonProperty
    public void setPassword(String password)
    {
        this.password = password;
    }

    @JsonIgnore
    public String getPassword()
    {
        return password;
    }

    public void setNickname(String nickname) 
    {
        this.nickname = nickname;
    }

    public String getNickname() 
    {
        return nickname;
    }

    public void setAvatar(String avatar) 
    {
        this.avatar = avatar;
    }

    public String getAvatar() 
    {
        return avatar;
    }

    public void setContactWay(String contactWay) 
    {
        this.contactWay = contactWay;
    }

    public String getContactWay() 
    {
        return contactWay;
    }

    public void setCreditScore(Long creditScore) 
    {
        this.creditScore = creditScore;
    }

    public Long getCreditScore() 
    {
        return creditScore;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setLastLoginTime(Date lastLoginTime) 
    {
        this.lastLoginTime = lastLoginTime;
    }

    public Date getLastLoginTime() 
    {
        return lastLoginTime;
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
            .append("userId", getUserId())
            .append("studentNo", getStudentNo())
            .append("phone", getPhone())
            .append("nickname", getNickname())
            .append("avatar", getAvatar())
            .append("contactWay", getContactWay())
            .append("creditScore", getCreditScore())
            .append("status", getStatus())
            .append("lastLoginTime", getLastLoginTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("delFlag", getDelFlag())
            .append("remark", getRemark())
            .toString();
    }
}
