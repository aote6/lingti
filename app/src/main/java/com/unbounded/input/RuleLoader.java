// JSON布局加载器：从assets解析布局配置，转成LayoutProfile
package com.unbounded.input;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.command.KeyEventCommand;
import com.unbounded.input.core.command.KeyChordCommand;

public class RuleLoader {

    public static class LayoutConfig {
        public int version;
        public String layout;
        public String context;
        public String activeState;
        public Map<String, List<List<KeyDef>>> stateRows = new HashMap<>();
        public Map<String, List<KeyDef>> states = new HashMap<>();
        public List<KeyDef> keys = new ArrayList<>();
        public boolean hasRows = false;

        public List<KeyModel> toKeyModels() {
            List<KeyDef> activeKeys = keys;
            if (!states.isEmpty() && activeState != null) {
                List<KeyDef> stateKeys = states.get(activeState);
                if (stateKeys != null) activeKeys = stateKeys;
            }
            List<KeyModel> models = new ArrayList<>();
            for (KeyDef d : activeKeys) {
                models.add(d.toKeyModel());
            }
            return models;
        }

        public com.unbounded.input.core.layout.LayoutProfile buildProfile() {
            com.unbounded.input.core.layout.LayoutProfile profile = new com.unbounded.input.core.layout.LayoutProfile(layout);
            if (hasRows && activeState != null) {
                List<List<KeyDef>> rows = stateRows.get(activeState);
                if (rows != null) {
                    for (List<KeyDef> rowDefs : rows) {
                        com.unbounded.input.core.layout.RowSpec row = new com.unbounded.input.core.layout.RowSpec();
                        for (KeyDef d : rowDefs) {
                            row.add(d.toKeyModel());
                        }
                        profile.addRow(row);
                    }
                }
            }
            return profile;
        }
    }

    private static final Map<String, LayoutConfig> cache = new HashMap<>();

    // 还原出厂布局用：强制清掉内存缓存，否则删了外部JSON文件后
    // 仍会返回上一次已解析过的缓存对象，看起来像"没有真正还原"。
    public static void clearCache(String fileName) {
        cache.remove(fileName);
    }

    public static LayoutConfig load(Context context, String fileName) {
        LayoutConfig cached = cache.get(fileName);
        if (cached != null) return cached;

        LayoutConfig config = new LayoutConfig();
        try {
            InputStream is;
            File externalFile = context.getExternalFilesDir(null) != null
                    ? new File(context.getExternalFilesDir(null), fileName) : null;
            if (externalFile != null && externalFile.exists()) {
                is = new FileInputStream(externalFile);
                SimpleImeService.log(context, "RuleLoader: 从可写目录加载 " + externalFile.getAbsolutePath());
            } else {
                is = context.getAssets().open(fileName);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            is.close();
            String json = out.toString(StandardCharsets.UTF_8.name());

            JSONObject root = new JSONObject(json);

            config.version = root.optInt("version", 1);
            config.layout = root.optString("layout", "ninekey");
            config.context = root.optString("context", "chinese");

            if (config.version < 2) {
                SimpleImeService.log(context, "RuleLoader: 旧版schema v" + config.version + "，请升级配置");
            }

            // 解析 states（v3+ 新格式）
            JSONObject statesObj = root.optJSONObject("states");
            if (statesObj != null) {
                for (Iterator<String> it = statesObj.keys(); it.hasNext(); ) {
                    String stateName = it.next();
                    JSONObject stateObj = statesObj.getJSONObject(stateName);
                    // 优先用 rows（二维数组），否则用 keys（一维数组）
                    JSONArray rowsArr = stateObj.optJSONArray("rows");
                    if (rowsArr != null) {
                        config.hasRows = true;
                        List<List<KeyDef>> rowList = new ArrayList<>();
                        for (int r = 0; r < rowsArr.length(); r++) {
                            JSONArray keysArr = rowsArr.optJSONArray(r);
                            if (keysArr == null) continue;
                            List<KeyDef> keyList = new ArrayList<>();
                            for (int i = 0; i < keysArr.length(); i++) {
                                JSONObject obj = keysArr.optJSONObject(i);
                                if (obj == null) continue;
                                keyList.add(parseKeyDef(obj));
                            }
                            rowList.add(keyList);
                        }
                        config.stateRows.put(stateName, rowList);
                    } else {
                        JSONArray keysArr = stateObj.getJSONArray("keys");
                        List<KeyDef> keyList = new ArrayList<>();
                        for (int i = 0; i < keysArr.length(); i++) {
                            JSONObject obj = keysArr.optJSONObject(i);
                            if (obj == null) continue;
                            keyList.add(parseKeyDef(obj));
                        }
                        config.states.put(stateName, keyList);
                    }
                }
                config.activeState = root.optString("default_state", "letter_lower");
            } else {
                // 旧版格式：直接解析顶层 keys
                JSONArray arr = root.getJSONArray("keys");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) continue;
                    config.keys.add(parseKeyDef(obj));
                }
            }

            // 只有成功才缓存
            cache.put(fileName, config);
        } catch (Exception e) {
            SimpleImeService.log(context, "RuleLoader 加载失败: " + e.getMessage());
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            SimpleImeService.log(context, sw.toString());
        }
        return config;
    }

    private static KeyDef parseKeyDef(JSONObject obj) {
        KeyDef key = new KeyDef();
        key.explicitLabel = obj.optString("label", null);
        key.span = (float) obj.optDouble("span", 1f);
        if (obj.has("x") && obj.has("y") && obj.has("w") && obj.has("h")) {
            key.x = (float) obj.optDouble("x");
            key.y = (float) obj.optDouble("y");
            key.w = (float) obj.optDouble("w");
            key.h = (float) obj.optDouble("h");
        }
        key.tap = parseCommand(obj.optJSONObject("tap"));
        key.swipeUp = parseCommand(obj.optJSONObject("swipeUp"));
        key.swipeDown = parseCommand(obj.optJSONObject("swipeDown"));
        key.swipeLeft = parseCommand(obj.optJSONObject("swipeLeft"));
        key.swipeRight = parseCommand(obj.optJSONObject("swipeRight"));
        key.longPress = parseCommand(obj.optJSONObject("longPress"));
        return key;
    }

    private static Command parseCommand(JSONObject obj) {
        if (obj == null) return null;
        String type = obj.optString("type", "");
        String text = obj.optString("text", "");
        switch (type) {
            case "insert": return Command.insert(text);
            case "backspace": return Command.backspace();
            case "commit": return Command.commit();

            case "key_event": {
                int keyCode = obj.optInt("keyCode", 0);
                int metaState = obj.optInt("metaState", 0);
                if (metaState != 0) return KeyEventCommand.withMeta(keyCode, metaState);
                return KeyEventCommand.of(keyCode);
            }
            case "key_chord": {
                JSONArray arr = obj.optJSONArray("keyCodes");
                if (arr == null) return null;
                int[] codes = new int[arr.length()];
                for (int i = 0; i < arr.length(); i++) codes[i] = arr.optInt(i);
                int metaState = obj.optInt("metaState", 0);
                if (metaState != 0) return new KeyChordCommand(codes, metaState);
                return KeyChordCommand.of(codes);
            }
            default: return null;
        }
    }

    // 拖拽摆放模式保存：把当前 LayoutProfile 写回可写目录的 JSON 文件。
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

    public static class KeyDef {
        public String explicitLabel;
        public float span = 1f;
        public Float x, y, w, h; // 百分比坐标（0-100），四者都非null才生效
        public Command tap;
        public Command swipeUp;
        public Command swipeDown;
        public Command swipeLeft;
        public Command swipeRight;
        public Command longPress;

        public KeyModel toKeyModel() {
            String id = explicitLabel != null ? explicitLabel : label();
            float s = span > 0 ? span : 1f;
            boolean hasPercent = x != null && y != null && w != null && h != null;
            KeyModel m = hasPercent
                ? new KeyModel(id, id, s, 2, 2, 2, 2, true, x, y, w, h)
                : new KeyModel(id, id, s, 2, 2, 2, 2);
            m.tap = tap;
            m.swipeUp = swipeUp;
            m.swipeDown = swipeDown;
            m.swipeLeft = swipeLeft;
            m.swipeRight = swipeRight;
            m.longPress = longPress;
            return m;
        }

        public String label() {
            if (explicitLabel != null && !explicitLabel.isEmpty()) return explicitLabel;
            if (tap != null && tap.type == Command.Type.INSERT_TEXT && !tap.text.isEmpty()) return tap.text;
            if (tap != null && tap.type == Command.Type.BACKSPACE) return "\u232b";
            if (tap != null) return tap.type.name().toLowerCase();
            return "?";
        }
    }
}
