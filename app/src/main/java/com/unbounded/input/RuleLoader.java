package com.unbounded.input;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RuleLoader {

    public static class LayoutConfig {
        public int version;
        public String layout;
        public String context;
        public List<KeyDef> keys = new ArrayList<>();
    }

    public static LayoutConfig load(Context context, String fileName) {
        LayoutConfig config = new LayoutConfig();
        try {
            InputStream is = context.getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
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
    }
}
