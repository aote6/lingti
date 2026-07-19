import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

old1 = """    private int getKeyboardHeight() {"""
new1 = """    private int getActiveSlot() {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        return prefs.getInt("active_slot", 1);
    }

    private void setActiveSlot(int slot) {
        if (prefs == null) prefs = getSharedPreferences("lingti_prefs", MODE_PRIVATE);
        prefs.edit().putInt("active_slot", slot).apply();
    }

    private int getKeyboardHeight() {"""
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

old2 = """        final String layoutFileName = "default.json";
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
                RuleLoader.clearCache(layoutFileName);
                rebuildKeyboard();
            }
        };

        keyboardView = new KeyboardView(this, dispatcher, profile, layoutFileName, stateName, onRestore);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
new2 = """        final int activeSlot = getActiveSlot();
        // 每个槽位对应独立文件；保存永远写这个文件，加载优先读这个文件，
        // 该槽位还没存过东西时才回退到内置的 default.json（出厂样式）。
        final String slotFileName = "layout_slot" + activeSlot + ".json";
        java.io.File externalDir = getExternalFilesDir(null);
        String loadFileName = slotFileName;
        if (externalDir == null || !new java.io.File(externalDir, slotFileName).exists()) {
            loadFileName = "default.json";
        }

        RuleLoader.LayoutConfig config = RuleLoader.load(this, loadFileName);
        LayoutProfile profile = config.buildProfile();
        String stateName = config.activeState != null ? config.activeState : "main";

        Runnable onRestore = new Runnable() {
            @Override
            public void run() {
                java.io.File dir = getExternalFilesDir(null);
                if (dir != null) {
                    java.io.File saved = new java.io.File(dir, slotFileName);
                    if (saved.exists()) saved.delete();
                }
                RuleLoader.clearCache(slotFileName);
                rebuildKeyboard();
            }
        };

        KeyboardView.SlotSwitchListener onSlotSwitch = new KeyboardView.SlotSwitchListener() {
            @Override
            public void onSwitchSlot(int slot) {
                setActiveSlot(slot);
                rebuildKeyboard();
            }
        };

        keyboardView = new KeyboardView(this, dispatcher, profile, slotFileName, stateName,
                onRestore, activeSlot, onSlotSwitch);
        inputRoot.addView(keyboardView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, h));
    }"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

path.write_text(text, encoding="utf-8")
print("SimpleImeService.java 打补丁完成，2处全部成功")
