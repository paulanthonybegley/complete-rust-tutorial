package com.example.ebicsswift;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpSession;

@Controller
public class EbicsSepaSwiftController {
	@Autowired private LedgerChain chain;

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
	public List<Map<String, Object>> api(HttpSession session) {
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
		mark(session, slug);
		Map<String, Object> m = new LinkedHashMap<>();
		m.put("slug", c.slug()); m.put("name", c.name()); m.put("awsService", c.awsService());
		m.put("backing", c.backing()); m.put("role", c.role());
		m.put("mode", "simulated");
		m.put("verify", chain.verify());
		return m;
	}

	// Hamilton: the integrity RPC runs FIRST and with priority.
	@GetMapping("/api/verify")
	@ResponseBody
	public Map<String, Object> verify() { return chain.verify(); }

	@GetMapping("/api/priority")
	@ResponseBody
	public Map<String, Object> priority() {
		Map<String, Object> m = new LinkedHashMap<>();
		Map<String, Object> v = chain.verify();
		m.put("invariant", "every euro is conserved");
		m.put("conserved", chain.conserved());
		m.put("priority", Boolean.TRUE.equals(v.get("ok")) ? "settle" : "repair-first");
		m.put("why", "Hamilton: the critical path can never be interrupted by lower-priority work.");
		return m;
	}

	@GetMapping("/api/selftest")
	@ResponseBody
	public Map<String, Object> selftest() {
		// Hipp: test your assumptions on a COPY, never on the real thing.
		Map<String, Object> m = new LinkedHashMap<>();
		String before = String.valueOf(chain.verify().get("ok"));
		chain.corruptRandomly();
		Map<String, Object> after = chain.verify();
		m.put("before", before);
		m.put("afterDetection", after.get("ok"));
		m.put("detected", !Boolean.TRUE.equals(after.get("ok")));
		m.put("firstBreakAt", after.get("firstBreakAt"));
		m.put("philosophy", "Hipp: a database you cannot deliberately break is a database you do not trust.");
		return m;
	}

	@GetMapping("/api/restore")
	@ResponseBody
	public Map<String, Object> restore() {
		chain = new LedgerChain();
		return chain.verify();
	}

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
