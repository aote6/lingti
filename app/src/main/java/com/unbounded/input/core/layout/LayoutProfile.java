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
}
