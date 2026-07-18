package com.unbounded.input.core.candidate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CandidateEngine {
    private final List<CandidateProvider> providers = new ArrayList<>();
    private CandidateProvider active;
    private final FrequencyCache cache = new FrequencyCache();

    public CandidateEngine() {
        register(new T9Provider());
        active = providers.get(0);
    }

    public void register(CandidateProvider provider) {
        providers.add(provider);
    }

    public void setActive(String id) {
        for (CandidateProvider p : providers) {
            if (p.id().equals(id)) { active = p; return; }
        }
    }

    public CandidateProvider getActive() { return active; }

    public FrequencyCache getCache() { return cache; }

    public List<String> query(String input) {
        if (active == null) return new ArrayList<>();
        List<String> raw = active.query(input);
        Collections.sort(raw, new Comparator<String>() {
            public int compare(String a, String b) {
                return cache.get(b) - cache.get(a);
            }
        });
        return raw;
    }

    public void onCandidateSelected(String word) {
        cache.hit(word);
    }
}
