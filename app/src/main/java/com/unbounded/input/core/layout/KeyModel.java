// 按键模型：label/span/rect + 6个Command（tap/四向swipe/longPress）
package com.unbounded.input.core.layout;

import android.graphics.Rect;

import com.unbounded.input.Command;

public class KeyModel {
    public final String id;
    public final String label;
    public final float span;
    public final float padLeft, padTop, padRight, padBottom;
    public final Rect rect;
    public Command tap, swipeUp, swipeDown, swipeLeft, swipeRight, longPress;
    public boolean pressed;
    public boolean enabled;

    // 百分比坐标（0-100）。非final：拖拽摆放模式需要实时改写这几个字段。
    public boolean hasPercentRect;
    public float percentX, percentY, percentW, percentH;

    public KeyModel(String id, String label, float span) {
        this(id, label, span, 0, 0, 0, 0);
    }

    public KeyModel(String id, String label, float span,
                    float padLeft, float padTop, float padRight, float padBottom) {
        this(id, label, span, padLeft, padTop, padRight, padBottom, false, 0, 0, 0, 0);
    }

    public KeyModel(String id, String label, float span,
                    float padLeft, float padTop, float padRight, float padBottom,
                    boolean hasPercentRect, float percentX, float percentY, float percentW, float percentH) {
        this.id = id;
        this.label = label;
        this.span = span;
        this.padLeft = padLeft;
        this.padTop = padTop;
        this.padRight = padRight;
        this.padBottom = padBottom;
        this.rect = new Rect();
        this.enabled = true;
        this.hasPercentRect = hasPercentRect;
        this.percentX = percentX;
        this.percentY = percentY;
        this.percentW = percentW;
        this.percentH = percentH;
    }

    // 拖拽摆放模式用：整体设定百分比矩形（首次从span布局转换为百分比时用）
    public void setPercentRect(float x, float y, float w, float h) {
        this.percentX = x;
        this.percentY = y;
        this.percentW = w;
        this.percentH = h;
        this.hasPercentRect = true;
    }

    // 拖拽摆放模式用：只改位置，不改大小（拖动过程中反复调用）
    public void setPercentPosition(float x, float y) {
        this.percentX = x;
        this.percentY = y;
        this.hasPercentRect = true;
    }
}
