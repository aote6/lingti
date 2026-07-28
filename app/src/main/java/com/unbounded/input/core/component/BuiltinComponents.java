package com.unbounded.input.core.component;

import com.unbounded.input.Command;
import com.unbounded.input.core.command.KeyEventCommand;
import com.unbounded.input.core.command.KeyChordCommand;
import com.unbounded.input.core.layout.KeyModel;
import java.util.List;

public class BuiltinComponents {

    public static synchronized void registerAll(ComponentRegistry registry) {
        if (registry.isInitialized()) {
            return;
        }

        // ========== BASE 字母类 ==========

        registry.register(
            new ComponentDescriptor(ComponentIds.QWERTY, ComponentCategory.BASE, "QWERTY 整套"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildQwerty(context);
                }
            }
        );

        // ========== DIGIT 数字类 ==========

        registry.register(
            new ComponentDescriptor(ComponentIds.DIGITS, ComponentCategory.DIGIT, "数字 0-9"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildDigits(context);
                }
            }
        );

        // ========== DIRECTION 方向键类 ==========

        registry.register(
            new ComponentDescriptor(ComponentIds.DIRECTION, ComponentCategory.DIRECTION, "四方向键组"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildDirectionGroup(context);
                }
            }
        );

        // ========== SYMBOL 符号类 ==========

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
            new ComponentDescriptor(ComponentIds.BRACKETS_FULL, ComponentCategory.SYMBOL, "括号全套"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildBracketsFull(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.PUNCTUATION, ComponentCategory.SYMBOL, "标点符号"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildPunctuation(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.OPERATORS, ComponentCategory.SYMBOL, "运算符"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildOperators(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.SPECIAL_SYMBOLS, ComponentCategory.SYMBOL, "特殊符号"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildSpecialSymbols(context);
                }
            }
        );

        // ========== KEYBOARD 真键盘类 ==========

        registry.register(
            new ComponentDescriptor(ComponentIds.CLIPBOARD_ENTER, ComponentCategory.KEYBOARD, "剪贴板+回车"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildClipboardEnter(context);
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

        registry.register(
            new ComponentDescriptor(ComponentIds.TAB, ComponentCategory.KEYBOARD, "Tab 键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildTab(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.DELETE_KEY, ComponentCategory.KEYBOARD, "Delete 键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildDeleteKey(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.INSERT, ComponentCategory.KEYBOARD, "Insert 键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildInsert(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.HOME_END, ComponentCategory.KEYBOARD, "Home / End"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildHomeEnd(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.PAGE_UP_DOWN, ComponentCategory.KEYBOARD, "PageUp / PageDown"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildPageUpDown(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.CTRL_BASIC, ComponentCategory.KEYBOARD, "Ctrl 常用四键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildCtrlBasic(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.CTRL_EDIT, ComponentCategory.KEYBOARD, "Ctrl 编辑四键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildCtrlEdit(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.CTRL_EXTRA, ComponentCategory.KEYBOARD, "Ctrl 扩展三键"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildCtrlExtra(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.EDIT_SELECT_ALL, ComponentCategory.KEYBOARD, "全选"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildSelectAll(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.EDIT_COPY, ComponentCategory.KEYBOARD, "复制"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildCopy(context);
                }
            }
        );

        registry.register(
            new ComponentDescriptor(ComponentIds.EDIT_CUT, ComponentCategory.KEYBOARD, "剪切"),
            new ComponentFactory() {
                @Override
                public List<KeyModel> instantiate(ComponentContext context) {
                    return buildCut(context);
                }
            }
        );

        registry.markInitialized();
    }

    // ==================== 辅助工厂方法 ====================

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

    private static KeyModel ctrlKey(String id, String label, float x, float y, float w, float h, int keyCode) {
        KeyModel k = new KeyModel(id, label, 0, 0, 0, 0, 0, true, x, y, w, h);
        k.tap = new KeyChordCommand(new int[]{keyCode}, android.view.KeyEvent.META_CTRL_ON);
        return k;
    }

    private static KeyModel keyCodeKey(String id, String label, float x, float y, float w, float h, int keyCode) {
        KeyModel k = new KeyModel(id, label, 0, 0, 0, 0, 0, true, x, y, w, h);
        k.tap = KeyEventCommand.of(keyCode);
        return k;
    }

    // ==================== 各组件构建方法 ====================

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
        list.add(dirKey("dir_up_" + stamp, "↑", 42f, 40f, android.view.KeyEvent.KEYCODE_DPAD_UP));
        list.add(dirKey("dir_down_" + stamp, "↓", 42f, 58f, android.view.KeyEvent.KEYCODE_DPAD_DOWN));
        list.add(dirKey("dir_left_" + stamp, "←", 30f, 49f, android.view.KeyEvent.KEYCODE_DPAD_LEFT));
        list.add(dirKey("dir_right_" + stamp, "→", 54f, 49f, android.view.KeyEvent.KEYCODE_DPAD_RIGHT));
        return list;
    }

    public static List<KeyModel> buildBrackets(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        list.add(charKey("br_open_" + stamp, '(', 35f, 45f, 12f, 10f));
        list.add(charKey("br_close_" + stamp, ')', 47f, 45f, 12f, 10f));
        return list;
    }

    public static List<KeyModel> buildBracketsFull(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float y1 = 35f, y2 = 48f;
        float w = 14f, h = 10f;
        list.add(charKey("bf_lparen_" + stamp, '(', 5f, y1, w, h));
        list.add(charKey("bf_rparen_" + stamp, ')', 20f, y1, w, h));
        list.add(charKey("bf_lbrack_" + stamp, '[', 38f, y1, w, h));
        list.add(charKey("bf_rbrack_" + stamp, ']', 53f, y1, w, h));
        list.add(charKey("bf_lbrace_" + stamp, '{', 71f, y1, w, h));
        list.add(charKey("bf_rbrace_" + stamp, '}', 86f, y1, w, h));
        list.add(charKey("bf_langle_" + stamp, '<', 5f, y2, w, h));
        list.add(charKey("bf_rangle_" + stamp, '>', 20f, y2, w, h));
        return list;
    }

    public static List<KeyModel> buildPunctuation(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float y1 = 35f, y2 = 50f;
        float w = 14f, h = 12f;
        list.add(charKey("p_comma_" + stamp, ',', 5f, y1, w, h));
        list.add(charKey("p_period_" + stamp, '.', 22f, y1, w, h));
        list.add(charKey("p_semicolon_" + stamp, ';', 39f, y1, w, h));
        list.add(charKey("p_colon_" + stamp, ':', 56f, y1, w, h));
        list.add(charKey("p_singleq_" + stamp, '\'', 73f, y1, w, h));
        list.add(charKey("p_doubleq_" + stamp, '"', 86f, y1, w, h));
        list.add(charKey("p_exclaim_" + stamp, '!', 5f, y2, w, h));
        list.add(charKey("p_question_" + stamp, '?', 22f, y2, w, h));
        return list;
    }

    public static List<KeyModel> buildOperators(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float y1 = 35f, y2 = 50f;
        float w = 14f, h = 12f;
        list.add(charKey("op_plus_" + stamp, '+', 5f, y1, w, h));
        list.add(charKey("op_minus_" + stamp, '-', 22f, y1, w, h));
        list.add(charKey("op_mul_" + stamp, '*', 39f, y1, w, h));
        list.add(charKey("op_div_" + stamp, '/', 56f, y1, w, h));
        list.add(charKey("op_eq_" + stamp, '=', 73f, y1, w, h));
        list.add(charKey("op_mod_" + stamp, '%', 86f, y1, w, h));
        list.add(charKey("op_xor_" + stamp, '^', 5f, y2, w, h));
        list.add(charKey("op_and_" + stamp, '&', 22f, y2, w, h));
        list.add(charKey("op_tilde_" + stamp, '~', 39f, y2, w, h));
        return list;
    }

    public static List<KeyModel> buildSpecialSymbols(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float y1 = 35f, y2 = 50f;
        float w = 14f, h = 12f;
        list.add(charKey("ss_backtick_" + stamp, '`', 5f, y1, w, h));
        list.add(charKey("ss_at_" + stamp, '@', 22f, y1, w, h));
        list.add(charKey("ss_hash_" + stamp, '#', 39f, y1, w, h));
        list.add(charKey("ss_dollar_" + stamp, '$', 56f, y1, w, h));
        list.add(charKey("ss_under_" + stamp, '_', 73f, y1, w, h));
        list.add(charKey("ss_bslash_" + stamp, '\\', 86f, y1, w, h));
        list.add(charKey("ss_pipe_" + stamp, '|', 5f, y2, w, h));
        return list;
    }

    public static List<KeyModel> buildClipboardEnter(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel pasteKey = new KeyModel("paste_" + stamp, "粘贴", 0, 0, 0, 0, 0, true, 30f, 45f, 16f, 10f);
        pasteKey.tap = Command.clipboardOpenPanel();
        KeyModel enterKey = new KeyModel("enter_" + stamp, "回车", 0, 0, 0, 0, 0, true, 48f, 45f, 16f, 10f);
        enterKey.tap = KeyEventCommand.of(android.view.KeyEvent.KEYCODE_ENTER);
        list.add(pasteKey);
        list.add(enterKey);
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

    public static List<KeyModel> buildTab(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel tabKey = new KeyModel("tab_" + stamp, "Tab", 0, 0, 0, 0, 0, true, 40f, 45f, 14f, 10f);
        tabKey.tap = KeyEventCommand.of(android.view.KeyEvent.KEYCODE_TAB);
        list.add(tabKey);
        return list;
    }

    public static List<KeyModel> buildDeleteKey(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel delKey = new KeyModel("del_" + stamp, "Del", 0, 0, 0, 0, 0, true, 40f, 45f, 14f, 10f);
        delKey.tap = KeyEventCommand.of(android.view.KeyEvent.KEYCODE_FORWARD_DEL);
        list.add(delKey);
        return list;
    }

    public static List<KeyModel> buildInsert(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel insKey = new KeyModel("ins_" + stamp, "Ins", 0, 0, 0, 0, 0, true, 40f, 45f, 14f, 10f);
        insKey.tap = KeyEventCommand.of(android.view.KeyEvent.KEYCODE_INSERT);
        list.add(insKey);
        return list;
    }

    public static List<KeyModel> buildHomeEnd(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        list.add(keyCodeKey("home_" + stamp, "Home", 30f, 45f, 18f, 10f, android.view.KeyEvent.KEYCODE_MOVE_HOME));
        list.add(keyCodeKey("end_" + stamp, "End", 52f, 45f, 18f, 10f, android.view.KeyEvent.KEYCODE_MOVE_END));
        return list;
    }

    public static List<KeyModel> buildPageUpDown(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        list.add(keyCodeKey("pgup_" + stamp, "PgUp", 25f, 45f, 22f, 10f, android.view.KeyEvent.KEYCODE_PAGE_UP));
        list.add(keyCodeKey("pgdn_" + stamp, "PgDn", 53f, 45f, 22f, 10f, android.view.KeyEvent.KEYCODE_PAGE_DOWN));
        return list;
    }

    public static List<KeyModel> buildCtrlBasic(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float w = 20f, h = 12f, y = 45f;
        list.add(ctrlKey("ctrlc_" + stamp, "^C", 3f, y, w, h, android.view.KeyEvent.KEYCODE_C));
        list.add(ctrlKey("ctrld_" + stamp, "^D", 27f, y, w, h, android.view.KeyEvent.KEYCODE_D));
        list.add(ctrlKey("ctrll_" + stamp, "^L", 51f, y, w, h, android.view.KeyEvent.KEYCODE_L));
        list.add(ctrlKey("ctrlr_" + stamp, "^R", 75f, y, w, h, android.view.KeyEvent.KEYCODE_R));
        return list;
    }

    public static List<KeyModel> buildCtrlEdit(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float w = 20f, h = 12f, y = 45f;
        list.add(ctrlKey("ctrla_" + stamp, "^A", 3f, y, w, h, android.view.KeyEvent.KEYCODE_A));
        list.add(ctrlKey("ctrle_" + stamp, "^E", 27f, y, w, h, android.view.KeyEvent.KEYCODE_E));
        list.add(ctrlKey("ctrlu_" + stamp, "^U", 51f, y, w, h, android.view.KeyEvent.KEYCODE_U));
        list.add(ctrlKey("ctrlk_" + stamp, "^K", 75f, y, w, h, android.view.KeyEvent.KEYCODE_K));
        return list;
    }

    public static List<KeyModel> buildCtrlExtra(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        float w = 20f, h = 12f, y = 45f;
        list.add(ctrlKey("ctrlz_" + stamp, "^Z", 20f, y, w, h, android.view.KeyEvent.KEYCODE_Z));
        list.add(ctrlKey("ctrlw_" + stamp, "^W", 48f, y, w, h, android.view.KeyEvent.KEYCODE_W));
        list.add(ctrlKey("ctrly_" + stamp, "^Y", 72f, y, w, h, android.view.KeyEvent.KEYCODE_Y));
        return list;
    }

    public static List<KeyModel> buildSelectAll(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel key = new KeyModel("selall_" + stamp, "全选", 0, 0, 0, 0, 0, true, 40f, 45f, 18f, 10f);
        key.tap = new KeyChordCommand(new int[]{android.view.KeyEvent.KEYCODE_A}, android.view.KeyEvent.META_CTRL_ON);
        list.add(key);
        return list;
    }

    public static List<KeyModel> buildCopy(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel key = new KeyModel("copy_" + stamp, "复制", 0, 0, 0, 0, 0, true, 40f, 45f, 18f, 10f);
        key.tap = new KeyChordCommand(new int[]{android.view.KeyEvent.KEYCODE_C}, android.view.KeyEvent.META_CTRL_ON);
        list.add(key);
        return list;
    }

    public static List<KeyModel> buildCut(ComponentContext ctx) {
        List<KeyModel> list = new java.util.ArrayList<KeyModel>();
        long stamp = ctx.stamp;
        KeyModel key = new KeyModel("cut_" + stamp, "剪切", 0, 0, 0, 0, 0, true, 40f, 45f, 18f, 10f);
        key.tap = new KeyChordCommand(new int[]{android.view.KeyEvent.KEYCODE_X}, android.view.KeyEvent.META_CTRL_ON);
        list.add(key);
        return list;
    }
}
