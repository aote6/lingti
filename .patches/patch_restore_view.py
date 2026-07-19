import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardView.java")
text = path.read_text(encoding="utf-8")

old1 = """    private boolean editMode = false;
    private KeyModel dragKey = null;
    private Rect editButtonRect = new Rect();
    private Rect saveButtonRect = new Rect();
    private final String layoutFileName;
    private final String layoutStateName;"""
new1 = """    private boolean editMode = false;
    private KeyModel dragKey = null;
    private Rect editButtonRect = new Rect();
    private Rect saveButtonRect = new Rect();
    private Rect restoreButtonRect = new Rect();
    private final String layoutFileName;
    private final String layoutStateName;
    private final Runnable onRestore;"""
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

old2 = """    public KeyboardView(Context context, final KeyboardActionDispatcher dispatcher, final LayoutProfile profile,
                        String layoutFileName, String layoutStateName) {
        super(context);
        this.layoutFileName = layoutFileName;
        this.layoutStateName = layoutStateName;"""
new2 = """    public KeyboardView(Context context, final KeyboardActionDispatcher dispatcher, final LayoutProfile profile,
                        String layoutFileName, String layoutStateName, Runnable onRestore) {
        super(context);
        this.layoutFileName = layoutFileName;
        this.layoutStateName = layoutStateName;
        this.onRestore = onRestore;"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

old3 = """        int btnSize = Math.round(40 * dpScale);
        int margin = Math.round(8 * dpScale);
        editButtonRect.set(w - margin - btnSize, margin, w - margin, margin + btnSize);
        saveButtonRect.set(w - margin - btnSize * 2 - margin, margin,
                w - margin - btnSize - margin, margin + btnSize);
    }"""
new3 = """        int btnSize = Math.round(32 * dpScale);
        int margin = Math.round(6 * dpScale);
        int gap = Math.round(4 * dpScale);
        // 三个控制按钮放在屏幕最右侧、纵向排成一列（而不是横排在顶部），
        // 减少对第一排字母键的横向遮挡。
        editButtonRect.set(w - margin - btnSize, margin, w - margin, margin + btnSize);
        saveButtonRect.set(w - margin - btnSize, margin + btnSize + gap,
                w - margin, margin + btnSize * 2 + gap);
        restoreButtonRect.set(w - margin - btnSize, margin + btnSize * 2 + gap * 2,
                w - margin, margin + btnSize * 3 + gap * 2);
    }"""
assert text.count(old3) == 1, "补丁3锚点未找到或不唯一"
text = text.replace(old3, new3, 1)

old4 = """        if (editMode) {
            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(saveButtonRect, bg);
            canvas.drawRect(saveButtonRect, border);
            drawCenteredText(canvas, saveButtonRect, "保存", text);
        }
    }"""
new4 = """        if (editMode) {
            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(saveButtonRect, bg);
            canvas.drawRect(saveButtonRect, border);
            drawCenteredText(canvas, saveButtonRect, "保存", text);

            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(restoreButtonRect, bg);
            canvas.drawRect(restoreButtonRect, border);
            drawCenteredText(canvas, restoreButtonRect, "还原", text);
        }
    }"""
assert text.count(old4) == 1, "补丁4锚点未找到或不唯一"
text = text.replace(old4, new4, 1)

old5 = """            if (editMode && saveButtonRect.contains(x, y)) {
                RuleLoader.save(getContext(), layoutManager.getProfile(), layoutFileName, layoutStateName);
                Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();
                invalidate();
                return true;
            }"""
new5 = """            if (editMode && saveButtonRect.contains(x, y)) {
                RuleLoader.save(getContext(), layoutManager.getProfile(), layoutFileName, layoutStateName);
                Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();
                invalidate();
                return true;
            }
            if (editMode && restoreButtonRect.contains(x, y)) {
                if (onRestore != null) onRestore.run();
                Toast.makeText(getContext(), "已还原为出厂布局", Toast.LENGTH_SHORT).show();
                return true;
            }"""
assert text.count(old5) == 1, "补丁5锚点未找到或不唯一"
text = text.replace(old5, new5, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardView.java 打补丁完成，5处全部成功")
