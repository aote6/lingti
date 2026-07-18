package com.unbounded.input.core.candidate;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashMap;
import java.util.Map;

public class FrequencyCache {
    private final Map<String, Integer> freq = new HashMap<>();
    private SharedPreferences prefs;
    private boolean dirty = false;

    public void init(Context context) {
        prefs = context.getSharedPreferences("lingti_freq", Context.MODE_PRIVATE);
        Map<String, ?> all = prefs.getAll();
        for (Map.Entry<String, ?> e : all.entrySet()) {
            try {
                freq.put(e.getKey(), (Integer) e.getValue());
            } catch (Exception ignored) {}
        }
    }

    public void hit(String word) {
        if (word == null || word.isEmpty()) return;
        Integer count = freq.get(word);
        freq.put(word, (count == null ? 0 : count) + 1);
        dirty = true;
    }

    public int get(String word) {
        Integer count = freq.get(word);
        return count == null ? 0 : count;
    }

    public void save() {
        if (prefs == null || !dirty) return;
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, Integer> e : freq.entrySet()) {
            editor.putInt(e.getKey(), e.getValue());
        }
        editor.apply();
        dirty = false;
    }
}
