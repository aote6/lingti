// 行描述：存放一行内的KeyModel列表，计算totalSpan
package com.unbounded.input.core.layout;

import java.util.ArrayList;
import java.util.List;

public class RowSpec {
    public final List<KeyModel> keys = new ArrayList<>();

    public RowSpec add(KeyModel key) {
        keys.add(key);
        return this;
    }

    public float totalSpan() {
        float sum = 0;
        for (KeyModel k : keys) sum += k.span;
        return sum;
    }

    // 整行是否为纯百分比定位（第一个键定基调）
    public boolean isPercentRow() {
        if (keys.isEmpty()) return false;
        return keys.get(0).hasPercentRect;
    }

    // 一行内同时存在百分比键和span键，视为配置错误
    public boolean isMixedRow() {
        boolean sawPercent = false, sawSpan = false;
        for (KeyModel k : keys) {
            if (k.hasPercentRect) sawPercent = true; else sawSpan = true;
        }
        return sawPercent && sawSpan;
    }
}
