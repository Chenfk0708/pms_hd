package com.jeez.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * IP地址工具类
 *
 * @author jeez
 * @since 2025-12-15
 */
@Slf4j
public class IpUtils {

    private static final Pattern IP_PATTERN = Pattern.compile(
        "^([1-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])(\\.([1-9]|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5])){3}$"
    );

    private static final String UNKNOWN = "unknown";

    /**
     * 获取客户端IP地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return isValidIP(ip) ? ip : UNKNOWN;
    }

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 验证IP地址格式
     */
    public static boolean isValidIP(String ip) {
        return ip != null && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 检查是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIP(ip)) {
            return false;
        }

        String[] parts = ip.split("\\.");
        int first = Integer.parseInt(parts[0]);
        int second = Integer.parseInt(parts[1]);

        return first == 10 ||
               (first == 172 && second >= 16 && second <= 31) ||
               (first == 192 && second == 168);
    }

    /**
     * 获取主机名
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error("获取主机名失败", e);
            return "unknown";
        }
    }

    /**
     * 获取本地IP地址
     */
    public static String getLocalIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取本地IP失败", e);
            return "127.0.0.1";
        }
    }
}