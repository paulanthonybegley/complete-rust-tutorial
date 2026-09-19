package com.example.euind.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.euind.model.Category;
import com.example.euind.service.PolicyService;

/**
 * Dashboard: one stat per theme plus a headline "independence meter". The meter
 * is a deliberately rough proxy — the share of policies the video shows in
 * progress or delivered vs merely proposed.
 */
@Controller
public class HomeController {

	private final PolicyService policies;

	public HomeController(PolicyService policies) {
		this.policies = policies;
	}

	@GetMapping("/")
	public String index(Model model) {
		double delivered = policies.all().stream()
				.filter(p -> p.status().name().equals("DELIVERED") || p.status().name().equals("IN_PROGRESS"))
				.count();
		int meter = (int) Math.round(100.0 * delivered / policies.all().size());
		model.addAttribute("meter", meter);
		model.addAttribute("meterNote", meter >= 60 ? "Delivery is outpacing proposals."
				: "Mostly proposals — the speech is young.");
		model.addAttribute("total", policies.all().size());
		java.util.Map<Category, Integer> counts = new java.util.EnumMap<>(Category.class);
		for (Category c : Category.values()) {
			counts.put(c, policies.byCategory(c).size());
		}
		model.addAttribute("counts", counts);
		model.addAttribute("featured", policies.all().stream()
				.filter(p -> List.of("def-article4", "raw-china", "energy-2040", "part-canada").contains(p.id()))
				.toList());
		return "index";
	}
}