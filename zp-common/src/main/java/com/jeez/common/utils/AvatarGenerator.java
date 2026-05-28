package com.jeez.common.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 头像生成工具类
 * 生成基于用户名首字母的彩色头像（Data URL 格式）
 *
 * @author Jeez
 */
public class AvatarGenerator {

    private static final int SIZE = 200;
    private static final int FONT_SIZE = 80;

    // 预定义的柔和背景色
    private static final Color[] COLORS = {
            new Color(255, 107, 107),  // 珊瑚红
            new Color(78, 205, 196),   // 青绿色
            new Color(69, 183, 209),   // 天蓝色
            new Color(255, 195, 113),  // 橙黄色
            new Color(162, 155, 254),  // 紫色
            new Color(108, 92, 231),   // 深紫色
            new Color(255, 159, 243),  // 粉红色
            new Color(85, 239, 196),   // 薄荷绿
            new Color(129, 236, 236),  // 浅蓝色
            new Color(255, 234, 167)   // 浅黄色
    };

    /**
     * 根据用户名生成头像
     *
     * @param name 用户名
     * @return Base64 编码的头像 Data URL
     */
    public static String generateAvatar(String name) {
        if (StrUtil.isBlank(name)) {
            name = "?";
        }

        // 获取首字母（支持中文和英文）
        String letter = getFirstLetter(name);

        // 根据用户名选择背景色
        Color backgroundColor = selectColor(name);

        try {
            // 创建图像
            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            // 开启抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 绘制背景
            g2d.setColor(backgroundColor);
            g2d.fillRect(0, 0, SIZE, SIZE);

            // 绘制文字
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, FONT_SIZE));

            // 计算文字居中位置
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(letter);
            int textHeight = fm.getAscent();
            int x = (SIZE - textWidth) / 2;
            int y = (SIZE + textHeight) / 2 - fm.getDescent();

            g2d.drawString(letter, x, y);
            g2d.dispose();

            // 转换为 Base64 Data URL
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64 = Base64.encode(imageBytes);

            return "data:image/png;base64," + base64;

        } catch (IOException e) {
            throw new RuntimeException("生成头像失败", e);
        }
    }

    /**
     * 获取首字母（中文取拼音首字母，英文取第一个字符）
     */
    private static String getFirstLetter(String name) {
        if (StrUtil.isBlank(name)) {
            return "?";
        }

        char firstChar = name.trim().charAt(0);

        // 如果是英文字母或数字
        if ((firstChar >= 'a' && firstChar <= 'z') || (firstChar >= 'A' && firstChar <= 'Z')) {
            return String.valueOf(firstChar).toUpperCase();
        }

        // 如果是数字
        if (firstChar >= '0' && firstChar <= '9') {
            return String.valueOf(firstChar);
        }

        // 如果是中文，直接取第一个字
        if (firstChar >= 0x4E00 && firstChar <= 0x9FA5) {
            return String.valueOf(firstChar);
        }

        return "?";
    }

    /**
     * 根据名字选择背景色（保证同一个名字总是相同的颜色）
     */
    private static Color selectColor(String name) {
        int hash = name.hashCode();
        int index = Math.abs(hash) % COLORS.length;
        return COLORS[index];
    }

    /**
     * 生成随机颜色的头像
     */
    public static String generateRandomAvatar(String name) {
        if (StrUtil.isBlank(name)) {
            name = "?";
        }

        String letter = getFirstLetter(name);
        Random random = new Random();
        Color randomColor = COLORS[random.nextInt(COLORS.length)];

        try {
            BufferedImage image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2d.setColor(randomColor);
            g2d.fillRect(0, 0, SIZE, SIZE);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Microsoft YaHei", Font.BOLD, FONT_SIZE));

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(letter);
            int textHeight = fm.getAscent();
            int x = (SIZE - textWidth) / 2;
            int y = (SIZE + textHeight) / 2 - fm.getDescent();

            g2d.drawString(letter, x, y);
            g2d.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64 = Base64.encode(imageBytes);

            return "data:image/png;base64," + base64;

        } catch (IOException e) {
            throw new RuntimeException("生成头像失败", e);
        }
    }
}