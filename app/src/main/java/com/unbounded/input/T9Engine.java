package com.unbounded.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class T9Engine {
    private static final Map<String, String[]> dict = new HashMap<>();

    static {
        dict.put("64426", new String[]{"你好"});
        dict.put("9432634", new String[]{"知道了"});
        dict.put("4263", new String[]{"好的"});
        dict.put("646424", new String[]{"明白"});
        dict.put("9347663", new String[]{"为什么"});
        dict.put("526526", new String[]{"看看"});
        dict.put("5394", new String[]{"可以", "客气"});
        dict.put("967447484", new String[]{"我是谁"});
        dict.put("744", new String[]{"是"});
        dict.put("96", new String[]{"我"});

        dict.put("2", new String[]{"啊", "不", "才"});
        dict.put("3", new String[]{"的", "到", "大"});
        dict.put("4", new String[]{"个", "国", "工"});
        dict.put("5", new String[]{"就", "可", "看"});
        dict.put("6", new String[]{"你", "没", "们"});
        dict.put("7", new String[]{"是", "上", "时"});
        dict.put("8", new String[]{"他", "到", "同"});
        dict.put("9", new String[]{"我", "为", "无"});
    }

    public static List<String> getCandidates(String digits) {
        List<String> res = new ArrayList<>();
        if (digits == null || digits.isEmpty()) return res;
        if (dict.containsKey(digits)) {
            for (String w : dict.get(digits)) res.add(w);
        }
        for (String key : dict.keySet()) {
            if (key.startsWith(digits) && !key.equals(digits)) {
                for (String w : dict.get(key)) {
                    if (!res.contains(w)) res.add(w);
                }
            }
        }
        return res;
    }
}
