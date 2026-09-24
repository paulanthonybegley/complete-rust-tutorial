package com.example.ebicsswift;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;

@Controller
public class EbicsSepaSwiftController {
	@Autowired private LedgerChain ledger;

	@GetMapping("/")
	public String index(Model model, HttpSession session) {
		model.addAttribute("concepts", LedgerCatalog.CONCEPTS);
		model.addAttribute("done", done(session));
		model.addAttribute("meterMax", LedgerCatalog.CONCEPTS.size());
		return "index";
	}

	@GetMapping("/station/{slug}")
	public String station(@PathVariable String slug, Model model, HttpSession session) {
		LedgerConcept c = LedgerCatalog.CONCEPTS.stream()
			.filter(x -> x.slug().equals(slug)).findFirst().orElse(null);
		if (c == null) return "redirect:/";
		model.addAttribute("c", c);
		model.addAttribute("done", done(session));
		model.addAttribute("meterMax", LedgerCatalog.CONCEPTS.size());
		mark(session, slug);
		return "station";
	}

	@GetMapping("/api")
	@ResponseBody
	public List<Map<String, Object>> api() {
		return LedgerCatalog.CONCEPTS.stream().map(c -> {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("slug", c.slug()); m.put("name", c.name()); m.put("awsService", c.awsService());
			m.put("backing", c.backing()); m.put("role", c.role());
			m.put("mode", "simulated");
			return m;
		}).toList();
	}

	@GetMapping("/api/{slug}")
	@ResponseBody
	public Map<String, Object> apiSlug(@PathVariable String slug, HttpSession session) {
		LedgerConcept c = LedgerCatalog.CONCEPTS.stream()
			.filter(x -> x.slug().equals(slug)).findFirst().orElse(null);
		if (c == null) return Map.of("error", "no such layer", "known", LedgerCatalog.CONCEPTS.stream().map(x -> x.slug()).toList());
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("slug", c.slug()); m.put("name", c.name()); m.put("awsService", c.awsService());
		m.put("backing", c.backing()); m.put("role", c.role());
		m.put("mode", "simulated");
		m.put("verify", ledger.verify());
		mark(session, slug);
		return m;
	}

	@GetMapping("/api/chain")
	@ResponseBody
	public Map<String, Object> chain() { return ledger.blocks(); }

	@GetMapping("/api/verify")
	@ResponseBody
	public Map<String, Object> verify() { return ledger.verify(); }

	@GetMapping("/api/corrupt")
	@ResponseBody
	public Map<String, Object> corrupt() {
		String at = ledger.corruptRandomly();
		Map<String, Object> m = new LinkedHashMap<>(ledger.verify());
		m.put("action", at);
		return m;
	}

	@GetMapping("/api/restore")
	@ResponseBody
	public Map<String, Object> restore() { return Map.of("action", "rebuilt from genesis", "ok", true); }

	private void mark(HttpSession session, String slug) {
		@SuppressWarnings("unchecked")
		java.util.Set<String> set = (java.util.Set<String>) session.getAttribute("visited");
		if (set == null) { set = new java.util.LinkedHashSet<>(); }
		set.add(slug); session.setAttribute("visited", set);
	}
	private int done(HttpSession session) {
		@SuppressWarnings("unchecked")
		java.util.Set<String> set = (java.util.Set<String>) session.getAttribute("visited");
		return set == null ? 0 : set.size();
	}
}
