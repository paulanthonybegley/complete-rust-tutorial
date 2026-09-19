package com.example.euind.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.euind.model.PartnerKind;
import com.example.euind.service.PartnerService;

/**
 * The /partners explorer: associate-member candidates, trade agreements and
 * observing "Euro-curious" countries. The page inserts the whole groups
 * fragment; theme chips re-fetch a single group via htmx.
 */
@Controller
public class PartnerController {

	private final PartnerService partners;

	public PartnerController(PartnerService partners) {
		this.partners = partners;
	}

	@GetMapping("/partners")
	public String page(Model model) {
		model.addAttribute("groups", groups());
		return "partners";
	}

	@GetMapping("/partners/group")
	public String group(@RequestParam(required = false) PartnerKind kind, Model model) {
		model.addAttribute("groups", kind == null ? groups() : java.util.Map.of(kind, partners.byKind(kind)));
		return "_fragments :: partnerGroups";
	}

	private java.util.Map<PartnerKind, java.util.List<com.example.euind.model.Partner>> groups() {
		java.util.Map<PartnerKind, java.util.List<com.example.euind.model.Partner>> map = new java.util.EnumMap<>(
				PartnerKind.class);
		for (PartnerKind k : PartnerKind.values()) {
			map.put(k, partners.byKind(k));
		}
		return map;
	}
}