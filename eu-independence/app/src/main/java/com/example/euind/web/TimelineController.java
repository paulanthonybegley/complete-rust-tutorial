package com.example.euind.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.euind.model.Category;
import com.example.euind.service.TimelineService;

/**
 * The "last year → the speech → what's next" story as a filterable timeline.
 * The page inserts the {@code grid} fragment; clicking a theme re-fetches only
 * that fragment via htmx.
 */
@Controller
public class TimelineController {

	private final TimelineService timeline;

	public TimelineController(TimelineService timeline) {
		this.timeline = timeline;
	}

	@GetMapping("/timeline")
	public String page(@RequestParam(required = false) Category cat, Model model) {
		model.addAttribute("events", cat == null ? timeline.all() : timeline.byCategory(cat));
		model.addAttribute("selected", cat);
		return "timeline";
	}

	@GetMapping("/timeline/grid")
	public String grid(@RequestParam(required = false) Category cat, Model model) {
		model.addAttribute("events", cat == null ? timeline.all() : timeline.byCategory(cat));
		model.addAttribute("selected", cat);
		return "_fragments :: timelineGrid";
	}
}