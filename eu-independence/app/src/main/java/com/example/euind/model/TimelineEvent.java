package com.example.euind.model;

/**
 * One stop on the "last year -> the speech -> what's next" story the video
 * tells.
 */
public record TimelineEvent(String when, String title, Category category, String summary) {
}