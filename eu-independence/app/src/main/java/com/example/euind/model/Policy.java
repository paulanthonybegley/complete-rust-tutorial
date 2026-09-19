package com.example.euind.model;

import java.util.List;

import org.springframework.util.StringUtils;

/**
 * One feature raised in the video, ready to be rendered as a policy card.
 */
public record Policy(
		String id,
		String title,
		String subtitle,
		Category category,
		Status status,
		String summary,
		List<String> detail,
		List<Figure> figures) {

	public String tagline() {
		return title + (StringUtils.hasText(subtitle) ? " — " + subtitle : "");
	}
}