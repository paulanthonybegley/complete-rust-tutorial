package com.example.devops.web;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.devops.model.Category;
import com.example.devops.model.VideoStatus;
import com.example.devops.service.ConceptService;

/**
 * Dashboard: a "ship meter" (share of concepts already practised or shipped
 * in the video) plus one stat per theme and a few featured concepts.
 */
@Controller
public class HomeController {

	private final ConceptService concepts;

	public HomeController(ConceptService concepts) {
		this.concepts = concepts;
	}

	@GetMapping("/")
	public String index(Model model) {
		long moving = concepts.all().stream()
				.filter(c -> c.status() == VideoStatus.PRACTISED || c.status() == VideoStatus.SHIPPED)
				.count();
		int meter = (int) Math.round(100.0 * moving / concepts.all().size());
		model.addAttribute("meter", meter);
		model.addAttribute("meterNote", meter >= 60 ? "Most of the course lands in the build and the deploy."
				: "Heavy on fundamentals — the shipping arc is still ahead.");
		model.addAttribute("total", concepts.all().size());

		Map<Category, Integer> counts = new EnumMap<>(Category.class);
		for (Category c : Category.values()) {
			counts.put(c, concepts.byCategory(c).size());
		}
		model.addAttribute("counts", counts);

		model.addAttribute("featured", concepts.all().stream()
				.filter(c -> List.of("culture", "pipeline", "docker", "ship").contains(c.id()))
				.toList());
		return "index";
	}
}