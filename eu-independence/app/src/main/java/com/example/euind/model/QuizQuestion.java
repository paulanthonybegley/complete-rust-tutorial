package com.example.euind.model;

import java.util.List;

/**
 * A self-grading quiz question used by the /quiz htmx endpoint.
 */
public record QuizQuestion(String id, String prompt, List<String> options, int answerIndex, Category category,
		String explanation) {
}