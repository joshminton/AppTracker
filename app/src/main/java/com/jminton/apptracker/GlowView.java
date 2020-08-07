package com.jminton.apptracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class GlowView extends View {

    Bitmap inner, outer;


    public GlowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        inner = BitmapFactory.decodeResource(getResources(), R.drawable.border);
        outer = BitmapFactory.decodeResource(getResources(), R.drawable.wide_border);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Rect dstRect = new Rect(0, canvas.getHeight() / 2, canvas.getWidth(), canvas.getHeight());
        Rect srcRect = new Rect(0, inner.getHeight() / 2, inner.getWidth(), inner.getHeight());

        canvas.drawBitmap(inner, srcRect, dstRect, null);

        srcRect = new Rect(0, outer.getHeight() / 2, outer.getWidth(), outer.getHeight());
        canvas.drawBitmap(outer, srcRect, dstRect, null);

        canvas.

    }
}
