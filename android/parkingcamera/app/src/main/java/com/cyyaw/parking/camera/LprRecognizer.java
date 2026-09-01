package com.cyyaw.parking.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

/**
 * HyperLPR3 ONNX 推理封装：检测(y5fu_320x) -> 透视矫正 -> 识别(rpv3_mdict) -> 分类(litemodel_cls)。
 *
 * <p>算法严格复刻 hyperlpr3 0.1.3 的 LPRMultiTaskPipeline：
 * <ul>
 *   <li>det 输入 320x320 RGB /255（letter_box 居中黑边）；输出 [1,6300,15]，后处理
 *       置信度过滤 0.25 + xywh2xyxy + NMS(0.5) + 坐标还原（restore_box）</li>
 *   <li>识别输入 48x160，BGR，(x-127.5)/127.5，右侧补 0；输出 [1,20,78]，CTC 解码
 *       （跳 blank、去连续重复、取平均置信度）</li>
 *   <li>分类输入 96x96 BGR /255；输出 3 类（蓝/绿/黄）</li>
 * </ul>
 */
public class LprRecognizer {

    // ───── plate type constants（与 hyperlpr3.common.typedef 一致）─────
    public static final int PLATE_UNKNOWN = -1;
    public static final int PLATE_BLUE = 0;
    public static final int PLATE_YELLOW_SINGLE = 1;
    public static final int PLATE_WHITE_SINGLE = 2;
    public static final int PLATE_GREEN = 3;
    public static final int PLATE_BLACK_HK_MACAO = 4;
    public static final int PLATE_HK_SINGLE = 5;
    public static final int PLATE_HK_DOUBLE = 6;
    public static final int PLATE_MACAO_SINGLE = 7;
    public static final int PLATE_MACAO_DOUBLE = 8;
    public static final int PLATE_YELLOW_DOUBLE = 9;

    private static final int MONO = 0;    // 单层车牌
    private static final int DOUBLE = 1;  // 双层车牌

    private static final int DET_SIZE = 320;
    private static final int DET_ANCHORS = 6300;   // det 输出 [1,6300,15]
    private static final float DET_CONF_THRESH = 0.25f;
    private static final float NMS_IOU_THRESH = 0.5f;

    // 识别模型固定输入 48x160（onnx 元数据 [1,3,48,160]）
    private static final int REC_H = 48;
    private static final int REC_W = 160;
    private static final int REC_STEPS = 20;
    private static final int REC_CLASSES = 78;

    private static final int CLS_SIZE = 96;

    // ───── 字符表（与 hyperlpr3.common.tokenize.token 完全一致，77 项）─────
    private static final String[] CHARS = {
            "blank", // 0
            "'",     // 1
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
            "云", "京", "冀", "吉", "学", "宁", "川", "挂", "新", "晋", "桂",
            "民", "沪", "津", "浙", "渝", "港", "湘", "琼", "甘", "皖", "粤",
            "航", "苏", "蒙", "藏", "警", "豫", "贵", "赣", "辽", "鄂", "闽",
            "陕", "青", "鲁", "黑", "领", "使", "澳",
    };

    private final OrtEnvironment env;
    private final OrtSession detSession;
    private final OrtSession recSession;
    private final OrtSession clsSession;

    public LprRecognizer(@NonNull Context context) throws IOException, OrtException {
        env = OrtEnvironment.getEnvironment();
        detSession = loadSession(context, "hyperlpr3/y5fu_320x_sim.onnx");
        recSession = loadSession(context, "hyperlpr3/rpv3_mdict_160_r3.onnx");
        clsSession = loadSession(context, "hyperlpr3/litemodel_cls_96x_r1.onnx");
    }

    private OrtSession loadSession(Context context, String assetPath) throws IOException, OrtException {
        byte[] bytes;
        try (InputStream is = context.getAssets().open(assetPath);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            bytes = bos.toByteArray();
        }
        return env.createSession(bytes, new OrtSession.SessionOptions());
    }

    public void close() {
        try {
            detSession.close();
        } catch (OrtException ignored) {
        }
        try {
            recSession.close();
        } catch (OrtException ignored) {
        }
        try {
            clsSession.close();
        } catch (OrtException ignored) {
        }
    }

    /** 单条识别结果。 */
    public static class PlateResult {
        public String plate;
        public float confidence;        // 字符平均置信度（rec_confidence）
        public int plateType = PLATE_UNKNOWN;
        public float[] box;             // x1,y1,x2,y2（原图坐标）
        public float[][] vertex;        // 4 个角点，用于画框/裁剪

        @NonNull
        @Override
        public String toString() {
            return "PlateResult{plate='" + plate + "', conf=" + confidence
                    + ", type=" + plateType + "}";
        }
    }

    /**
     * 识别单帧。返回符合长度约束(>=7)的车牌列表；调用方自行取置信度最高者。
     *
     * @param frame RGB 位图（ARGB_8888，横向画面）
     */
    public List<PlateResult> recognize(@NonNull Bitmap frame) throws OrtException {
        List<PlateResult> results = new ArrayList<>();

        // ── 1. 检测 ──
        LetterBox lb = letterBox(frame, DET_SIZE);
        float[] detInput = detPreprocess(lb);
        float[] detOut = run(detSession, "input", detInput,
                new long[]{1, 3, DET_SIZE, DET_SIZE}, DET_ANCHORS * 15);
        List<float[]> dets = detPostprocess(detOut, lb);

        // ── 2. 逐候选：透视矫正 + 识别 + 类型 ──
        for (float[] d : dets) {
            // d 布局：[:4]=rect, [4]=score, [5:13]=landmarks(4,2), [13]=layer
            float[][] lm = new float[4][2];
            for (int i = 0; i < 4; i++) {
                lm[i][0] = d[5 + i * 2];
                lm[i][1] = d[6 + i * 2];
            }
            int layer = (int) d[13];

            Bitmap pad = rotateCrop(frame, lm);
            if (pad == null || pad.getWidth() < 2 || pad.getHeight() < 2) {
                continue;
            }

            String plate;
            float conf;
            if (layer == DOUBLE) {
                int h = pad.getHeight();
                int line = (int) (h * 0.4f);
                Bitmap top = Bitmap.createBitmap(pad, 0, 0, pad.getWidth(), line);
                Bitmap bottom = Bitmap.createBitmap(pad, 0, line, pad.getWidth(), h - line);
                RecResult r1 = rec(top);
                RecResult r2 = rec(bottom);
                if (r1.text == null || r1.text.isEmpty()
                        || r2.text == null || r2.text.isEmpty()) {
                    continue;
                }
                plate = r1.text + r2.text;
                conf = (r1.conf + r2.conf) / 2f;
            } else {
                RecResult r1 = rec(pad);
                if (r1.text == null || r1.text.isEmpty()) {
                    continue;
                }
                plate = r1.text;
                conf = r1.conf;
            }

            if (plate.length() < 7) {
                continue;
            }

            int plateType = codeFilter(plate);
            if (plateType == PLATE_UNKNOWN) {
                int idx = argmax(run(clsSession, "data", clsPreprocess(pad),
                        new long[]{1, 3, CLS_SIZE, CLS_SIZE}, 3));
                // 分类模型输出顺序：蓝 / 绿 / 黄
                if (idx == 2) {
                    plateType = (layer == DOUBLE) ? PLATE_YELLOW_DOUBLE : PLATE_YELLOW_SINGLE;
                } else if (idx == 0) {
                    plateType = PLATE_BLUE;
                } else if (idx == 1) {
                    plateType = PLATE_GREEN;
                }
            }

            PlateResult r = new PlateResult();
            r.plate = plate;
            r.confidence = conf;
            r.plateType = plateType;
            r.box = new float[]{d[0], d[1], d[2], d[3]};
            r.vertex = lm;
            results.add(r);
        }
        return results;
    }

    // ────────────────────── 检测 ──────────────────────

    /** letter_box：等比例缩放 + 居中黑边，返回填充后的位图及还原参数。 */
    private static class LetterBox {
        Bitmap padded;   // DET_SIZE x DET_SIZE 黑底，内容居中
        float r;         // 缩放比（原图 -> 320）
        int left, top;   // 黑边偏移
    }

    private static LetterBox letterBox(Bitmap frame, int size) {
        int h = frame.getHeight();
        int w = frame.getWidth();
        float r = Math.min((float) size / h, (float) size / w);
        int newH = (int) (h * r);
        int newW = (int) (w * r);
        int top = (int) ((size - newH) / 2.0);
        int left = (int) ((size - newW) / 2.0);

        Bitmap scaled = Bitmap.createScaledBitmap(frame, newW, newH, true);
        Bitmap padded = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(padded);
        c.drawColor(Color.BLACK);
        c.drawBitmap(scaled, left, top, null);
        if (scaled != frame) {
            scaled.recycle();
        }

        LetterBox lb = new LetterBox();
        lb.padded = padded;
        lb.r = r;
        lb.left = left;
        lb.top = top;
        return lb;
    }

    /** det 前处理：CHW RGB /255。 */
    private static float[] detPreprocess(LetterBox lb) {
        Bitmap bmp = lb.padded;
        int size = DET_SIZE;
        int[] px = new int[size * size];
        bmp.getPixels(px, 0, size, 0, 0, size, size);
        float[] out = new float[3 * size * size];
        int plane = size * size;
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            out[i] = ((p >> 16) & 0xFF) / 255f;       // R
            out[plane + i] = ((p >> 8) & 0xFF) / 255f; // G
            out[2 * plane + i] = (p & 0xFF) / 255f;    // B
        }
        return out;
    }

    /** det 后处理：置信度过滤 + xywh2xyxy + NMS + 坐标还原。 */
    private static List<float[]> detPostprocess(float[] raw, LetterBox lb) {
        int total = DET_ANCHORS * 15;
        List<float[]> cand = new ArrayList<>();
        for (int i = 0; i < total; i += 15) {
            float objConf = raw[i + 4];
            if (objConf <= DET_CONF_THRESH) {
                continue;
            }
            float c0 = raw[i + 13] * objConf;
            float c1 = raw[i + 14] * objConf;
            float score = Math.max(c0, c1);
            int layer = (c1 > c0) ? 1 : 0;

            float cx = raw[i], cy = raw[i + 1], bw = raw[i + 2], bh = raw[i + 3];
            float x1 = cx - bw / 2f, y1 = cy - bh / 2f, x2 = cx + bw / 2f, y2 = cy + bh / 2f;

            float[] row = new float[14];
            row[0] = x1;
            row[1] = y1;
            row[2] = x2;
            row[3] = y2;
            row[4] = score;
            System.arraycopy(raw, i + 5, row, 5, 8);
            row[13] = layer;
            cand.add(row);
        }

        List<float[]> kept = nms(cand, NMS_IOU_THRESH);
        // 还原到原图坐标：减去黑边偏移，除以缩放比
        for (float[] d : kept) {
            for (int c : new int[]{0, 2, 5, 7, 9, 11}) {
                d[c] = (d[c] - lb.left) / lb.r;
            }
            for (int c : new int[]{1, 3, 6, 8, 10, 12}) {
                d[c] = (d[c] - lb.top) / lb.r;
            }
        }
        return kept;
    }

    /** NMS（与 hyperlpr3 的 nms 一致，按 score 降序、IoU 阈值）。 */
    private static List<float[]> nms(List<float[]> boxes, float iouThresh) {
        List<float[]> keep = new ArrayList<>();
        if (boxes.isEmpty()) {
            return keep;
        }
        boolean[] suppressed = new boolean[boxes.size()];
        int[] order = new int[boxes.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        // 按 score 降序（简单选择排序）
        for (int i = 0; i < order.length; i++) {
            int best = i;
            for (int j = i + 1; j < order.length; j++) {
                if (boxes.get(order[j])[4] > boxes.get(order[best])[4]) {
                    best = j;
                }
            }
            int tmp = order[i];
            order[i] = order[best];
            order[best] = tmp;
        }

        for (int i = 0; i < order.length; i++) {
            int idx = order[i];
            if (suppressed[idx]) {
                continue;
            }
            float[] a = boxes.get(idx);
            keep.add(a);
            for (int j = i + 1; j < order.length; j++) {
                int jdx = order[j];
                if (suppressed[jdx]) {
                    continue;
                }
                float[] b = boxes.get(jdx);
                if (iou(a, b) > iouThresh) {
                    suppressed[jdx] = true;
                }
            }
        }
        return keep;
    }

    private static float iou(float[] a, float[] b) {
        float ax1 = a[0], ay1 = a[1], ax2 = a[2], ay2 = a[3];
        float bx1 = b[0], by1 = b[1], bx2 = b[2], by2 = b[3];
        float ix1 = Math.max(ax1, bx1), iy1 = Math.max(ay1, by1);
        float ix2 = Math.min(ax2, bx2), iy2 = Math.min(ay2, by2);
        float iw = Math.max(0f, ix2 - ix1), ih = Math.max(0f, iy2 - iy1);
        float inter = iw * ih;
        float areaA = (ax2 - ax1) * (ay2 - ay1);
        float areaB = (bx2 - bx1) * (by2 - by1);
        float uni = areaA + areaB - inter;
        return uni > 0f ? inter / uni : 0f;
    }

    // ────────────────────── 透视矫正 ──────────────────────

    /** 透视矫正裁剪：复刻 get_rotate_crop_image。 */
    private static Bitmap rotateCrop(Bitmap src, float[][] lm) {
        // lm: 4 个角点 [左上, 右上, 右下, 左下]
        float wTop = dist(lm[0], lm[1]);
        float wBot = dist(lm[2], lm[3]);
        float hLef = dist(lm[0], lm[3]);
        float hRig = dist(lm[1], lm[2]);
        int cropW = Math.max((int) wTop, (int) wBot);
        int cropH = Math.max((int) hLef, (int) hRig);
        if (cropW < 1 || cropH < 1) {
            return null;
        }
        float[] srcPts = new float[]{lm[0][0], lm[0][1], lm[1][0], lm[1][1],
                lm[2][0], lm[2][1], lm[3][0], lm[3][1]};
        float[] dstPts = new float[]{0, 0, cropW, 0, cropW, cropH, 0, cropH};
        Matrix m = new Matrix();
        boolean ok = m.setPolyToPoly(srcPts, 0, dstPts, 0, 4);
        if (!ok) {
            return null;
        }
        Bitmap out = Bitmap.createBitmap(cropW, cropH, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(out);
        c.drawColor(Color.BLACK);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        c.drawBitmap(src, m, paint);
        if (cropH * 1.0f / cropW >= 1.5f) {
            // cv2.rot90 逆时针旋转 90°
            Matrix rot = new Matrix();
            rot.postRotate(-90);
            Bitmap rotated = Bitmap.createBitmap(out, 0, 0, cropW, cropH, rot, true);
            if (rotated != out) {
                out.recycle();
            }
            out = rotated;
        }
        return out;
    }

    private static float dist(float[] a, float[] b) {
        float dx = a[0] - b[0], dy = a[1] - b[1];
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    // ────────────────────── 识别 ──────────────────────

    private static class RecResult {
        String text;
        float conf;
    }

    /** 识别前处理：BGR，(x-127.5)/127.5，右侧补 0（复刻 recognition.encode_images）。 */
    private static float[] recPreprocess(Bitmap bmp) {
        int h = bmp.getHeight();
        int w = bmp.getWidth();
        float ratio = w * 1.0f / h;
        int ratioImgH = (int) Math.ceil(REC_H * ratio);
        ratioImgH = Math.max(ratioImgH, REC_H);
        int resizedW = Math.min(ratioImgH, REC_W);
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, resizedW, REC_H, true);
        int[] px = new int[resizedW * REC_H];
        scaled.getPixels(px, 0, resizedW, 0, 0, resizedW, REC_H);
        float[] out = new float[3 * REC_H * REC_W];
        int plane = REC_H * REC_W;
        for (int y = 0; y < REC_H; y++) {
            for (int x = 0; x < resizedW; x++) {
                int p = px[y * resizedW + x];
                int idx = y * REC_W + x;
                out[idx] = ((p & 0xFF) - 127.5f) / 127.5f;               // B
                out[plane + idx] = (((p >> 8) & 0xFF) - 127.5f) / 127.5f;  // G
                out[2 * plane + idx] = (((p >> 16) & 0xFF) - 127.5f) / 127.5f; // R
            }
        }
        if (scaled != bmp) {
            scaled.recycle();
        }
        return out;
    }

    /** 识别单块：跑 rec 模型 + CTC 解码（跳 blank、去连续重复、平均置信度）。 */
    private RecResult rec(Bitmap pad) throws OrtException {
        float[] input = recPreprocess(pad);
        float[] prod = run(recSession, "data", input,
                new long[]{1, 3, REC_H, REC_W}, REC_STEPS * REC_CLASSES);
        int[] arg = new int[REC_STEPS];
        for (int t = 0; t < REC_STEPS; t++) {
            int best = 0;
            float bestVal = -Float.MAX_VALUE;
            for (int c = 0; c < REC_CLASSES; c++) {
                float v = prod[t * REC_CLASSES + c];
                if (v > bestVal) {
                    bestVal = v;
                    best = c;
                }
            }
            arg[t] = best;
        }
        StringBuilder sb = new StringBuilder();
        float confSum = 0f;
        int confN = 0;
        for (int t = 0; t < REC_STEPS; t++) {
            if (arg[t] == 0) {
                continue; // blank
            }
            if (t > 0 && arg[t - 1] == arg[t]) {
                continue; // 去连续重复
            }
            if (arg[t] >= CHARS.length) {
                continue; // 越界保护（模型 78 类 vs 字符表 77 项）
            }
            sb.append(CHARS[arg[t]]);
            confSum += prod[t * REC_CLASSES + arg[t]];
            confN++;
        }
        RecResult r = new RecResult();
        r.text = sb.toString();
        r.conf = confN > 0 ? confSum / confN : 0f;
        return r;
    }

    // ────────────────────── 分类 ──────────────────────

    /** 分类前处理：BGR /255（复刻 classification.encode_images）。 */
    private static float[] clsPreprocess(Bitmap bmp) {
        Bitmap scaled = Bitmap.createScaledBitmap(bmp, CLS_SIZE, CLS_SIZE, true);
        int[] px = new int[CLS_SIZE * CLS_SIZE];
        scaled.getPixels(px, 0, CLS_SIZE, 0, 0, CLS_SIZE, CLS_SIZE);
        float[] out = new float[3 * CLS_SIZE * CLS_SIZE];
        int plane = CLS_SIZE * CLS_SIZE;
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            out[i] = (p & 0xFF) / 255f;                 // B
            out[plane + i] = ((p >> 8) & 0xFF) / 255f;  // G
            out[2 * plane + i] = ((p >> 16) & 0xFF) / 255f; // R
        }
        if (scaled != bmp) {
            scaled.recycle();
        }
        return out;
    }

    // ────────────────────── ONNX 推理 ──────────────────────

    /** 单次 ONNX 推理，返回展平的 float 输出。 */
    private float[] run(OrtSession session, String inputName, float[] data,
                        long[] shape, int outLen) throws OrtException {
        OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(data), shape);
        try (OrtSession.Result result = session.run(Collections.singletonMap(inputName, tensor))) {
            OnnxValue value = result.get(0);
            OnnxTensor t = (OnnxTensor) value;
            FloatBuffer fb = t.getFloatBuffer();
            float[] out = new float[fb.remaining()];
            fb.get(out);
            if (out.length != outLen) {
                float[] tmp = new float[outLen];
                System.arraycopy(out, 0, tmp, 0, Math.min(out.length, outLen));
                out = tmp;
            }
            return out;
        }
    }

    private static int argmax(float[] a) {
        int best = 0;
        for (int i = 1; i < a.length; i++) {
            if (a[i] > a[best]) {
                best = i;
            }
        }
        return best;
    }

    /** 复刻 typedef.code_filter：根据车牌代码先验推断类型。 */
    private static int codeFilter(String code) {
        if (code.startsWith("WJ")) {
            return PLATE_WHITE_SINGLE;
        } else if (code.length() == 8) {
            return PLATE_GREEN;
        } else if (code.contains("学")) {
            return PLATE_BLUE;
        } else if (code.contains("港")) {
            return PLATE_BLACK_HK_MACAO;
        } else if (code.contains("澳")) {
            return PLATE_BLACK_HK_MACAO;
        } else if (code.contains("警")) {
            return PLATE_WHITE_SINGLE;
        } else if (code.contains("粤Z")) {
            return PLATE_BLACK_HK_MACAO;
        }
        return PLATE_UNKNOWN;
    }
}
