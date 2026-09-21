package com.example.devops.web;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.model.Category;
import com.example.devops.model.Concept;
import com.example.devops.service.ConceptService;

/**
 * The concept explorer: every feature raised in the video is a card; the grid
 * re-filters by theme and drills into a detail drawer via htmx fragments.
 */
@Controller
public class ConceptController {

	private final ConceptService concepts;

	public ConceptController(ConceptService concepts) {
		this.concepts = concepts;
	}

	@GetMapping("/concepts")
	public String page(@RequestParam(required = false) Category cat, Model model) {
		model.addAttribute("items", items(cat));
		model.addAttribute("selected", cat);
		model.addAttribute("counts", counts());
		return "concepts";
	}

	@GetMapping("/concepts/grid")
	public String grid(@RequestParam(required = false) Category cat, Model model) {
		model.addAttribute("items", items(cat));
		model.addAttribute("selected", cat);
		return "_fragments :: conceptsGrid";
	}

	@GetMapping("/concepts/detail")
	public String detail(@RequestParam String id, Model model) {
		Concept concept = concepts.byId(id);
		if (concept == null) {
			model.addAttribute("missing", id);
			return "_fragments :: conceptDetail";
		}
		model.addAttribute("concept", concept);
		return "_fragments :: conceptDetail";
	}

	private java.util.List<Concept> items(Category cat) {
		return cat == null ? concepts.all() : concepts.byCategory(cat);
	}

	private Map<Category, Integer> counts() {
		Map<Category, Integer> map = new EnumMap<>(Category.class);
		for (Category c : Category.values()) {
			map.put(c, concepts.byCategory(c).size());
		}
		return map;
	}
}