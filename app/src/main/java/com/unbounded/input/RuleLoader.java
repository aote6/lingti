package com.unbounded.input;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.unbounded.input.core.layout.KeyModel;
import com.unbounded.input.core.command.KeyEventCommand;
import com.unbounded.input.core.command.KeyChordCommand;

public class RuleLoader {

    public static class LayoutConfig {
        public int version;
        public String layout;
        public String context;
        public List<KeyDef> keys = new ArrayList<>();
        public List<KeyModel> toKeyModels() {
            List<KeyModel> models = new ArrayList<>();
            for (KeyDef d : keys) {
                KeyModel m = new KeyModel(d.label(), d.label(), 1f, 2, 2, 2, 2);
                m.tap = d.tap;
                m.swipeUp = d.swipeUp;
                m.swipeDown = d.swipeDown;
                m.swipeLeft = d.swipeLeft;
                m.swipeRight = d.swipeRight;
                m.longPress = d.longPress;
                models.add(m);
            }
            return models;
        }
    }

    private static final java.util.Map<String, LayoutConfig> cache = new java.util.HashMap<>();

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

            JSONArray arr = root.getJSONArray("keys");
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                KeyDef key = new KeyDef();
                key.tap = parseCommand(obj.optJSONObject("tap"));
                key.swipeUp = parseCommand(obj.optJSONObject("swipeUp"));
                key.swipeDown = parseCommand(obj.optJSONObject("swipeDown"));
                key.swipeLeft = parseCommand(obj.optJSONObject("swipeLeft"));
                key.swipeRight = parseCommand(obj.optJSONObject("swipeRight"));
                key.longPress = parseCommand(obj.optJSONObject("longPress"));
                config.keys.add(key);
            }
        } catch (Exception e) {
            SimpleImeService.log(context, "RuleLoader 加载失败: " + e.getMessage());
        }
        cache.put(fileName, config);
        return config;
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
        public Command tap;
        public Command swipeUp;
        public Command swipeDown;
        public Command swipeLeft;
        public Command swipeRight;
        public Command longPress;

        public String label() {
            if (tap != null && tap.type == Command.Type.INSERT_TEXT && !tap.text.isEmpty()) return tap.text;
            if (tap != null) return tap.type.name().toLowerCase();
            return "?";
        }
    }
}
