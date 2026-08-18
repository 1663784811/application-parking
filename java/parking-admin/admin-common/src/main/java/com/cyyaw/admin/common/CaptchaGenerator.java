package com.cyyaw.admin.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * 图形验证码生成工具（限制扭曲范围）
 */
public class CaptchaGenerator {

    // 默认配置
    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_LINE_COUNT = 10;
    private static final float DEFAULT_NOISE_RATE = 0.005f;
    private static final Font DEFAULT_FONT = new Font("Arial", Font.BOLD, 30);
    private static final int MAX_SHEAR_OFFSET = 0; // 最大扭曲偏移量

    /**
     * 生成验证码图片（使用默认配置）
     */
    public static CaptchaResult generate(String code) {
        return generate(code, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_LINE_COUNT, DEFAULT_NOISE_RATE);
    }

    /**
     * 生成验证码图片（完全自定义配置）
     */
    public static CaptchaResult generate(String code, int width, int height, int lineCount, float noiseRate) {
        // 参数验证
        validateParameters(code, width, height, lineCount, noiseRate);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景
        setupBackground(g, width, height);

        // 绘制干扰线
        drawInterferenceLines(g, width, height, lineCount);

        // 绘制验证码文本
        drawText(g, code, width, height);

        // 添加噪点
        addNoise(image, width, height, noiseRate);

        // 应用扭曲效果（限制在宽高范围内）
        applyShearEffect(g, width, height);

        g.dispose();
        return new CaptchaResult(image, code);
    }

    private static void validateParameters(String code, int width, int height, int lineCount, float noiseRate) {
        if (code == null || code.isEmpty()) {
            throw new IllegalArgumentException("验证码文本不能为空");
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("宽度和高度必须大于0");
        }
        if (lineCount < 0) {
            throw new IllegalArgumentException("干扰线数量不能为负数");
        }
        if (noiseRate < 0 || noiseRate > 1) {
            throw new IllegalArgumentException("噪点率必须在0-1之间");
        }
    }

    private static void setupBackground(Graphics2D g, int width, int height) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
    }

    private static void drawInterferenceLines(Graphics2D g, int width, int height, int lineCount) {
        Random random = new Random();
        for (int i = 0; i < lineCount; i++) {
            g.setColor(getRandomColor());
            g.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
    }

    private static void drawText(Graphics2D g, String code, int width, int height) {
        g.setFont(DEFAULT_FONT);
        int charWidth = width / (code.length() + 1);
        Random random = new Random();

        for (int i = 0; i < code.length(); i++) {
            g.setColor(getRandomColor());
            // 控制字符在垂直方向上的位置，确保不超出范围
            int yPos = Math.min(height - 5, height / 2 + random.nextInt(11) - 5);
            g.drawString(String.valueOf(code.charAt(i)), (i + 1) * charWidth, yPos + 10);
        }
    }

    private static void addNoise(BufferedImage image, int width, int height, float noiseRate) {
        Random random = new Random();
        int area = (int) (noiseRate * width * height);
        for (int i = 0; i < area; i++) {
            image.setRGB(random.nextInt(width), random.nextInt(height), random.nextInt(255));
        }
    }

    private static void applyShearEffect(Graphics2D g, int width, int height) {
        // 限制扭曲范围在图片边界内
        shearX(g, width, height);
        shearY(g, width, height);
    }

    private static void shearX(Graphics2D g, int width, int height) {
        Random random = new Random();
        int period = random.nextInt(2) + 2;

        for (int i = 0; i < height; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / period);
            // 限制扭曲偏移量
            d = Math.max(-MAX_SHEAR_OFFSET, Math.min(MAX_SHEAR_OFFSET, d));
            g.copyArea(0, i, width, 1, (int) d, 0);
        }
    }

    private static void shearY(Graphics2D g, int width, int height) {
        Random random = new Random();
        int period = random.nextInt(40) + 10;

        for (int i = 0; i < width; i++) {
            double d = (double) (period >> 1) * Math.sin((double) i / period);
            // 限制扭曲偏移量
            d = Math.max(-MAX_SHEAR_OFFSET, Math.min(MAX_SHEAR_OFFSET, d));
            g.copyArea(i, 0, 1, height, 0, (int) d);
        }
    }

    private static Color getRandomColor() {
        Random random = new Random();
        return new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150));
    }

    public static class CaptchaResult {
        private final BufferedImage image;
        private final String code;

        public CaptchaResult(BufferedImage image, String code) {
            this.image = image;
            this.code = code;
        }

        public BufferedImage getImage() {
            return image;
        }

        public String getCode() {
            return code;
        }

        public String getImageBase64() throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
}