package com.example.euind.model;

/**
 * A flashcard pair for the /flashcards deck browser.
 */
public record Flashcard(String front, String back, Category category) {
}