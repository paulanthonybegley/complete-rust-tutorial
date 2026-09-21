package com.example.devops.model;

/**
 * A flip card: term on the front, definition on the back.
 */
public record Flashcard(Category category, String front, String back) {
}