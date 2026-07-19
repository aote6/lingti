import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

# 补丁1：加 import
old1 = "import android.widget.FrameLayout;"
new1 = "import android.widget.FrameLayout;\nimport com.unbounded.input.core.layout.LayoutProfile;"
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

# 补丁2：rebuildKeyboard() 接上JSON加载
old2 = """        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        // TODO: JSON 布局加载
    }"""
new2 = """        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        RuleLoader.LayoutConfig config = RuleLoader.load(this, "default.json");
        LayoutProfile profile = config.buildProfile();
        keyboardView = new KeyboardView(this, dispatcher, profile);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

# 补丁3：onCreateInputView 里的重复死dispatcher清掉，改成调用rebuildKeyboard()
old3 = """        int h = getKeyboardHeight();
        inputRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));

        KeyboardActionDispatcher dispatcher = new KeyboardActionDispatcher() {
            @Override
            public void onCommand(Command cmd) {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null) InputEngine.execute(ic, cmd);
            }
        };

        // TODO: JSON 布局加载
        return inputRoot;
    }"""
new3 = """        int h = getKeyboardHeight();
        inputRoot.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h));

        rebuildKeyboard();
        return inputRoot;
    }"""
assert text.count(old3) == 1, "补丁3锚点未找到或不唯一"
text = text.replace(old3, new3, 1)

path.write_text(text, encoding="utf-8")
print("SimpleImeService.java 打补丁完成，3处全部成功")
