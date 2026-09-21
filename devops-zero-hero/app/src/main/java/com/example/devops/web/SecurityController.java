package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.SecurityService;

/**
 * The edge-guard simulator: shape a request (kind of client, bot-likeness,
 * API key) and watch the risk score decide allow vs block.
 */
@Controller
public class SecurityController {

	private final SecurityService security;

	public SecurityController(SecurityService security) {
		this.security = security;
	}

	@GetMapping("/security")
	public String page(@RequestParam(defaultValue = "browser") String kind,
			@RequestParam(defaultValue = "10") int botLike,
			@RequestParam(defaultValue = "true") boolean hasKey, Model model) {
		model.addAttribute("result", security.check(kind, botLike, hasKey));
		model.addAttribute("kind", kind);
		model.addAttribute("botLike", botLike);
		model.addAttribute("hasKey", hasKey);
		return "security";
	}

	@GetMapping("/security/check")
	public String check(@RequestParam(defaultValue = "browser") String kind,
			@RequestParam(defaultValue = "10") int botLike,
			@RequestParam(defaultValue = "true") boolean hasKey, Model model) {
		model.addAttribute("result", security.check(kind, botLike, hasKey));
		return "_fragments :: securityVerdict";
	}
}