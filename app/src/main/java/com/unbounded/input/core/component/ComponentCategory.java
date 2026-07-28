package com.unbounded.input.core.component;

public enum ComponentCategory {
    BASE("category_base", "字母类", 10),
    DIGIT("category_digit", "数字类", 20),
    DIRECTION("category_direction", "方向键类", 30),
    SYMBOL("category_symbol", "符号类", 40),
    KEYBOARD("category_keyboard", "真键盘类", 50);

    private final String key;
    private final String defaultLabel;
    private final int sortOrder;

    ComponentCategory(String key, String defaultLabel, int sortOrder) {
        this.key = key;
        this.defaultLabel = defaultLabel;
        this.sortOrder = sortOrder;
    }

    public String getKey() { return key; }
    public String getDefaultLabel() { return defaultLabel; }
    public int getSortOrder() { return sortOrder; }
}
