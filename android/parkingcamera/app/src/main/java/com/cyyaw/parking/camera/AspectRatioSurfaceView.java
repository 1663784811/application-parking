package com.cyyaw.parking.camera;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;


public class AspectRatioSurfaceView extends SurfaceView {

    private int ratioWidth = 0;
    private int ratioHeight = 0;

    public AspectRatioSurfaceView(Context context) {
        this(context, null);
    }

    public AspectRatioSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AspectRatioSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            return;
        }
        if (ratioWidth == width && ratioHeight == height) {
            return;
        }
        ratioWidth = width;
        ratioHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentW = MeasureSpec.getSize(widthMeasureSpec);
        int parentH = MeasureSpec.getSize(heightMeasureSpec);
        int w = parentW;
        int h = parentH;
        if (ratioWidth > 0 && ratioHeight > 0) {
            float ratio = (float) ratioWidth / ratioHeight;
            float parentRatio = (float) parentW / parentH;
            if (parentRatio > ratio) {
                // Parent is wider: fit to height, letterbox left/right.
                h = parentH;
                w = Math.round(parentH * ratio);
            } else {
                // Parent is taller: fit to width, letterbox top/bottom.
                w = parentW;
                h = Math.round(parentW / ratio);
            }
        }
        setMeasuredDimension(w, h);
    }
}
