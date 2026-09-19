package com.example.euind.web;

import java.time.Year;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.euind.model.Category;

/**
 * Shared model attributes for every page: the theme list used by the header
 * nav and the timelines, plus the current year for footers.
 */
@ControllerAdvice
public class NavAdvice {

	@ModelAttribute("navCategories")
	public Category[] navCategories() {
		return Category.values();
	}

	@ModelAttribute("currentYear")
	public int currentYear() {
		return Year.now().getValue();
	}
}