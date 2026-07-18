// 布局接口：id()和build()，RuleLoader和Java布局实现此接口
package com.unbounded.input.core.layout;

public interface KeyboardLayout {
    String id();
    LayoutProfile build();
}
