package com.jminton.apptracker;

//https://gist.github.com/arriolac/3843346

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Created by chris on 7/27/16.
 */
public class BottomCropImage extends androidx.appcompat.widget.AppCompatImageView {

    public BottomCropImage(Context context) {
        super(context);
        init();
    }

    public BottomCropImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomCropImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    public BottomCropImage(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        init();
//    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        recomputeImgMatrix();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        recomputeImgMatrix();
        return super.setFrame(l, t, r, b);
    }

    private void init() {
        setScaleType(ScaleType.FIT_END);
    }

    private void recomputeImgMatrix() {

        final Drawable drawable = getDrawable();
        if (drawable == null) {
            return;
        }

        final Matrix matrix = getImageMatrix();

        float scale;
        final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            scale = (float) viewHeight / (float) drawableHeight;
        } else {
            scale = (float) viewWidth / (float) drawableWidth;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate( 0, viewHeight - drawableHeight * scale);
        setImageMatrix(matrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        recomputeImgMatrix();
        super.onDraw(canvas);
    }
}