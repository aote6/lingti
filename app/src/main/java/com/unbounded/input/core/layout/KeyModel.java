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

    public KeyModel(String id, String label, float span) {
        this(id, label, span, 0, 0, 0, 0);
    }

    public KeyModel(String id, String label, float span,
                    float padLeft, float padTop, float padRight, float padBottom) {
        this.id = id;
        this.label = label;
        this.span = span;
        this.padLeft = padLeft;
        this.padTop = padTop;
        this.padRight = padRight;
        this.padBottom = padBottom;
        this.rect = new Rect();
        this.enabled = true;
    }
}
