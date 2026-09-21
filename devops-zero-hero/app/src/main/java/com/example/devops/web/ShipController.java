package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

import com.example.devops.service.ConceptService;

/**
 * The "ship it" page: shows the four-command shipping arc and the artefacts
 * the course provides for containerizing the eighth-api-rules API.
 */
@Controller
public class ShipController {

	private final ConceptService concepts;

	public ShipController(ConceptService concepts) {
		this.concepts = concepts;
	}

	@GetMapping("/ship")
	public String page(Model model) {
		model.addAttribute("ship", concepts.byId("ship"));
		model.addAttribute("dockerize", concepts.byId("dockerize"));
		model.addAttribute("kubernetes", concepts.byId("kubernetes"));
		return "ship";
	}
}