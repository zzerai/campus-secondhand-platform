package com.ruoyi.common.constant;

/**
 * 缓存的key 常量
 * 
 * @author ruoyi
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = "pwd_err_cnt:";

    /**
     * 启用商品分类列表 cache key
     */
    public static final String TRADE_CATEGORY_KEY = "trade_category:";

    /**
     * 交易统计 cache key
     */
    public static final String TRADE_STATISTICS_KEY = "trade_statistics:";

    /**
     * 移动端公告列表 cache key
     */
    public static final String TRADE_ANNOUNCEMENT_KEY = "trade_announcement:";

    /**
     * 商品浏览量待回刷增量 cache key（Redis Hash：field=goodsId，value=待回刷增量）
     */
    public static final String TRADE_GOODS_VIEW_KEY = "trade_goods_view:";

    /**
     * 移动端学生用户在线心跳 redis key（key=app_online:userId，靠 TTL 自动过期）
     */
    public static final String APP_ONLINE_KEY = "app_online:";
}
