import pathlib
path = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/KeyboardView.java")
text = path.read_text(encoding="utf-8")

# 1. 新增字段：组件面板状态、按钮、模板名字
old1 = """    private Rect trashZoneRect = new Rect();
    private Rect[] slotButtonRects = new Rect[3];"""
new1 = """    private Rect trashZoneRect = new Rect();
    private Rect[] slotButtonRects = new Rect[3];
    private boolean componentPanelOpen = false;
    private Rect componentButtonRect = new Rect();
    private Rect componentPanelBg = new Rect();
    private static final String[] TEMPLATE_NAMES = {"方向键组", "剪贴板+回车"};
    private Rect[] templateItemRects = new Rect[TEMPLATE_NAMES.length];"""
assert text.count(old1) == 1, "锚点1未找到或不唯一"
text = text.replace(old1, new1, 1)

# 2. 构造函数里初始化 templateItemRects 数组
old2 = """        gestureController = new KeyboardGestureController(allKeys, dispatcher, this);
        for (int i = 0; i < slotButtonRects.length; i++) slotButtonRects[i] = new Rect();
    }"""
new2 = """        gestureController = new KeyboardGestureController(allKeys, dispatcher, this);
        for (int i = 0; i < slotButtonRects.length; i++) slotButtonRects[i] = new Rect();
        for (int i = 0; i < templateItemRects.length; i++) templateItemRects[i] = new Rect();
    }"""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

# 3. onSizeChanged：加"组件"按钮位置
old3 = """        editButtonRect.set(rightX - btnW, topY, rightX, topY + btnH);
        saveButtonRect.set(rightX - btnW * 2 - gap, topY, rightX - btnW - gap, topY + btnH);
        restoreButtonRect.set(rightX - btnW * 3 - gap * 2, topY, rightX - btnW * 2 - gap * 2, topY + btnH);"""
new3 = """        editButtonRect.set(rightX - btnW, topY, rightX, topY + btnH);
        saveButtonRect.set(rightX - btnW * 2 - gap, topY, rightX - btnW - gap, topY + btnH);
        restoreButtonRect.set(rightX - btnW * 3 - gap * 2, topY, rightX - btnW * 2 - gap * 2, topY + btnH);
        componentButtonRect.set(rightX - btnW * 4 - gap * 3, topY, rightX - btnW * 3 - gap * 3, topY + btnH);"""
assert text.count(old3) == 1, "锚点3未找到或不唯一"
text = text.replace(old3, new3, 1)

# 4. onSizeChanged：计算组件面板和条目的位置
old4 = """        trashZoneHeight = 40 * dpScale;
        trashZoneRect.set(0, h - Math.round(trashZoneHeight), w, h);
    }"""
new4 = """        trashZoneHeight = 40 * dpScale;
        trashZoneRect.set(0, h - Math.round(trashZoneHeight), w, h);

        int panelW = Math.round(180 * dpScale);
        int itemH = Math.round(40 * dpScale);
        int panelX = (w - panelW) / 2;
        int panelY = Math.round(controlBarHeight + 8 * dpScale);
        componentPanelBg.set(panelX, panelY, panelX + panelW, panelY + itemH * TEMPLATE_NAMES.length);
        for (int i = 0; i < templateItemRects.length; i++) {
            templateItemRects[i].set(panelX, panelY + i * itemH, panelX + panelW, panelY + (i + 1) * itemH);
        }
    }"""
assert text.count(old4) == 1, "锚点4未找到或不唯一"
text = text.replace(old4, new4, 1)

# 5. onDraw：面板打开时画出来
old5 = """        drawSlotButtons(canvas);
        drawEditControls(canvas);
        if (editMode) {
            drawTrashZone(canvas);
            drawResizeHandles(canvas);
        }
        drawFlashMessage(canvas);
    }"""
new5 = """        drawSlotButtons(canvas);
        drawEditControls(canvas);
        if (editMode) {
            drawTrashZone(canvas);
            drawResizeHandles(canvas);
        }
        if (componentPanelOpen) drawComponentPanel(canvas);
        drawFlashMessage(canvas);
    }"""
assert text.count(old5) == 1, "锚点5未找到或不唯一"
text = text.replace(old5, new5, 1)

# 6. drawEditControls：加"组件"按钮的绘制
old6 = """            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(restoreButtonRect, bg);
            canvas.drawRect(restoreButtonRect, border);
            drawCenteredText(canvas, restoreButtonRect, "还原", text);
        }
    }"""
new6 = """            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(restoreButtonRect, bg);
            canvas.drawRect(restoreButtonRect, border);
            drawCenteredText(canvas, restoreButtonRect, "还原", text);

            bg.setColor(ThemeTokens.SURFACE);
            canvas.drawRect(componentButtonRect, bg);
            canvas.drawRect(componentButtonRect, border);
            drawCenteredText(canvas, componentButtonRect, "组件", text);
        }
    }"""
assert text.count(old6) == 1, "锚点6未找到或不唯一"
text = text.replace(old6, new6, 1)

# 7. 新增 drawComponentPanel 方法
old7 = """    private void drawCenteredText(Canvas canvas, Rect rect, String label, Paint paint) {"""
new7 = """    private void drawComponentPanel(Canvas canvas) {
        Paint bg = ThemeTokens.newBgPaint();
        bg.setColor(ThemeTokens.SURFACE_RAISED);
        canvas.drawRect(componentPanelBg, bg);
        Paint border = ThemeTokens.newBorderPaint();
        border.setColor(ThemeTokens.BORDER);
        canvas.drawRect(componentPanelBg, border);
        Paint text = ThemeTokens.newTextPaint();
        text.setTextSize(14f);
        text.setColor(ThemeTokens.TEXT_PRIMARY);
        for (int i = 0; i < templateItemRects.length; i++) {
            canvas.drawRect(templateItemRects[i], border);
            drawCenteredText(canvas, templateItemRects[i], TEMPLATE_NAMES[i], text);
        }
    }

    private void drawCenteredText(Canvas canvas, Rect rect, String label, Paint paint) {"""
assert text.count(old7) == 1, "锚点7未找到或不唯一"
text = text.replace(old7, new7, 1)

# 8. onTouchEvent：加面板的触摸处理 + 组件按钮点击入口
old8 = """        int x = (int) event.getX(), y = (int) event.getY();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < slotButtonRects.length; i++) {"""
new8 = """        int x = (int) event.getX(), y = (int) event.getY();

        if (componentPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                for (int i = 0; i < templateItemRects.length; i++) {
                    if (templateItemRects[i].contains(x, y)) {
                        instantiateTemplate(i);
                        break;
                    }
                }
                componentPanelOpen = false;
                invalidate();
            }
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (editMode && componentButtonRect.contains(x, y)) {
                componentPanelOpen = true;
                invalidate();
                return true;
            }
            for (int i = 0; i < slotButtonRects.length; i++) {"""
assert text.count(old8) == 1, "锚点8未找到或不唯一"
text = text.replace(old8, new8, 1)

# 9. 新增 instantiateTemplate + dirKey 方法
old9 = """    private boolean isInResizeHandle(KeyModel key, int x, int y) {
        float handlePx = RESIZE_HANDLE_DP * dpScale;
        float left = key.rect.right - handlePx;
        float top = key.rect.bottom - handlePx;
        return x >= left && x <= key.rect.right && y >= top && y <= key.rect.bottom;
    }
}"""
new9 = """    private boolean isInResizeHandle(KeyModel key, int x, int y) {
        float handlePx = RESIZE_HANDLE_DP * dpScale;
        float left = key.rect.right - handlePx;
        float top = key.rect.bottom - handlePx;
        return x >= left && x <= key.rect.right && y >= top && y <= key.rect.bottom;
    }

    private void instantiateTemplate(int idx) {
        LayoutProfile profile = layoutManager.getProfile();
        com.unbounded.input.core.layout.RowSpec newRow = new com.unbounded.input.core.layout.RowSpec();
        long stamp = System.currentTimeMillis();
        if (idx == 0) {
            newRow.add(dirKey("dir_up_" + stamp, "↑", 42f, 40f, android.view.KeyEvent.KEYCODE_DPAD_UP));
            newRow.add(dirKey("dir_down_" + stamp, "↓", 42f, 58f, android.view.KeyEvent.KEYCODE_DPAD_DOWN));
            newRow.add(dirKey("dir_left_" + stamp, "←", 30f, 49f, android.view.KeyEvent.KEYCODE_DPAD_LEFT));
            newRow.add(dirKey("dir_right_" + stamp, "→", 54f, 49f, android.view.KeyEvent.KEYCODE_DPAD_RIGHT));
        } else {
            KeyModel pasteKey = new KeyModel("paste_" + stamp, "粘贴", 0, 0, 0, 0, 0,
                    true, 30f, 45f, 16f, 10f);
            pasteKey.tap = Command.clipboardOpenPanel();
            KeyModel enterKey = new KeyModel("enter_" + stamp, "回车", 0, 0, 0, 0, 0,
                    true, 48f, 45f, 16f, 10f);
            enterKey.tap = com.unbounded.input.core.command.KeyEventCommand.of(android.view.KeyEvent.KEYCODE_ENTER);
            newRow.add(pasteKey);
            newRow.add(enterKey);
        }
        profile.addRow(newRow);
        gestureController.updateKeys(profile.allKeys());
        layoutManager.computeRects();
        showFlash(idx == 0 ? "已添加方向键组，可拖拽调整位置" : "已添加剪贴板+回车组合，可拖拽调整位置");
        invalidate();
    }

    private KeyModel dirKey(String id, String label, float x, float y, int keyCode) {
        KeyModel k = new KeyModel(id, label, 0, 0, 0, 0, 0, true, x, y, 12f, 9f);
        k.tap = com.unbounded.input.core.command.KeyEventCommand.of(keyCode);
        return k;
    }
}"""
assert text.count(old9) == 1, "锚点9未找到或不唯一"
text = text.replace(old9, new9, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardView.java 组件库功能打补丁完成，9处全部成功")
