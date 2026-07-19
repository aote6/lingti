import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

old = """        RuleLoader.LayoutConfig config = RuleLoader.load(this, "default.json");
        LayoutProfile profile = config.buildProfile();
        keyboardView = new KeyboardView(this, dispatcher, profile);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
new = """        String layoutFileName = "default.json";
        RuleLoader.LayoutConfig config = RuleLoader.load(this, layoutFileName);
        LayoutProfile profile = config.buildProfile();
        String stateName = config.activeState != null ? config.activeState : "main";
        keyboardView = new KeyboardView(this, dispatcher, profile, layoutFileName, stateName);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("SimpleImeService.java 打补丁完成")
