package com.unbounded.input.core.candidate;

import java.util.HashMap;
import java.util.Map;

public class FrequencyCache {
    private final Map<String, Integer> freq = new HashMap<>();

    public void hit(String word) {
        if (word == null || word.isEmpty()) return;
        Integer count = freq.get(word);
        freq.put(word, (count == null ? 0 : count) + 1);
    }

    public int get(String word) {
        Integer count = freq.get(word);
        return count == null ? 0 : count;
    }
}
