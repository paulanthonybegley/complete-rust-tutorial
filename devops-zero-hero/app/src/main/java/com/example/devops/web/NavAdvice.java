package com.example.devops.web;

import java.time.Year;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.devops.model.Category;

/**
 * Shared model attributes for every page: the category list used by the header
 * nav and the current year for footers.
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