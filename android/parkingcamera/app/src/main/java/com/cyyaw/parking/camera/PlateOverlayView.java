package com.cyyaw.parking.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 覆盖在相机预览上的车牌检测框 Overlay。
 *
 * <p>识别结果坐标在"帧坐标系"（ImageReader 原始帧，尺寸 frameW x frameH）下；
 * 宿主在收到结果时把该帧到本 View 坐标系的映射参数（偏移 + 缩放）一起传入，
 * onDraw 时再把 4 个角点变换到屏幕坐标画框。</p>
 */
public class PlateOverlayView extends View {

    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private List<LprRecognizer.PlateResult> results = new ArrayList<>();
    private float offsetX = 0, offsetY = 0, scaleX = 1, scaleY = 1;

    public PlateOverlayView(Context context) {
        this(context, null);
    }

    public PlateOverlayView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlateOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(6f);
        boxPaint.setColor(Color.GREEN);
        textPaint.setColor(Color.GREEN);
        textPaint.setTextSize(30f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    /** 更新识别结果与帧->屏幕映射参数。 */
    public void setResults(List<LprRecognizer.PlateResult> list,
                           float offsetX, float offsetY, float scaleX, float scaleY) {
        this.results = list != null ? list : new ArrayList<>();
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        postInvalidate();
    }

    public void clear() {
        results.clear();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 帧坐标系与 SurfaceView 显示的原始 buffer 同向，无需旋转；只做偏移+缩放
        for (LprRecognizer.PlateResult r : results) {
            if (r.vertex == null || r.vertex.length < 4) {
                continue;
            }
            float[] vx = new float[4];
            float[] vy = new float[4];
            for (int i = 0; i < 4; i++) {
                vx[i] = offsetX + r.vertex[i][0] * scaleX;
                vy[i] = offsetY + r.vertex[i][1] * scaleY;
            }
            path.reset();
            path.moveTo(vx[0], vy[0]);
            path.lineTo(vx[1], vy[1]);
            path.lineTo(vx[2], vy[2]);
            path.lineTo(vx[3], vy[3]);
            path.close();
            canvas.drawPath(path, boxPaint);

            String label = String.format(Locale.getDefault(), "%s  %.2f", r.plate, r.confidence);
            canvas.drawText(label, vx[0], Math.max(vy[0] - 8f, 20f), textPaint);
        }
    }
}
