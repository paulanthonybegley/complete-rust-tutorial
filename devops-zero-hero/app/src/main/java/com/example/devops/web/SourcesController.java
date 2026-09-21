package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Sources: the video with its timestamped roadmap.
 */
@Controller
public class SourcesController {

	@GetMapping("/sources")
	public String page(Model model) {
		return "sources";
	}
}