package com.example.euind.model;

/**
 * A single number from the speech or its press coverage, with a plain-English
 * reading. Every figure in the course is a {@link Figure}.
 */
public record Figure(String label, String value, String note) {
}