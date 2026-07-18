// 主题配置：bg/surface/text颜色值，提供Default/Amber/IBM预设
package com.unbounded.input.core.theme;

import android.graphics.Typeface;

public class ThemeProfile {
    public final int bg, surface, surfaceRaised, border, borderActive;
    public final int textPrimary, textSecondary, textAccent, pressBg;
    public final Typeface font;
    public final String name;

    public ThemeProfile(String name,
                        int bg, int surface, int surfaceRaised,
                        int border, int borderActive,
                        int textPrimary, int textSecondary, int textAccent,
                        int pressBg, Typeface font) {
        this.name = name;
        this.bg = bg; this.surface = surface; this.surfaceRaised = surfaceRaised;
        this.border = border; this.borderActive = borderActive;
        this.textPrimary = textPrimary; this.textSecondary = textSecondary;
        this.textAccent = textAccent; this.pressBg = pressBg;
        this.font = font;
    }

    public static final ThemeProfile DEFAULT = new ThemeProfile(
        "Default",
        0xFF000000, 0xFF020705, 0xFF030D08,
        0xFF081C12, 0xFF55EEAA,
        0xFF22CC88, 0xFF1C9E6A, 0xFF55EEAA,
        0xFF0A3D28, Typeface.MONOSPACE
    );

    public static final ThemeProfile AMBER = new ThemeProfile(
        "Amber",
        0xFF0A0A00, 0xFF0F0F02, 0xFF1A1A05,
        0xFF332200, 0xFFFFAA00,
        0xFFFFBB33, 0xFFCC8800, 0xFFFFCC66,
        0xFF442200, Typeface.MONOSPACE
    );

    public static final ThemeProfile IBM = new ThemeProfile(
        "IBM",
        0xFF000000, 0xFF001100, 0xFF002200,
        0xFF003300, 0xFF00FF66,
        0xFF33FF99, 0xFF009944, 0xFF66FFBB,
        0xFF004422, Typeface.MONOSPACE
    );
}
