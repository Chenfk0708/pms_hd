package com.jeez.common.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码加密工具类
 *
 * @author Jeez
 */
public class PasswordUtil {

    /**
     * 加密密码
     *
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String rawPassword) {
        return BCrypt.hashpw(rawPassword);
    }

    /**
     * 验证密码
     *
     * @param rawPassword     原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        return BCrypt.checkpw(rawPassword, encryptedPassword);
    }
}