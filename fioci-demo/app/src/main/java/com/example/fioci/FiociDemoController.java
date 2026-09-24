package com.example.fioci;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

@Controller
public class FiociDemoController {
	private final FiociService svc;
	public FiociDemoController(FiociService svc) { this.svc = svc; }

	@GetMapping("/")
	public String index(HttpSession session, Model model) {
		model.addAttribute("concepts", AwsCatalog.CONCEPTS);
		model.addAttribute("done", svc.done(session));
		model.addAttribute("meterMax", AwsCatalog.CONCEPTS.size());
		return "index";
	}

	@GetMapping("/station/{slug}")
	public String station(@PathVariable String slug, HttpSession session, Model model) {
		AwsConcept c = AwsCatalog.CONCEPTS.stream()
			.filter(x -> x.slug().equals(slug)).findFirst().orElseThrow();
		model.addAttribute("c", c);
		model.addAttribute("done", svc.done(session));
		model.addAttribute("meterMax", AwsCatalog.CONCEPTS.size());
		return "station";
	}

	@GetMapping("/api/{slug}")
	@ResponseBody
	public Map<String, Object> api(@PathVariable String slug, HttpSession session) {
		AwsConcept c = AwsCatalog.CONCEPTS.stream()
			.filter(x -> x.slug().equals(slug)).findFirst().orElseThrow();
		return svc.describe(c, session);
	}

	@GetMapping("/api")
	@ResponseBody
	public List<Map<String, Object>> api(HttpSession session) {
		return AwsCatalog.CONCEPTS.stream().map(c -> svc.describe(c, session)).toList();
	}
}
