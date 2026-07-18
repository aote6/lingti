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
}
