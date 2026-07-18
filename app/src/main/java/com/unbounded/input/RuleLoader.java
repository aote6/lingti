package com.unbounded.input;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
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

    public static LayoutConfig load(Context context, String fileName) {
        LayoutConfig cached = cache.get(fileName);
        if (cached != null) return cached;

        LayoutConfig config = new LayoutConfig();
        try {
            InputStream is = context.getAssets().open(fileName);
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

    public static class KeyDef {
        public String explicitLabel;
        public float span = 1f;
        public Command tap;
        public Command swipeUp;
        public Command swipeDown;
        public Command swipeLeft;
        public Command swipeRight;
        public Command longPress;

        public KeyModel toKeyModel() {
            String id = explicitLabel != null ? explicitLabel : label();
            float s = span > 0 ? span : 1f;
            KeyModel m = new KeyModel(id, id, s, 2, 2, 2, 2);
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
