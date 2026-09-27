package com.example.codesmells.domain;

import java.util.List;

/**
 * One lab = one code smell from the playlist, its before/after snippet pair,
 * and the assessment options that make the course measurable.
 */
public record Smell(
        String slug,
        int episode,
        String title,
        String subtitle,
        String videoId,
        String summary,
        String symptom,
        String fix,
        String why,
        String beforeSnippet,
        String afterSnippet,
        List<String> identifyOptions,
        int identifyCorrect,
        List<String> refactorOptions,
        int refactorCorrect) {

    public String identifyAnswer() {
        return identifyOptions.get(identifyCorrect);
    }

    public String refactorAnswer() {
        return refactorOptions.get(refactorCorrect);
    }
}