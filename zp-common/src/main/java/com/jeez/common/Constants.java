package com.jeez.common;

/**
 * 系统常量
 *
 * @author Jeez
 */
public class Constants {

    /**
     * 验证码 Redis Key 前缀
     */
    public static final String SMS_CODE_PREFIX = "sms:code:";

    /**
     * 邮箱验证码 Redis Key 前缀
     */
    public static final String EMAIL_CODE_PREFIX = "email:code:";

    /**
     * 验证码有效期（分钟）
     */
    public static final Integer SMS_CODE_EXPIRE = 5;

    /**
     * 邮箱验证码有效期（分钟）
     */
    public static final Integer EMAIL_CODE_EXPIRE = 5;

    /**
     * 微信 OpenID Redis Key 前缀
     */
    public static final String WECHAT_OPENID_PREFIX = "wechat:openid:";

    /**
     * 用户角色：管理员
     */
    public static final String ROLE_ADMIN = "admin";

    /**
     * 用户角色：教练
     */
    public static final String ROLE_COACH = "coach";

    /**
     * 用户角色：普通会员
     */
    public static final String ROLE_MEMBER = "member";

    /**
     * 用户角色：游客
     */
    public static final String ROLE_GUEST = "guest";

    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
}
