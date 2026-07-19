import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/SimpleImeService.java")
text = path.read_text(encoding="utf-8")

old = """                java.io.File dir = getExternalFilesDir(null);
                if (dir != null) {
                    java.io.File saved = new java.io.File(dir, layoutFileName);
                    if (saved.exists()) saved.delete();
                }
                rebuildKeyboard();"""
new = """                java.io.File dir = getExternalFilesDir(null);
                if (dir != null) {
                    java.io.File saved = new java.io.File(dir, layoutFileName);
                    if (saved.exists()) saved.delete();
                }
                RuleLoader.clearCache(layoutFileName);
                rebuildKeyboard();"""
assert text.count(old) == 1, "锚点未找到或不唯一"
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("SimpleImeService.java 打补丁完成")
