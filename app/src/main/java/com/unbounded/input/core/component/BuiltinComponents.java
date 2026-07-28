package com.unbounded.input.core.component;

import com.unbounded.input.KeyboardView;
import com.unbounded.input.core.layout.KeyModel;
import java.util.List;

public class BuiltinComponents {

    public static synchronized void registerAll(ComponentRegistry registry) {
        if (registry.isInitialized()) {
            return;
        }

        registry.register(
            new ComponentDescriptor(ComponentIds.QWERTY, ComponentCategory.BASE, "QWERTY 整套"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildQwerty(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.DIGITS, ComponentCategory.DIGIT, "数字 0-9"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildDigits(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.DIRECTION, ComponentCategory.DIRECTION, "四方向键组"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildDirectionGroup(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.CLIPBOARD_ENTER, ComponentCategory.SYMBOL, "剪贴板+回车"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildClipboardEnter(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.BRACKETS, ComponentCategory.SYMBOL, "括号对"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildBrackets(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.FN_TEST, ComponentCategory.KEYBOARD, "Fn 测试键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildFnTest(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.ESC, ComponentCategory.KEYBOARD, "Esc 键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return KeyboardView.buildEsc(context);
                }
            }
        );

        registry.markInitialized();
    }
}
