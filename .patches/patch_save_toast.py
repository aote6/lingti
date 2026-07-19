import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/KeyboardView.java")
text = path.read_text(encoding="utf-8")

old1 = "import android.view.View;"
new1 = "import android.view.View;\nimport android.widget.Toast;"
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

old2 = """            if (editMode && saveButtonRect.contains(x, y)) {
                RuleLoader.save(getContext(), layoutManager.getProfile(), layoutFileName, layoutStateName);
                invalidate();
                return true;
            }"""
new2 = """            if (editMode && saveButtonRect.contains(x, y)) {
                RuleLoader.save(getContext(), layoutManager.getProfile(), layoutFileName, layoutStateName);
                Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();
                invalidate();
                return true;
            }"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

path.write_text(text, encoding="utf-8")
print("KeyboardView.java 打补丁完成，2处全部成功")
