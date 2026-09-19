package com.example.euind.web;

import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.euind.model.CouncilMember;
import com.example.euind.model.CouncilResult;
import com.example.euind.model.EnergyResult;
import com.example.euind.model.MaterialsResult;
import com.example.euind.model.Policy;
import com.example.euind.service.PartnerService;
import com.example.euind.service.PolicyService;
import com.example.euind.service.SimService;

/**
 * The Independence Lab: three transparent simulators driven by htmx form
 * inputs. Each GET returns the matching fragment so the numbers update in
 * place.
 */
@Controller
public class SimController {

	private final SimService sims;
	private final PartnerService partners;
	private final PolicyService policies;

	public SimController(SimService sims, PartnerService partners, PolicyService policies) {
		this.sims = sims;
		this.partners = partners;
		this.policies = policies;
	}

	@GetMapping("/lab")
	public String lab(Model model) {
		model.addAttribute("energy", sims.energy(44.0));
		model.addAttribute("materials", sims.materials(90.0));
		model.addAttribute("council", sims.council(Set.of(), Set.of("Canada".toLowerCase())));
		model.addAttribute("permanent", partners.permanentMembers());
		model.addAttribute("rotating", partners.rotatingMembers());
		model.addAttribute("external", sims.externalPartners());
		model.addAttribute("labContext", policies.byId("raw-corp"));
		model.addAttribute("energyContext", policies.byId("energy-2040"));
		model.addAttribute("councilContext", policies.byId("def-council"));
		return "lab";
	}

	@GetMapping("/lab/energy")
	public String energy(@RequestParam(defaultValue = "44") double share, Model model) {
		model.addAttribute("energy", sims.energy(share));
		model.addAttribute("energyContext", policies.byId("energy-2040"));
		return "_fragments :: energyResult";
	}

	@GetMapping("/lab/materials")
	public String materials(@RequestParam(defaultValue = "90") double share, Model model) {
		model.addAttribute("materials", sims.materials(share));
		model.addAttribute("labContext", policies.byId("raw-corp"));
		return "_fragments :: materialsResult";
	}

	@GetMapping("/lab/council")
	public String council(@RequestParam(required = false) Set<String> members,
			@RequestParam(required = false) Set<String> partnersIn, Model model) {
		model.addAttribute("council", sims.council(members, partnersIn));
		model.addAttribute("councilContext", policies.byId("def-council"));
		model.addAttribute("chosen", members == null ? Set.of() : members);
		model.addAttribute("chosenPartners", partnersIn == null ? Set.of() : partnersIn);
		return "_fragments :: councilResult";
	}
}