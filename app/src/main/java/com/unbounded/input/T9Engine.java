package com.unbounded.input;

import android.content.Context;
import com.unbounded.input.core.candidate.CandidateEngine;
import java.util.List;

public class T9Engine {
    private static final CandidateEngine engine = new CandidateEngine();
    private static boolean initialized = false;

    public static void init(Context context) {
        if (!initialized) {
            engine.getCache().init(context);
            initialized = true;
        }
    }

    public static void save() {
        engine.getCache().save();
    }

    public static List<String> getCandidates(String digits) {
        return engine.query(digits);
    }

    public static void onCandidateSelected(String word) {
        engine.onCandidateSelected(word);
    }
}
