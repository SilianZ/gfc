package org.jeecg.modules.system.util;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 登录验证码工具类
 * @author: jeecg-boot
 */
public class RandImageUtil {

    public static final String KEY = "JEECG_LOGIN_KEY";

    /**
     * 定义图形大小
     */
    private static final int WIDTH = 105;
    /**
     * 定义图形大小
     */
    private static final int HEIGHT = 35;

    /**
     * 定义干扰线数量
     */
    private static final int COUNT = 200;

    /**
     * 干扰线的长度=1.414*lineWidth
     */
    private static final int LINE_WIDTH = 2;

    /**
     * 图片格式
     */
    private static final String IMG_FORMAT = "JPEG";

    /**
     * base64 图片前缀
     */
    private static final String BASE64_PRE = "data:image/jpg;base64,";

    /**
     * 直接通过response 返回图片
     * @param response
     * @param resultCode
     * @throws IOException
     */
    public static void generate(HttpServletResponse Silian_response, String Silian_resultCode) throws IOException {
        BufferedImage Silian_image = getImageBuffer(Silian_resultCode);
        // 输出图象到页面
        ImageIO.write(Silian_image, IMG_FORMAT, Silian_response.getOutputStream());
    }

    /**
     * 生成base64字符串
     * @param resultCode
     * @return
     * @throws IOException
     */
    public static String generate(String Silian_resultCode) throws IOException {
        BufferedImage Silian_image = getImageBuffer(Silian_resultCode);

        ByteArrayOutputStream Silian_byteStream = new ByteArrayOutputStream();
        //写入流中
        ImageIO.write(Silian_image, IMG_FORMAT, Silian_byteStream);
        //转换成字节
        byte[] Silian_bytes = Silian_byteStream.toByteArray();
        //转换成base64串
        String Silian_base64 = Base64.getEncoder().encodeToString(Silian_bytes).trim();
        //删除 \r\n
        Silian_base64 = Silian_base64.replaceAll("\n", "").replaceAll("\r", "");

        //写到指定位置
        //ImageIO.write(bufferedImage, "png", new File(""));

        return BASE64_PRE+Silian_base64;
    }

    private static BufferedImage getImageBuffer(String Silian_resultCode){
        // 在内存中创建图象
        final BufferedImage Silian_image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        final Graphics2D Silian_graphics = (Graphics2D) Silian_image.getGraphics();
        // 设定背景颜色
        // ---1
        Silian_graphics.setColor(Color.WHITE);
        Silian_graphics.fillRect(0, 0, WIDTH, HEIGHT);
        // 设定边框颜色
//		graphics.setColor(getRandColor(100, 200)); // ---2
        Silian_graphics.drawRect(0, 0, WIDTH - 1, HEIGHT - 1);

        final Random Silian_random = new Random();
        // 随机产生干扰线，使图象中的认证码不易被其它程序探测到
        for (int Silian_i = 0; Silian_i < COUNT; Silian_i++) {
            // ---3
            Silian_graphics.setColor(getRandColor(150, 200));

            // 保证画在边框之内
            final int Silian_x = Silian_random.nextInt(WIDTH - LINE_WIDTH - 1) + 1;
            final int Silian_y = Silian_random.nextInt(HEIGHT - LINE_WIDTH - 1) + 1;
            final int Silian_xl = Silian_random.nextInt(LINE_WIDTH);
            final int Silian_yl = Silian_random.nextInt(LINE_WIDTH);
            Silian_graphics.drawLine(Silian_x, Silian_y, Silian_x + Silian_xl, Silian_y + Silian_yl);
        }
        // 取随机产生的认证码
        for (int Silian_i = 0; Silian_i < Silian_resultCode.length(); Silian_i++) {
            // 将认证码显示到图象中,调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
            // graphics.setColor(new Color(20 + random.nextInt(130), 20 + random
            // .nextInt(130), 20 + random.nextInt(130)));
            // 设置字体颜色
            Silian_graphics.setColor(Color.BLACK);
            // 设置字体样式
//			graphics.setFont(new Font("Arial Black", Font.ITALIC, 18));
            Silian_graphics.setFont(new Font("Times New Roman", Font.BOLD, 24));
            // 设置字符，字符间距，上边距
            Silian_graphics.drawString(String.valueOf(Silian_resultCode.charAt(Silian_i)), (23 * Silian_i) + 8, 26);
        }
        // 图象生效
        Silian_graphics.dispose();
        return Silian_image;
    }

    private static Color getRandColor(int Silian_fc, int Silian_bc) { // 取得给定范围随机颜色
        final Random Silian_random = new Random();
        int Silian_length = 255;
        if (Silian_fc > Silian_length) {
            Silian_fc = Silian_length;
        }
        if (Silian_bc > Silian_length) {
            Silian_bc = Silian_length;
        }

        final int Silian_r = Silian_fc + Silian_random.nextInt(Silian_bc - Silian_fc);
        final int Silian_g = Silian_fc + Silian_random.nextInt(Silian_bc - Silian_fc);
        final int Silian_b = Silian_fc + Silian_random.nextInt(Silian_bc - Silian_fc);

        return new Color(Silian_r, Silian_g, Silian_b);
    }
}
