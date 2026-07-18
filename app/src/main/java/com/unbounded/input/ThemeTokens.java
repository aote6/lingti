package com.unbounded.input;

import android.graphics.Paint;
import android.graphics.Typeface;

public class ThemeTokens {
    // 背景层级
    public static final int BG = 0xFF000000;
    public static final int SURFACE = 0xFF020705;
    public static final int SURFACE_RAISED = 0xFF030D08;

    // 边框
    public static final int BORDER = 0xFF081C12;
    public static final int BORDER_ACTIVE = 0xFF55EEAA;

    // 文本
    public static final int TEXT_PRIMARY = 0xFF22CC88;
    public static final int TEXT_SECONDARY = 0xFF1C9E6A;
    public static final int TEXT_ACCENT = 0xFF55EEAA;

    // 按压
    public static final int PRESS_BG = 0xFF0A3D28;

    // 字体
    public static final Typeface FONT = Typeface.MONOSPACE;

    public static Paint newBgPaint() {
        Paint p = new Paint();
        p.setAntiAlias(true);
        return p;
    }

    public static Paint newBorderPaint() {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f);
        p.setAntiAlias(true);
        return p;
    }

    public static Paint newTextPaint() {
        Paint p = new Paint();
        p.setTypeface(FONT);
        p.setAntiAlias(true);
        return p;
    }
}
