import pathlib
path = pathlib.Path("/data/data/com.termux/files/home/storage/shared/lingti/app/src/main/java/com/unbounded/input/KeyboardView.java")
text = path.read_text(encoding="utf-8")

# 1. 新增字段：resizeKey + 缩放热区尺寸常量
old1 = """    private boolean editMode = false;
    private KeyModel dragKey = null;
    private Rect editButtonRect = new Rect();"""
new1 = """    private boolean editMode = false;
    private KeyModel dragKey = null;
    private KeyModel resizeKey = null;
    private static final float RESIZE_HANDLE_DP = 32f;
    private Rect editButtonRect = new Rect();"""
assert text.count(old1) == 1, "锚点1未找到或不唯一"
text = text.replace(old1, new1, 1)

# 2. onDraw 里加上画缩放手柄
old2 = """        drawSlotButtons(canvas);
        drawEditControls(canvas);
        if (editMode) drawTrashZone(canvas);
        drawFlashMessage(canvas);"""
new2 = """        drawSlotButtons(canvas);
        drawEditControls(canvas);
        if (editMode) {
            drawTrashZone(canvas);
            drawResizeHandles(canvas);
        }
        drawFlashMessage(canvas);"""
assert text.count(old2) == 1, "锚点2未找到或不唯一"
text = text.replace(old2, new2, 1)

# 3. 新增 drawResizeHandles 方法（插在 drawTrashZone 之后）
old3 = """        drawCenteredText(canvas, trashZoneRect, hovering ? "松手删除" : "拖到此处删除", text);
    }

    private void drawSlotButtons(Canvas canvas) {"""
new3 = """        drawCenteredText(canvas, trashZoneRect, hovering ? "松手删除" : "拖到此处删除", text);
    }

    private void drawResizeHandles(Canvas canvas) {
        List<KeyModel> keys = layoutManager.getProfile().allKeys();
        Paint handlePaint = new Paint();
        handlePaint.setAntiAlias(true);
        handlePaint.setColor(0xAAFFFFFF);
        float handlePx = RESIZE_HANDLE_DP * dpScale;
        for (KeyModel k : keys) {
            float right = k.rect.right;
            float bottom = k.rect.bottom;
            android.graphics.Path path = new android.graphics.Path();
            path.moveTo(right, bottom - handlePx * 0.5f);
            path.lineTo(right, bottom);
            path.lineTo(right - handlePx * 0.5f, bottom);
            path.close();
            canvas.drawPath(path, handlePaint);
        }
    }

    private void drawSlotButtons(Canvas canvas) {"""
assert text.count(old3) == 1, "锚点3未找到或不唯一"
text = text.replace(old3, new3, 1)

# 4. handleEditTouch 整体重写：区分"摸中缩放热区"和"摸中键身移动"
old4 = """    private boolean handleEditTouch(MotionEvent event, int x, int y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dragKey = findTopmostKey(x, y);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragKey == null) return true;
                float touchXPercent = x * 100f / getWidth();
                float touchYPercent = y * 100f / getHeight();
                float barPercent = candidateBarHeight * 100f / getHeight();
                float newX = touchXPercent - dragKey.percentW;
                float newY = touchYPercent;
                newX = Math.max(0f, Math.min(newX, 100f - dragKey.percentW));
                newY = Math.max(barPercent, Math.min(newY, 100f - dragKey.percentH));
                dragKey.setPercentPosition(newX, newY);
                layoutManager.computeRects();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragKey != null && trashZoneRect.contains(x, y)) {
                    String deletedLabel = dragKey.label;
                    layoutManager.getProfile().removeKey(dragKey);
                    gestureController.updateKeys(layoutManager.getProfile().allKeys());
                    showFlash("已删除: " + deletedLabel);
                }
                dragKey = null;
                invalidate();
                return true;
        }
        return true;
    }"""
new4 = """    private boolean handleEditTouch(MotionEvent event, int x, int y) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                KeyModel topKey = findTopmostKey(x, y);
                if (topKey != null && isInResizeHandle(topKey, x, y)) {
                    resizeKey = topKey;
                    dragKey = null;
                } else {
                    dragKey = topKey;
                    resizeKey = null;
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE:
                if (resizeKey != null) {
                    float touchXPercent = x * 100f / getWidth();
                    float touchYPercent = y * 100f / getHeight();
                    float minWPercent = RESIZE_HANDLE_DP * dpScale * 100f / getWidth();
                    float minHPercent = RESIZE_HANDLE_DP * dpScale * 100f / getHeight();
                    float newW = touchXPercent - resizeKey.percentX;
                    float newH = touchYPercent - resizeKey.percentY;
                    newW = Math.max(minWPercent, Math.min(newW, 100f - resizeKey.percentX));
                    newH = Math.max(minHPercent, Math.min(newH, 100f - resizeKey.percentY));
                    resizeKey.percentW = newW;
                    resizeKey.percentH = newH;
                    resizeKey.hasPercentRect = true;
                    layoutManager.computeRects();
                    invalidate();
                    return true;
                }
                if (dragKey == null) return true;
                float touchXPercent2 = x * 100f / getWidth();
                float touchYPercent2 = y * 100f / getHeight();
                float barPercent = candidateBarHeight * 100f / getHeight();
                float newX = touchXPercent2 - dragKey.percentW;
                float newY = touchYPercent2;
                newX = Math.max(0f, Math.min(newX, 100f - dragKey.percentW));
                newY = Math.max(barPercent, Math.min(newY, 100f - dragKey.percentH));
                dragKey.setPercentPosition(newX, newY);
                layoutManager.computeRects();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (resizeKey != null) {
                    resizeKey = null;
                    invalidate();
                    return true;
                }
                if (dragKey != null && trashZoneRect.contains(x, y)) {
                    String deletedLabel = dragKey.label;
                    layoutManager.getProfile().removeKey(dragKey);
                    gestureController.updateKeys(layoutManager.getProfile().allKeys());
                    showFlash("已删除: " + deletedLabel);
                }
                dragKey = null;
                invalidate();
                return true;
        }
        return true;
    }"""
assert text.count(old4) == 1, "锚点4未找到或不唯一"
text = text.replace(old4, new4, 1)

# 5. 新增 isInResizeHandle 判定方法（插在 findTopmostKey 之后）
old5 = """    private KeyModel findTopmostKey(int x, int y) {
        List<KeyModel> keys = layoutManager.getProfile().allKeys();
        for (int i = keys.size() - 1; i >= 0; i--) {
            KeyModel k = keys.get(i);
            if (k.rect.contains(x, y)) return k;
        }
        return null;
    }
}"""
new5 = """    private KeyModel findTopmostKey(int x, int y) {
        List<KeyModel> keys = layoutManager.getProfile().allKeys();
        for (int i = keys.size() - 1; i >= 0; i--) {
            KeyModel k = keys.get(i);
            if (k.rect.contains(x, y)) return k;
        }
        return null;
    }

    private boolean isInResizeHandle(KeyModel key, int x, int y) {
        float handlePx = RESIZE_HANDLE_DP * dpScale;
        float left = key.rect.right - handlePx;
        float top = key.rect.bottom - handlePx;
        return x >= left && x <= key.rect.right && y >= top && y <= key.rect.bottom;
    }
}"""
assert text.count(old5) == 1, "锚点5未找到或不唯一"
text = text.replace(old5, new5, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardView.java 缩放功能打补丁完成，5处全部成功")
