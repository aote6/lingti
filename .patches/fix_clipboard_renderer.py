import pathlib
path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardRenderer.java")
text = path.read_text(encoding="utf-8")

old = '''    public void drawClipboardPopup(Canvas canvas, java.util.List<String> history) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        Paint overlay = new Paint();
        overlay.setColor(0xCC000000);
        canvas.drawRect(0, 0, w, h, overlay);

        if (history == null || history.isEmpty()) {
            Paint emptyPaint = ThemeTokens.newTextPaint();
            emptyPaint.setTextSize(20f);
            emptyPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            canvas.drawText("剪贴板为空", w / 2f - 60, h / 2f, emptyPaint);
            return;
        }

        int visibleCount = Math.min(history.size(), MAX_VISIBLE_CLIPBOARD_ITEMS);
        float itemHeight = h / (float) visibleCount;
        Paint bgPaint = ThemeTokens.newBgPaint();
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setTextSize(18f);
        Paint borderPaint = ThemeTokens.newBorderPaint();
        borderPaint.setColor(ThemeTokens.BORDER);

        for (int i = 0; i < visibleCount; i++) {
            String raw = history.get(history.size() - 1 - i);
            float top = i * itemHeight;
            float bottom = top + itemHeight;

            bgPaint.setColor(ThemeTokens.SURFACE_RAISED);
            canvas.drawRect(0, top, w, bottom, bgPaint);
            canvas.drawLine(0, bottom, w, bottom, borderPaint);

            String display = raw.replace("\\n", " ");
            if (display.length() > 40) display = display.substring(0, 40) + "...";
            textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = top + itemHeight / 2 - (fm.ascent + fm.descent) / 2;
            canvas.drawText(display, 16, textY, textPaint);
        }
    }

    public int hitTestClipboardItem(int viewHeight, int totalCount, float y) {
        if (totalCount == 0) return -1;
        int visibleCount = Math.min(totalCount, MAX_VISIBLE_CLIPBOARD_ITEMS);
        float itemHeight = viewHeight / (float) visibleCount;
        int idx = (int) (y / itemHeight);
        if (idx < 0 || idx >= visibleCount) return -1;
        return idx;
    }
}'''

new = '''    public static class ClipboardHit {
        public static final int NONE = 0, EXIT = 1, PASTE = 2, PIN = 3, DELETE = 4;
        public int action = NONE;
        public int index = -1;
    }

    private final Rect clipExitRect = new Rect();
    private final java.util.List<Rect> clipTextRects = new java.util.ArrayList<>();
    private final java.util.List<Rect> clipPinRects = new java.util.ArrayList<>();
    private final java.util.List<Rect> clipDeleteRects = new java.util.ArrayList<>();
    private final java.util.List<Integer> clipRowRealIndex = new java.util.ArrayList<>();

    public void drawClipboardPopup(Canvas canvas, java.util.List<SimpleImeService.ClipboardEntry> history) {
        int w = canvas.getWidth();
        int h = canvas.getHeight();

        clipTextRects.clear();
        clipPinRects.clear();
        clipDeleteRects.clear();
        clipRowRealIndex.clear();

        Paint overlay = new Paint();
        overlay.setColor(0xCC000000);
        canvas.drawRect(0, 0, w, h, overlay);

        int topBarHeight = 90;
        int exitW = 110, exitPad = 10;
        clipExitRect.set(w - exitW - exitPad, exitPad, w - exitPad, topBarHeight - exitPad);

        Paint barBg = ThemeTokens.newBgPaint();
        barBg.setColor(ThemeTokens.SURFACE_RAISED);
        canvas.drawRect(0, 0, w, topBarHeight, barBg);
        Paint exitBorder = ThemeTokens.newBorderPaint();
        exitBorder.setColor(ThemeTokens.BORDER_ACTIVE);
        canvas.drawRect(clipExitRect, exitBorder);
        Paint exitText = ThemeTokens.newTextPaint();
        exitText.setTextSize(20f);
        exitText.setColor(ThemeTokens.TEXT_PRIMARY);
        Paint.FontMetrics efm = exitText.getFontMetrics();
        float exitTextY = clipExitRect.centerY() - (efm.ascent + efm.descent) / 2;
        canvas.drawText("退出", clipExitRect.centerX() - exitText.measureText("退出") / 2, exitTextY, exitText);

        if (history == null || history.isEmpty()) {
            Paint emptyPaint = ThemeTokens.newTextPaint();
            emptyPaint.setTextSize(20f);
            emptyPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            canvas.drawText("剪贴板为空", w / 2f - 60, topBarHeight + (h - topBarHeight) / 2f, emptyPaint);
            return;
        }

        java.util.List<Integer> pinnedIdx = new java.util.ArrayList<>();
        java.util.List<Integer> regularIdx = new java.util.ArrayList<>();
        for (int i = 0; i < history.size(); i++) {
            if (history.get(i).pinned) pinnedIdx.add(i); else regularIdx.add(i);
        }
        int regularVisible = Math.min(regularIdx.size(), MAX_VISIBLE_CLIPBOARD_ITEMS);
        java.util.List<Integer> rows = new java.util.ArrayList<>();
        rows.addAll(pinnedIdx);
        for (int i = regularIdx.size() - regularVisible; i < regularIdx.size(); i++) {
            rows.add(regularIdx.get(i));
        }

        float listTop = topBarHeight;
        float listHeight = h - topBarHeight;
        float itemHeight = listHeight / rows.size();
        Paint bgPaint = ThemeTokens.newBgPaint();
        Paint textPaint = ThemeTokens.newTextPaint();
        textPaint.setTextSize(18f);
        Paint borderPaint = ThemeTokens.newBorderPaint();
        borderPaint.setColor(ThemeTokens.BORDER);
        Paint btnTextPaint = ThemeTokens.newTextPaint();
        btnTextPaint.setTextSize(15f);

        int btnW = 90;

        for (int row = 0; row < rows.size(); row++) {
            int realIdx = rows.get(row);
            SimpleImeService.ClipboardEntry entry = history.get(realIdx);
            float top = listTop + row * itemHeight;
            float bottom = top + itemHeight;

            bgPaint.setColor(entry.pinned ? ThemeTokens.PRESS_BG : ThemeTokens.SURFACE_RAISED);
            canvas.drawRect(0, top, w, bottom, bgPaint);
            canvas.drawLine(0, bottom, w, bottom, borderPaint);

            int pinBtnLeft = w - btnW * 2;
            int delBtnLeft = w - btnW;

            Rect textRect = new Rect(0, (int) top, pinBtnLeft, (int) bottom);
            Rect pinRect = new Rect(pinBtnLeft, (int) top, delBtnLeft, (int) bottom);
            Rect delRect = new Rect(delBtnLeft, (int) top, w, (int) bottom);
            clipTextRects.add(textRect);
            clipPinRects.add(pinRect);
            clipDeleteRects.add(delRect);
            clipRowRealIndex.add(realIdx);

            String display = entry.text.replace("\\n", " ");
            int maxChars = Math.max(4, (pinBtnLeft - 16) / 20);
            if (display.length() > maxChars) display = display.substring(0, maxChars) + "...";
            textPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            Paint.FontMetrics fm = textPaint.getFontMetrics();
            float textY = top + itemHeight / 2 - (fm.ascent + fm.descent) / 2;
            canvas.drawText(display, 16, textY, textPaint);

            canvas.drawLine(pinBtnLeft, top, pinBtnLeft, bottom, borderPaint);
            canvas.drawLine(delBtnLeft, top, delBtnLeft, bottom, borderPaint);

            btnTextPaint.setColor(ThemeTokens.TEXT_PRIMARY);
            String pinLabel = entry.pinned ? "取消" : "固定";
            Paint.FontMetrics bfm = btnTextPaint.getFontMetrics();
            float btnTextY = top + itemHeight / 2 - (bfm.ascent + bfm.descent) / 2;
            canvas.drawText(pinLabel, pinBtnLeft + btnW / 2f - btnTextPaint.measureText(pinLabel) / 2, btnTextY, btnTextPaint);
            canvas.drawText("删除", delBtnLeft + btnW / 2f - btnTextPaint.measureText("删除") / 2, btnTextY, btnTextPaint);
        }
    }

    public ClipboardHit hitTestClipboard(float x, float y) {
        ClipboardHit hit = new ClipboardHit();
        if (clipExitRect.contains((int) x, (int) y)) {
            hit.action = ClipboardHit.EXIT;
            return hit;
        }
        for (int row = 0; row < clipTextRects.size(); row++) {
            int realIdx = clipRowRealIndex.get(row);
            if (clipPinRects.get(row).contains((int) x, (int) y)) {
                hit.action = ClipboardHit.PIN;
                hit.index = realIdx;
                return hit;
            }
            if (clipDeleteRects.get(row).contains((int) x, (int) y)) {
                hit.action = ClipboardHit.DELETE;
                hit.index = realIdx;
                return hit;
            }
            if (clipTextRects.get(row).contains((int) x, (int) y)) {
                hit.action = ClipboardHit.PASTE;
                hit.index = realIdx;
                return hit;
            }
        }
        return hit;
    }
}'''

assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)
path.write_text(text, encoding="utf-8")
print("KeyboardRenderer.java 打补丁完成")
