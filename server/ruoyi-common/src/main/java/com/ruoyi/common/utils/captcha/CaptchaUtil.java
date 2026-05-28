package com.ruoyi.common.utils.captcha;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码生成工具类
 */
public class CaptchaUtil {
    // 验证码字符集（排除易混淆字符：0/O、1/I等）
    private static final char[] CODE_CHAR = {
            '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
            'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private static final int WIDTH = 110;
    private static final int HEIGHT = 38;
    private static final int CODE_COUNT = 4;
    private static final int LINE_COUNT = 150;

    public static CaptchaResult generate() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 24));

        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_COUNT; i++) {
            char c = CODE_CHAR[random.nextInt(CODE_CHAR.length)];
            code.append(c);
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            g.rotate(Math.toRadians(random.nextInt(60) - 30), 25 * i + 10, HEIGHT / 2);
            g.drawString(String.valueOf(c), 25 * i + 10, HEIGHT / 2);
            g.rotate(-Math.toRadians(random.nextInt(60) - 30), 25 * i + 10, HEIGHT / 2);
        }

        for (int i = 0; i < LINE_COUNT; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawLine(random.nextInt(WIDTH), random.nextInt(HEIGHT),
                    random.nextInt(WIDTH), random.nextInt(HEIGHT));
        }

        for (int i = 0; i < WIDTH * HEIGHT / 20; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.fillRect(random.nextInt(WIDTH), random.nextInt(HEIGHT), 1, 1);
        }

        g.dispose();
        return new CaptchaResult(code.toString(), image);
    }

    public static class CaptchaResult {
        private String code;
        private BufferedImage image;

        public CaptchaResult(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }

        public String getCode() { return code; }
        public BufferedImage getImage() { return image; }
    }
}