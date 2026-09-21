package com.example.devops.model;

import java.util.List;

/**
 * One feature raised in the video: what it is, what the video says about it,
 * and the key commands/artifacts that make it real.
 */
public record Concept(String id, Category category, VideoStatus status, String title, String subtitle, String summary,
		List<String> inVideo, List<Figure> figures) {
}