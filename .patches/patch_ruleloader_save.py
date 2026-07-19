import pathlib

path = pathlib.Path("app/src/main/java/com/unbounded/input/RuleLoader.java")
text = path.read_text(encoding="utf-8")

# 补丁1：加 import
old1 = """import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;"""
new1 = """import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;"""
assert text.count(old1) == 1, "补丁1锚点未找到或不唯一"
text = text.replace(old1, new1, 1)

# 补丁2：加载优先读 getExternalFilesDir，找不到再回退 assets
old2 = """            InputStream is = context.getAssets().open(fileName);"""
new2 = """            InputStream is;
            File externalFile = context.getExternalFilesDir(null) != null
                    ? new File(context.getExternalFilesDir(null), fileName) : null;
            if (externalFile != null && externalFile.exists()) {
                is = new FileInputStream(externalFile);
                SimpleImeService.log(context, "RuleLoader: 从可写目录加载 " + externalFile.getAbsolutePath());
            } else {
                is = context.getAssets().open(fileName);
            }"""
assert text.count(old2) == 1, "补丁2锚点未找到或不唯一"
text = text.replace(old2, new2, 1)

# 补丁3：在类末尾（最后的 } 之前）插入 save() 和序列化方法
old3 = """    public static class KeyDef {"""
new3 = """    // 拖拽摆放模式保存：把当前 LayoutProfile 写回可写目录的 JSON 文件。
    // 注意：只保存 profile 里实际持有的那些行/键（即当前激活的 state），
    // 如果原JSON里有其他未激活的 state（比如 shift 布局），这次保存不会保留它们。
    public static void save(Context context, com.unbounded.input.core.layout.LayoutProfile profile,
                            String fileName, String stateName) {
        try {
            JSONObject root = new JSONObject();
            root.put("version", 3);
            root.put("layout", profile.id);
            root.put("context", "terminal");
            root.put("default_state", stateName);

            JSONArray rowsArr = new JSONArray();
            for (com.unbounded.input.core.layout.RowSpec row : profile.rows) {
                JSONArray keysArr = new JSONArray();
                for (KeyModel key : row.keys) {
                    keysArr.put(keyModelToJson(key));
                }
                rowsArr.put(keysArr);
            }
            JSONObject stateObj = new JSONObject();
            stateObj.put("rows", rowsArr);
            JSONObject statesObj = new JSONObject();
            statesObj.put(stateName, stateObj);
            root.put("states", statesObj);

            File dir = context.getExternalFilesDir(null);
            if (dir == null) {
                SimpleImeService.log(context, "RuleLoader.save: getExternalFilesDir 返回 null，无法保存");
                return;
            }
            if (!dir.exists()) dir.mkdirs();
            File outFile = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            fos.write(root.toString(2).getBytes(StandardCharsets.UTF_8));
            fos.close();

            cache.remove(fileName);
            SimpleImeService.log(context, "RuleLoader.save: 已保存到 " + outFile.getAbsolutePath());
        } catch (Exception e) {
            SimpleImeService.log(context, "RuleLoader.save 失败: " + e.getMessage());
        }
    }

    private static JSONObject keyModelToJson(KeyModel key) throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("label", key.label);
        if (key.hasPercentRect) {
            obj.put("x", key.percentX);
            obj.put("y", key.percentY);
            obj.put("w", key.percentW);
            obj.put("h", key.percentH);
        } else {
            obj.put("span", key.span);
        }
        JSONObject tap = serializeCommand(key.tap);
        if (tap != null) obj.put("tap", tap);
        JSONObject su = serializeCommand(key.swipeUp);
        if (su != null) obj.put("swipeUp", su);
        JSONObject sd = serializeCommand(key.swipeDown);
        if (sd != null) obj.put("swipeDown", sd);
        JSONObject sl = serializeCommand(key.swipeLeft);
        if (sl != null) obj.put("swipeLeft", sl);
        JSONObject sr = serializeCommand(key.swipeRight);
        if (sr != null) obj.put("swipeRight", sr);
        JSONObject lp = serializeCommand(key.longPress);
        if (lp != null) obj.put("longPress", lp);
        return obj;
    }

    // 和 parseCommand() 对称：把 Command 对象还原成 JSON。
    // 注意：CLIPBOARD_* 类型不支持往返序列化（parseCommand 本来就不认识这个type），
    // 剪贴板键目前是靠 label 硬匹配触发的，不受影响。
    private static JSONObject serializeCommand(Command cmd) throws JSONException {
        if (cmd == null) return null;
        JSONObject obj = new JSONObject();
        switch (cmd.type) {
            case INSERT_TEXT:
                obj.put("type", "insert");
                obj.put("text", cmd.text);
                return obj;
            case BACKSPACE:
                obj.put("type", "backspace");
                return obj;
            case COMMIT:
                obj.put("type", "commit");
                return obj;
            case KEY_EVENT:
                if (cmd instanceof KeyEventCommand) {
                    KeyEventCommand kec = (KeyEventCommand) cmd;
                    obj.put("type", "key_event");
                    obj.put("keyCode", kec.keyCode);
                    if (kec.metaState != 0) obj.put("metaState", kec.metaState);
                    return obj;
                }
                return null;
            case KEY_CHORD:
                if (cmd instanceof KeyChordCommand) {
                    KeyChordCommand kcc = (KeyChordCommand) cmd;
                    obj.put("type", "key_chord");
                    JSONArray codes = new JSONArray();
                    for (int c : kcc.keyCodes) codes.put(c);
                    obj.put("keyCodes", codes);
                    if (kcc.metaState != 0) obj.put("metaState", kcc.metaState);
                    return obj;
                }
                return null;
            default:
                return null;
        }
    }

    public static class KeyDef {"""
assert text.count(old3) == 1, "补丁3锚点未找到或不唯一"
text = text.replace(old3, new3, 1)

path.write_text(text, encoding="utf-8")
print("RuleLoader.java 打补丁完成，3处全部成功")
