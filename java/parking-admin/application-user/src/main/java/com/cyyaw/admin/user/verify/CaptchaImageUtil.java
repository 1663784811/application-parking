package com.cyyaw.admin.user.verify;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

/**
 * 验证码图片生成：BufferedImage + Graphics2D 渲染 4 位字母数字 PNG（headless 安全）。
 */
public final class CaptchaImageUtil {

    /** 去除易混淆字符（0/O/I/1 等） */
    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LEN = 4;

    private static final SecureRandom RANDOM = new SecureRandom();

    private CaptchaImageUtil() {
    }

    /** 生成随机验证码文本 */
    public static String randomText() {
        char[] buf = new char[CODE_LEN];
        for (int i = 0; i < CODE_LEN; i++) {
            buf[i] = CHARS[RANDOM.nextInt(CHARS.length)];
        }
        return new String(buf);
    }

    /** 渲染验证码为 PNG 字节流 */
    public static byte[] generate(String text) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            // 背景
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // 噪声线
            for (int i = 0; i < 6; i++) {
                g.setColor(randomColor(200, 255));
                g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                        RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
            }

            // 字符
            g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 28));
            for (int i = 0; i < text.length(); i++) {
                g.setColor(randomColor(20, 130));
                int x = 10 + i * 26;
                int y = 30 + RANDOM.nextInt(6) - 3;
                g.drawString(String.valueOf(text.charAt(i)), x, y);
            }
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        return bos.toByteArray();
    }

    private static Color randomColor(int min, int max) {
        int r = min + RANDOM.nextInt(max - min);
        int g = min + RANDOM.nextInt(max - min);
        int b = min + RANDOM.nextInt(max - min);
        return new Color(r, g, b);
    }

}
