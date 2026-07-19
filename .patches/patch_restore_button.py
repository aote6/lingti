import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

old = """        String layoutFileName = "default.json";
        RuleLoader.LayoutConfig config = RuleLoader.load(this, layoutFileName);
        LayoutProfile profile = config.buildProfile();
        String stateName = config.activeState != null ? config.activeState : "main";
        keyboardView = new KeyboardView(this, dispatcher, profile, layoutFileName, stateName);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
new = """        final String layoutFileName = "default.json";
        RuleLoader.LayoutConfig config = RuleLoader.load(this, layoutFileName);
        LayoutProfile profile = config.buildProfile();
        String stateName = config.activeState != null ? config.activeState : "main";

        Runnable onRestore = new Runnable() {
            @Override
            public void run() {
                java.io.File dir = getExternalFilesDir(null);
                if (dir != null) {
                    java.io.File saved = new java.io.File(dir, layoutFileName);
                    if (saved.exists()) saved.delete();
                }
                rebuildKeyboard();
            }
        };

        keyboardView = new KeyboardView(this, dispatcher, profile, layoutFileName, stateName, onRestore);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("SimpleImeService.java 打补丁完成")
