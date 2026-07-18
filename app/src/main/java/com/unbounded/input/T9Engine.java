package com.unbounded.input;

import com.unbounded.input.core.candidate.CandidateEngine;
import java.util.List;

public class T9Engine {
    private static final CandidateEngine engine = new CandidateEngine();

    public static List<String> getCandidates(String digits) {
        return engine.query(digits);
    }

    public static void onCandidateSelected(String word) {
        engine.onCandidateSelected(word);
    }
}
