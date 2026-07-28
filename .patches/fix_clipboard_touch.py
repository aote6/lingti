import pathlib
path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardView.java")
text = path.read_text(encoding="utf-8")

old = '''        if (clipboardPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                java.util.List<String> history = com.unbounded.input.SimpleImeService.getClipboardHistory();
                int count = history.size();
                int visibleIdx = renderer.hitTestClipboardItem(getHeight(), count, event.getY());
                if (visibleIdx >= 0) {
                    int realIndex = count - 1 - visibleIdx;
                    if (imeService != null) {
                        imeService.pasteClipboardItem(realIndex);
                    }
                }
                closeClipboardPanel();
            }
            return true;
        }'''

new = '''        if (clipboardPanelOpen) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                KeyboardRenderer.ClipboardHit hit = renderer.hitTestClipboard(event.getX(), event.getY());
                switch (hit.action) {
                    case KeyboardRenderer.ClipboardHit.EXIT:
                        closeClipboardPanel();
                        break;
                    case KeyboardRenderer.ClipboardHit.PASTE:
                        if (imeService != null) imeService.pasteClipboardItem(hit.index);
                        invalidate();
                        break;
                    case KeyboardRenderer.ClipboardHit.PIN:
                        com.unbounded.input.SimpleImeService.toggleClipboardPin(hit.index);
                        invalidate();
                        break;
                    case KeyboardRenderer.ClipboardHit.DELETE:
                        com.unbounded.input.SimpleImeService.deleteClipboardItem(hit.index);
                        invalidate();
                        break;
                    default:
                        break;
                }
            }
            return true;
        }'''

assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)
path.write_text(text, encoding="utf-8")
print("KeyboardView.java 打补丁完成")
