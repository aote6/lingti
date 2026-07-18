package com.unbounded.input.core.candidate;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class T9Provider implements CandidateProvider {
    private static final Map<String, List<String>> dict = new HashMap<>();
    private static boolean loaded = false;

    public static void init(Context context) {
        if (loaded) return;
        try {
            AssetManager am = context.getAssets();
            android.util.Log.d("T9Provider", "Loading t9_dict.txt from assets");
            android.util.Log.d("T9Provider", "Loading t9_dict.txt from assets");
            BufferedReader reader = new BufferedReader(new InputStreamReader(am.open("t9_dict.txt"), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 2) {
                    String digits = parts[0];
                    String word = parts[1];
                    List<String> list = dict.get(digits);
                    if (list == null) {
                        list = new ArrayList<>();
                        dict.put(digits, list);
                    }
                    if (!list.contains(word)) {
                        list.add(word);
                    }
                }
            }
            reader.close();
            loaded = true;
        } catch (Exception e) {
            android.util.Log.e("T9Provider", "Failed to load t9_dict.txt, using fallback", e);
            // 文件加载失败用内置备用词库
            buildFallbackDict();
            loaded = true;
        }
    }

    private static void buildFallbackDict() {
        addFallback("2", "啊", "不", "才", "吧", "比", "爱", "安");
        addFallback("3", "的", "到", "大", "但", "地", "对", "多");
        addFallback("4", "个", "国", "工", "过", "给", "关", "高");
        addFallback("5", "就", "可", "看", "开", "快", "口", "苦");
        addFallback("6", "你", "没", "们", "能", "那", "年", "女");
        addFallback("7", "是", "上", "时", "说", "谁", "水", "书");
        addFallback("8", "他", "到", "同", "天", "听", "头", "她");
        addFallback("9", "我", "为", "无", "问", "完", "王", "文");
        addFallback("23", "爱", "爸");
        addFallback("24", "不");
        addFallback("26", "的", "嗯");
        addFallback("28", "发");
        addFallback("33", "飞");
        addFallback("34", "个");
        addFallback("36", "好");
        addFallback("43", "就");
        addFallback("46", "可");
        addFallback("48", "了");
        addFallback("53", "妈");
        addFallback("56", "你");
        addFallback("63", "去");
        addFallback("66", "是");
        addFallback("68", "他");
        addFallback("74", "我");
        addFallback("78", "小");
        addFallback("84", "一");
        addFallback("86", "这");
        addFallback("88", "中");
        addFallback("94", "在");
        addFallback("2323", "爸爸", "宝宝");
        addFallback("5264", "开心", "看那");
        addFallback("64426", "你好");
        addFallback("9426464", "中国人");
        addFallback("646424", "明白");
        addFallback("9347663", "为什么");
        addFallback("526526", "看看");
    }

    private static void addFallback(String digits, String... words) {
        List<String> list = dict.get(digits);
        if (list == null) {
            list = new ArrayList<>();
            dict.put(digits, list);
        }
        for (String w : words) {
            if (!list.contains(w)) list.add(w);
        }
    }

    @Override
    public String id() { return "t9"; }

    @Override
    public List<String> query(String digits) {
        List<String> res = new ArrayList<>();
        if (digits == null || digits.isEmpty()) return res;
        List<String> exact = dict.get(digits);
        if (exact != null) {
            for (String w : exact) res.add(w);
        }
        for (Map.Entry<String, List<String>> e : dict.entrySet()) {
            if (e.getKey().startsWith(digits) && !e.getKey().equals(digits)) {
                for (String w : e.getValue()) {
                    if (!res.contains(w)) res.add(w);
                }
            }
        }
        return res;
    }
}
