// 主题取色：静态颜色常量+ThemeProfile刷新，供Renderer使用
package com.unbounded.input;

import android.graphics.Paint;
import android.graphics.Typeface;

import com.unbounded.input.core.theme.ThemeManager;
import com.unbounded.input.core.theme.ThemeProfile;

public class ThemeTokens {
    private static final ThemeManager manager = new ThemeManager();

    public static ThemeManager getManager() { return manager; }

    public static ThemeProfile current() { return manager.getTheme(); }

    public static int BG = 0xFF000000;
    public static int SURFACE = 0xFF020705;
    public static int SURFACE_RAISED = 0xFF030D08;
    public static int BORDER = 0xFF081C12;
    public static int BORDER_ACTIVE = 0xFF55EEAA;
    public static int TEXT_PRIMARY = 0xFF22CC88;
    public static int TEXT_SECONDARY = 0xFF1C9E6A;
    public static int TEXT_ACCENT = 0xFF55EEAA;
    public static int PRESS_BG = 0xFF0A3D28;
    public static Typeface FONT = Typeface.MONOSPACE;

    public static void refresh() {
        ThemeProfile t = current();
        BG = t.bg; SURFACE = t.surface; SURFACE_RAISED = t.surfaceRaised;
        BORDER = t.border; BORDER_ACTIVE = t.borderActive;
        TEXT_PRIMARY = t.textPrimary; TEXT_SECONDARY = t.textSecondary;
        TEXT_ACCENT = t.textAccent; PRESS_BG = t.pressBg; FONT = t.font;
    }

    public static Paint newBgPaint() {
        Paint p = new Paint(); p.setAntiAlias(true); return p;
    }
    public static Paint newBorderPaint() {
        Paint p = new Paint(); p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(1.5f); p.setAntiAlias(true); return p;
    }
    public static Paint newTextPaint() {
        Paint p = new Paint(); p.setTypeface(current().font); p.setAntiAlias(true); return p;
    }
}
