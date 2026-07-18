package com.unbounded.input;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class RuleLoader {

    public static List<KeyDef> load(Context context, String fileName) {
        List<KeyDef> keys = new ArrayList<>();
        try {
            InputStream is = context.getAssets().open(fileName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject root = new JSONObject(json);
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
                keys.add(key);
            }
        } catch (Exception e) {
            SimpleImeService.log(context, "RuleLoader 加载失败: " + e.getMessage());
        }
        return keys;
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
