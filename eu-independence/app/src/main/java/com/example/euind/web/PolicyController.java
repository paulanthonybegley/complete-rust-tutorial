package com.example.euind.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.euind.model.Category;
import com.example.euind.model.Policy;
import com.example.euind.service.PolicyService;

/**
 * The policy explorer. Every feature raised in the video is a card; the grid
 * is rendered by a fragment so htmx can re-filter and drill down without a
 * full page reload.
 */
@Controller
public class PolicyController {

	private final PolicyService policies;

	public PolicyController(PolicyService policies) {
		this.policies = policies;
	}

	@GetMapping("/policies")
	public String page(@RequestParam(required = false) Category cat, Model model) {
		model.addAttribute("items", cat == null ? policies.all() : policies.byCategory(cat));
		model.addAttribute("selected", cat);
		model.addAttribute("counts", counts());
		return "policies";
	}

	@GetMapping("/policies/grid")
	public String grid(@RequestParam(required = false) Category cat, Model model) {
		model.addAttribute("items", cat == null ? policies.all() : policies.byCategory(cat));
		model.addAttribute("selected", cat);
		return "_fragments :: policiesGrid";
	}

	@GetMapping("/policies/detail")
	public String detail(@RequestParam String id, Model model) {
		Policy policy = policies.byId(id);
		if (policy == null) {
			model.addAttribute("missing", id);
			return "_fragments :: policyDetail";
		}
		model.addAttribute("policy", policy);
		return "_fragments :: policyDetail";
	}

	/* Friendly path-based drill-down as well (for the lesson plan pages). */
	@GetMapping("/policies/{id}")
	public ResponseEntity<String> redirectToDetail(@PathVariable String id) {
		Policy policy = policies.byId(id);
		if (policy == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body("<div class=\"notice\">No policy card with id '" + id + "'.</div>");
		}
		String brand = """
				<div class="notice">This is a <strong>lesson document</strong> reference:
				open <code>/policies</code> in the app and drill into “%s”.</div>
				""".formatted(policy.title());
		return ResponseEntity.ok().contentType(org.springframework.http.MediaType.TEXT_HTML).body(brand);
	}

	private java.util.Map<Category, Integer> counts() {
		java.util.Map<Category, Integer> map = new java.util.EnumMap<>(Category.class);
		for (Category c : Category.values()) {
			map.put(c, policies.byCategory(c).size());
		}
		return map;
	}
}