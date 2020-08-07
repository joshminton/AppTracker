package com.jminton.apptracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class GlowView extends View {

    Bitmap bitmap;
    int width, height;
    float percentage;
    int bitHeight, bitWidth;


    public GlowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

    }

    protected void init(){
        bitHeight = bitmap.getHeight();
        bitWidth = bitmap.getWidth();
        percentage = 0.0f;
    }

    @Override
    public void onSizeChanged (int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect srcRect = new Rect(0, bitHeight - (int) (bitHeight * percentage), bitWidth, bitHeight);
        Rect dstRect = new Rect(0, 0, width, height);

        canvas.drawBitmap(bitmap, srcRect, dstRect, null);

//        Log.d("Canvas height", height + " ");

    }

    public void setHeight(float percentage, int height){
        this.percentage = percentage;
        this.getLayoutParams().height = (int) (height * percentage);
        this.setLayoutParams(this.getLayoutParams());
    }
}
