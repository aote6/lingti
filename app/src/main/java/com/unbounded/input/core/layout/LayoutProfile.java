// 布局配置：多行RowSpec组成，提供allKeys()扁平查询
package com.unbounded.input.core.layout;

import java.util.ArrayList;
import java.util.List;

public class LayoutProfile {
    public final String id;
    public final List<RowSpec> rows = new ArrayList<>();

    public LayoutProfile(String id) {
        this.id = id;
    }

    public LayoutProfile addRow(RowSpec row) {
        rows.add(row);
        return this;
    }

    public List<KeyModel> allKeys() {
        List<KeyModel> all = new ArrayList<>();
        for (RowSpec row : rows) {
            for (KeyModel k : row.keys) {
                if (k.enabled) all.add(k);
            }
        }
        return all;
    }

    // 删除摆放模式用：从所在行里摘掉这个键（按对象引用匹配，不是按id，
    // 允许同label/id的键并存也能精确删对）。返回是否真的找到并删除了。
    public boolean removeKey(KeyModel key) {
        for (RowSpec row : rows) {
            if (row.keys.remove(key)) return true;
        }
        return false;
    }
}
