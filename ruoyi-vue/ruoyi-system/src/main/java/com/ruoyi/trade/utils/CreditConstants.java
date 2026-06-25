package com.ruoyi.trade.utils;

/**
 * 学生信用分模块常量。
 *
 * <p>变动类型 / 业务类型与 {@code tr_credit_log} 的 change_type / biz_type 列对应；
 * 封禁阈值与时长走 sys_config（缺失时用此处默认值兜底）。</p>
 *
 * @author thr
 */
public class CreditConstants
{
    private CreditConstants() {}

    /* ---------- change_type ---------- */
    public static final String TYPE_ADMIN_ADJUST   = "admin_adjust";
    public static final String TYPE_ORDER_COMPLETE = "order_complete";
    public static final String TYPE_REPORT_VALID   = "report_valid";
    public static final String TYPE_DISPUTE_FAULT  = "dispute_fault";
    public static final String TYPE_ORDER_CANCEL   = "order_cancel";
    public static final String TYPE_EVALUATION     = "evaluation";
    public static final String TYPE_AUTO_BAN       = "auto_ban";
    public static final String TYPE_BAN_RELEASE    = "ban_release";

    /* ---------- biz_type ---------- */
    public static final String BIZ_ADMIN      = "admin";
    public static final String BIZ_SYSTEM     = "system";
    public static final String BIZ_ORDER      = "order";
    public static final String BIZ_REPORT     = "report";
    public static final String BIZ_DISPUTE    = "dispute";
    public static final String BIZ_EVALUATION = "evaluation";

    /* ---------- tr_student_user.status（复用现列，新增 '2'） ---------- */
    public static final String STATUS_NORMAL        = "0";
    public static final String STATUS_TEMP_BAN      = "1";
    public static final String STATUS_PERMANENT_BAN = "2";

    /* ---------- sys_config 键 ---------- */
    public static final String CFG_THRESHOLD   = "credit.ban.threshold";
    public static final String CFG_FIRST_DAYS  = "credit.ban.first.days";
    public static final String CFG_SECOND_DAYS = "credit.ban.second.days";

    /* ---------- 默认值（sys_config 缺失时兜底） ---------- */
    public static final int DEFAULT_THRESHOLD   = 60;
    public static final int DEFAULT_FIRST_DAYS  = 7;
    public static final int DEFAULT_SECOND_DAYS = 30;

    /** 解禁后信用分重置值（缓刑出狱的干净起点） */
    public static final int RESET_SCORE = 60;

    /** 分值下限（无上限） */
    public static final int MIN_SCORE = 0;

    /** 用户初始信用分（与 DDL 默认值一致，selectForUpdate 取到 null 时兜底） */
    public static final int INITIAL_SCORE = 100;

    /* ---------- 各业务事件分值权重（集中维护，调权重只改此处） ---------- */
    /** 订单完成：买卖双方各 +1 */
    public static final int DELTA_ORDER_COMPLETE = 1;
    /** 已确认订单被买家爽约取消：买家 -3 */
    public static final int DELTA_ORDER_CANCEL = -3;
    /** 举报被判定成立：被举报人 -10 */
    public static final int DELTA_REPORT_VALID = -10;
    /** 争议仲裁判定责任：责任方 -10 */
    public static final int DELTA_DISPUTE_FAULT = -10;
    /** 好评(5★)：被评价人 +1 */
    public static final int DELTA_GOOD_REVIEW = 1;
    /** 差评(1-2★)：被评价人 -1 */
    public static final int DELTA_BAD_REVIEW = -1;

    /** 好评分值下界（>=5 视为好评） */
    public static final int GOOD_REVIEW_MIN_SCORE = 5;
    /** 差评分值上界（<=2 视为差评） */
    public static final int BAD_REVIEW_MAX_SCORE = 2;

    /* ---------- 争议责任方（管理员仲裁时手动判定，仅本枚举触发扣分） ---------- */
    /** 被申诉人担责 */
    public static final String FAULT_RESPONDENT = "respondent";
    /** 发起人担责（恶意申诉） */
    public static final String FAULT_APPLICANT = "applicant";
    /** 双方各担 */
    public static final String FAULT_BOTH = "both";
    /** 无责（不扣分） */
    public static final String FAULT_NONE = "none";
}
