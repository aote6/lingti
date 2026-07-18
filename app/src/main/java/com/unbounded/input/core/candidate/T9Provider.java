package com.unbounded.input.core.candidate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class T9Provider implements CandidateProvider {
    private static final Map<String, String[]> dict = new HashMap<>();

    static {
        dict.put("64426", new String[]{"你好", "您好"});
        dict.put("9432634", new String[]{"知道了", "知道了啦"});
        dict.put("4263", new String[]{"好的", "好得", "号灯"});
        dict.put("646424", new String[]{"明白", "名白", "明摆"});
        dict.put("9347663", new String[]{"为什么", "为甚么", "为啥呢"});
        dict.put("526526", new String[]{"看看", "看见", "看啦", "看了"});
        dict.put("5394", new String[]{"可以", "客气", "可疑", "颗星"});
        dict.put("967447484", new String[]{"我是谁", "我试试"});
        dict.put("744", new String[]{"是", "时", "事", "十", "使"});
        dict.put("96", new String[]{"我", "喔", "窝"});
        dict.put("2", new String[]{"啊", "不", "才", "吧", "比", "爱", "安"});
        dict.put("3", new String[]{"的", "到", "大", "但", "地", "对", "多"});
        dict.put("4", new String[]{"个", "国", "工", "过", "给", "关", "高"});
        dict.put("5", new String[]{"就", "可", "看", "开", "快", "口", "苦"});
        dict.put("6", new String[]{"你", "没", "们", "能", "那", "年", "女"});
        dict.put("7", new String[]{"是", "上", "时", "说", "谁", "水", "书"});
        dict.put("8", new String[]{"他", "到", "同", "天", "听", "头", "她"});
        dict.put("9", new String[]{"我", "为", "无", "问", "完", "王", "文"});
        dict.put("9426464", new String[]{"中国人", "中国心", "中国行", "中国红", "中国风"});
        dict.put("2323", new String[]{"爸爸", "宝宝", "白白", "伯伯", "贝贝", "北边"});
        dict.put("5264", new String[]{"开心", "开明", "开年", "开慢", "开满", "看那", "看你"});
    }

    @Override
    public String id() { return "t9"; }

    @Override
    public List<String> query(String digits) {
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
