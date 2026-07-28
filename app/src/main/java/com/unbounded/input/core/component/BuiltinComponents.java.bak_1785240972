package com.unbounded.input.core.component;

import com.unbounded.input.Command;
import com.unbounded.input.core.command.KeyEventCommand;
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
                    return buildQwerty(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.DIGITS, ComponentCategory.DIGIT, "数字 0-9"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildDigits(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.DIRECTION, ComponentCategory.DIRECTION, "四方向键组"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildDirectionGroup(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.CLIPBOARD_ENTER, ComponentCategory.SYMBOL, "剪贴板+回车"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildClipboardEnter(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.BRACKETS, ComponentCategory.SYMBOL, "括号对"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildBrackets(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.FN_TEST, ComponentCategory.KEYBOARD, "Fn 测试键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildFnTest(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.ESC, ComponentCategory.KEYBOARD, "Esc 键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildEsc(context);
                }
            }
        );

        registry.markInitialized();
    }

    private static KeyModel dirKey(String id, String label, float x, float y, int keyCode) {
        KeyModel k = new KeyModel(id, label, 0, 0, 0, 0, 0, true, x, y, 12f, 9f);
        k.tap = KeyEventCommand.of(keyCode);
        return k;
    }

    private static KeyModel charKey(String id, char c, float x, float y, float w, float h) {
        KeyModel k = new KeyModel(id, String.valueOf(c), 0, 0, 0, 0, 0, true, x, y, w, h);
        k.tap = Command.insert(String.valueOf(c));
        return k;
    }

    public static List<KeyModel> buildQwerty(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        String row1 = "qwertyuiop", row2 = "asdfghjkl", row3 = "zxcvbnm";
        float w = 9.6f, h = 12f;
        float y1 = 38f, y2 = 51f, y3 = 64f, y4 = 77f;
        long stamp = ctx.stamp;
        for (int i = 0; i < row1.length(); i++)
            list.add(charKey("q1_" + i + "_" + stamp, row1.charAt(i), 2f + i * w, y1, w - 0.5f, h));
        for (int i = 0; i < row2.length(); i++)
            list.add(charKey("q2_" + i + "_" + stamp, row2.charAt(i), 2f + w / 2 + i * w, y2, w - 0.5f, h));
        for (int i = 0; i < row3.length(); i++)
            list.add(charKey("q3_" + i + "_" + stamp, row3.charAt(i), 2f + w * 1.5f + i * w, y3, w - 0.5f, h));
        KeyModel space = new KeyModel("q_space_" + stamp, "空格", 0, 0, 0, 0, 0, true, 20f, y4, 60f, h);
        space.tap = Command.space();
        list.add(space);
        KeyModel back = new KeyModel("q_back_" + stamp, "退格", 0, 0, 0, 0, 0, true, 82f, y4, 16f, h);
        back.tap = Command.backspace();
        list.add(back);
        return list;
    }

    public static List<KeyModel> buildDigits(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        String digits = "1234567890";
        float w = 9.6f, h = 12f, y = 45f;
        long stamp = ctx.stamp;
        for (int i = 0; i < digits.length(); i++)
            list.add(charKey("digit_" + i + "_" + stamp, digits.charAt(i), 2f + i * w, y, w - 0.5f, h));
        return list;
    }

    public static List<KeyModel> buildDirectionGroup(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        list.add(dirKey("dir_up_" + stamp, "\u2191", 42f, 40f, android.view.KeyEvent.KEYCODE_DPAD_UP));
        list.add(dirKey("dir_down_" + stamp, "\u2193", 42f, 58f, android.view.KeyEvent.KEYCODE_DPAD_DOWN));
        list.add(dirKey("dir_left_" + stamp, "\u2190", 30f, 49f, android.view.KeyEvent.KEYCODE_DPAD_LEFT));
        list.add(dirKey("dir_right_" + stamp, "\u2192", 54f, 49f, android.view.KeyEvent.KEYCODE_DPAD_RIGHT));
        return list;
    }

    public static List<KeyModel> buildClipboardEnter(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel pasteKey = new KeyModel("paste_" + stamp, "\u7c98\u8d34", 0, 0, 0, 0, 0, true, 30f, 45f, 16f, 10f);
        pasteKey.tap = Command.clipboardOpenPanel();
        KeyModel enterKey = new KeyModel("enter_" + stamp, "\u56de\u8f66", 0, 0, 0, 0, 0, true, 48f, 45f, 16f, 10f);
        enterKey.tap = KeyEventCommand.of(android.view.KeyEvent.KEYCODE_ENTER);
        list.add(pasteKey);
        list.add(enterKey);
        return list;
    }

    public static List<KeyModel> buildBrackets(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        list.add(charKey("br_open_" + stamp, '(', 35f, 45f, 12f, 10f));
        list.add(charKey("br_close_" + stamp, ')', 47f, 45f, 12f, 10f));
        return list;
    }

    public static List<KeyModel> buildFnTest(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel fnKey = new KeyModel("fn_" + stamp, "Fn", 0, 0, 0, 0, 0, true, 40f, 45f, 14f, 10f);
        fnKey.tap = Command.insert("[Fn]");
        list.add(fnKey);
        return list;
    }

    public static List<KeyModel> buildEsc(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel escKey = new KeyModel("esc_" + stamp, "Esc", 0, 0, 0, 0, 0, true, 40f, 45f, 14f, 10f);
        escKey.tap = KeyEventCommand.of(android.view.KeyEvent.KEYCODE_ESCAPE);
        list.add(escKey);
        return list;
    }
}
