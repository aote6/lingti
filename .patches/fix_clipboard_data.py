import pathlib
path = pathlib.Path("app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

old = '''    private final Handler focusHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private PasteManager pasteManager;
    private FrameLayout inputRoot;
    private static final java.util.List<String> clipboardHistory = new ArrayList<>();
    private static final int MAX_CLIPBOARD_HISTORY = 20;
    private ClipboardManager clipboardManager;

    public static java.util.List<String> getClipboardHistory() { return clipboardHistory; }



    private void pasteRecentClipboard() {
        if (clipboardHistory.isEmpty()) return;
        log(this, "[PASTE] history.size=" + clipboardHistory.size() + " isEmpty=" + clipboardHistory.isEmpty());
        String text = clipboardHistory.get(clipboardHistory.size() - 1);
        pasteManager.cancel();
        InputConnection ic = getCurrentInputConnection();
        pasteManager.paste(ic, text, pasteManager.shouldThrottle(getCurrentInputEditorInfo()));
    }

    public void pasteClipboardItem(int index) {
        if (index < 0 || index >= clipboardHistory.size()) return;
        String text = clipboardHistory.get(index);
        pasteManager.cancel();
        InputConnection ic = getCurrentInputConnection();
        pasteManager.paste(ic, text, pasteManager.shouldThrottle(getCurrentInputEditorInfo()));
    }

    private void initClipboard() {
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    log(SimpleImeService.this, "[CLIP] changed, history.size=" + clipboardHistory.size());
                    ClipData clip = clipboardManager.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        String text = clip.getItemAt(0).getText().toString();
                        if (text != null && !text.isEmpty() && !text.equals(clipboardHistory.isEmpty() ? "" : clipboardHistory.get(clipboardHistory.size() - 1))) {
                            clipboardHistory.add(text);
                            if (clipboardHistory.size() > MAX_CLIPBOARD_HISTORY) {
                                clipboardHistory.remove(0);
                            }
                        }
                    }
                }
            });
        }
    }'''

new = '''    private final Handler focusHandler = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private PasteManager pasteManager;
    private FrameLayout inputRoot;

    public static class ClipboardEntry {
        public String text;
        public boolean pinned;
        public ClipboardEntry(String text, boolean pinned) {
            this.text = text;
            this.pinned = pinned;
        }
    }

    private static final java.util.List<ClipboardEntry> clipboardHistory = new ArrayList<>();
    private static final int MAX_CLIPBOARD_HISTORY = 20;
    private ClipboardManager clipboardManager;

    public static java.util.List<ClipboardEntry> getClipboardHistory() { return clipboardHistory; }

    public static void toggleClipboardPin(int index) {
        if (index < 0 || index >= clipboardHistory.size()) return;
        ClipboardEntry e = clipboardHistory.get(index);
        e.pinned = !e.pinned;
    }

    public static void deleteClipboardItem(int index) {
        if (index < 0 || index >= clipboardHistory.size()) return;
        clipboardHistory.remove(index);
    }

    private void pasteRecentClipboard() {
        if (clipboardHistory.isEmpty()) return;
        log(this, "[PASTE] history.size=" + clipboardHistory.size() + " isEmpty=" + clipboardHistory.isEmpty());
        String text = clipboardHistory.get(clipboardHistory.size() - 1).text;
        pasteManager.cancel();
        InputConnection ic = getCurrentInputConnection();
        pasteManager.paste(ic, text, pasteManager.shouldThrottle(getCurrentInputEditorInfo()));
    }

    public void pasteClipboardItem(int index) {
        if (index < 0 || index >= clipboardHistory.size()) return;
        String text = clipboardHistory.get(index).text;
        pasteManager.cancel();
        InputConnection ic = getCurrentInputConnection();
        pasteManager.paste(ic, text, pasteManager.shouldThrottle(getCurrentInputEditorInfo()));
    }

    private void initClipboard() {
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    log(SimpleImeService.this, "[CLIP] changed, history.size=" + clipboardHistory.size());
                    ClipData clip = clipboardManager.getPrimaryClip();
                    if (clip != null && clip.getItemCount() > 0) {
                        String text = clip.getItemAt(0).getText().toString();
                        String lastText = clipboardHistory.isEmpty() ? "" : clipboardHistory.get(clipboardHistory.size() - 1).text;
                        if (text != null && !text.isEmpty() && !text.equals(lastText)) {
                            clipboardHistory.add(new ClipboardEntry(text, false));
                            if (clipboardHistory.size() > MAX_CLIPBOARD_HISTORY) {
                                int removeIdx = 0;
                                while (removeIdx < clipboardHistory.size() && clipboardHistory.get(removeIdx).pinned) {
                                    removeIdx++;
                                }
                                if (removeIdx < clipboardHistory.size()) {
                                    clipboardHistory.remove(removeIdx);
                                }
                            }
                        }
                    }
                }
            });
        }
    }'''

assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)
path.write_text(text, encoding="utf-8")
print("SimpleImeService.java 打补丁完成")
