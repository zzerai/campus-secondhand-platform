package com.ruoyi.common.core.domain.model;

import java.io.Serializable;

/**
 * 移动端登录返回Token信息
 * 
 * @author ruoyi
 */
public class AppTokenInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 学生用户信息
     */
    private AppStudentUser studentUser;

    public AppTokenInfo(String token)
    {
        this.token = token;
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public AppStudentUser getStudentUser()
    {
        return studentUser;
    }

    public void setStudentUser(AppStudentUser studentUser)
    {
        this.studentUser = studentUser;
    }

    /**
     * 移动端学生用户信息（不含密码等敏感字段）
     */
    public static class AppStudentUser implements Serializable
    {
        private static final long serialVersionUID = 1L;

        private Long userId;

        private String studentNo;

        private String phone;

        private String nickname;

        private String avatar;

        private String contactWay;

        private Long creditScore;

        private String status;

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(Long userId)
        {
            this.userId = userId;
        }

        public String getStudentNo()
        {
            return studentNo;
        }

        public void setStudentNo(String studentNo)
        {
            this.studentNo = studentNo;
        }

        public String getPhone()
        {
            return phone;
        }

        public void setPhone(String phone)
        {
            this.phone = phone;
        }

        public String getNickname()
        {
            return nickname;
        }

        public void setNickname(String nickname)
        {
            this.nickname = nickname;
        }

        public String getAvatar()
        {
            return avatar;
        }

        public void setAvatar(String avatar)
        {
            this.avatar = avatar;
        }

        public String getContactWay()
        {
            return contactWay;
        }

        public void setContactWay(String contactWay)
        {
            this.contactWay = contactWay;
        }

        public Long getCreditScore()
        {
            return creditScore;
        }

        public void setCreditScore(Long creditScore)
        {
            this.creditScore = creditScore;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }
    }
}
