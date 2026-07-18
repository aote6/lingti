package com.unbounded.input.core.candidate;

import java.util.ArrayList;
import java.util.List;

public class CandidateEngine {
    private final List<CandidateProvider> providers = new ArrayList<>();
    private CandidateProvider active;

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

    public List<String> query(String input) {
        if (active == null) return new ArrayList<>();
        return active.query(input);
    }
}
