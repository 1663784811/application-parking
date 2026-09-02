package com.cyyaw.parking.camera;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * 相机图像工具：把 {@link android.media.Image}（YUV_420_888）转成 {@link Bitmap}，
 * 再把整帧原图或按检测框裁出的车牌图编码成可直接放进上报载荷的 base64 字符串
 * （不含 {@code data:} 前缀）。
 *
 * <p>所有方法都不回收入参位图，由调用方负责其生命周期；
 * 仅 {@link #cropPlateJpegBase64} 回收它自己新建的中间裁剪位图。
 */
final class ImageUtils {

    /** JPEG 压缩质量（0–100）。 */
    private static final int JPEG_QUALITY = 85;

    private ImageUtils() {}

    /**
     * YUV_420_888 -> ARGB_8888。
     */
    static Bitmap yuvToBitmap(Image image) {
        int w = image.getWidth();
        int h = image.getHeight();
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yPlane = planes[0].getBuffer();
        ByteBuffer uPlane = planes[1].getBuffer();
        ByteBuffer vPlane = planes[2].getBuffer();
        int yRowStride = planes[0].getRowStride();
        int uvRowStride = planes[1].getRowStride();
        int uvPixelStride = planes[1].getPixelStride();

        int[] argb = new int[w * h];
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int y = (yPlane.get(j * yRowStride + i) & 0xFF) - 16;
                if (y < 0) y = 0;
                int uv = (j >> 1) * uvRowStride + (i >> 1) * uvPixelStride;
                int u = (uPlane.get(uv) & 0xFF) - 128;
                int v = (vPlane.get(uv) & 0xFF) - 128;

                int r = (int) (1.164f * y + 1.596f * v);
                int g = (int) (1.164f * y - 0.392f * u - 0.813f * v);
                int b = (int) (1.164f * y + 2.017f * u);
                r = clamp255(r);
                g = clamp255(g);
                b = clamp255(b);
                argb[j * w + i] = 0xFF000000 | (r << 16) | (g << 8) | b;
            }
        }
        return Bitmap.createBitmap(argb, w, h, Bitmap.Config.ARGB_8888);
    }

    /** 把位图编码成 JPEG base64 字符串（不含 data: 前缀），不回收入参位图。 */
    static String bitmapToJpegBase64(Bitmap bmp) {
        if (bmp == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    /**
     * 按检测框 box=[x1,y1,x2,y2]（原图坐标）从原图裁出车牌图，编码成 JPEG base64；
     * 裁出的中间位图随即回收。越界自动钳到原图范围内，退化（边长&lt;2）时返回空串。
     */
    static String cropPlateJpegBase64(Bitmap src, float[] box) {
        if (src == null || box == null || box.length < 4) return "";
        int w = src.getWidth(), h = src.getHeight();
        int x1 = Math.max(0, Math.min((int) box[0], w - 1));
        int y1 = Math.max(0, Math.min((int) box[1], h - 1));
        int x2 = Math.max(0, Math.min((int) box[2], w));
        int y2 = Math.max(0, Math.min((int) box[3], h));
        int cw = x2 - x1, ch = y2 - y1;
        if (cw < 2 || ch < 2) return "";
        Bitmap crop;
        try {
            crop = Bitmap.createBitmap(src, x1, y1, cw, ch);
        } catch (IllegalArgumentException e) {
            return "";
        }
        if (crop == src) return "";  // 子区域退化成整图，不当作车牌图
        try {
            return bitmapToJpegBase64(crop);
        } finally {
            crop.recycle();
        }
    }

    /** 把 YUV→RGB 换算出的通道值钳到 [0,255]。 */
    private static int clamp255(int v) {
        return v < 0 ? 0 : Math.min(v, 255);
    }
}
