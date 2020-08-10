package com.jminton.apptracker;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

public class OuterGlowView extends GlowView {

    public OuterGlowView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.outer_glow_2);
        init();
    }
}
