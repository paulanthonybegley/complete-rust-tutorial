package com.example.devops.model;

import java.util.List;

/**
 * A self-grading quiz question mapped to one course theme.
 */
public record QuizQuestion(String id, String prompt, List<String> options, int answerIndex, Category category,
		String explanation) {
}