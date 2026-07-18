package com.unbounded.input.core.candidate;

import java.util.List;

public interface CandidateProvider {
    String id();
    List<String> query(String input);
}
