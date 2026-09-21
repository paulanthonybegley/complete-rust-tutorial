package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.TerraformService;

/**
 * The IaC simulator: plan, apply or destroy a small declared environment and
 * watch the planner diff reality against the desired state.
 */
@Controller
public class IacController {

	private final TerraformService terraform;

	public IacController(TerraformService terraform) {
		this.terraform = terraform;
	}

	@GetMapping("/iac")
	public String page(@RequestParam(defaultValue = "plan") String mode,
			@RequestParam(defaultValue = "none") String change, Model model) {
		model.addAttribute("result", terraform.run(mode, change));
		model.addAttribute("mode", mode);
		model.addAttribute("change", change);
		return "iac";
	}

	@GetMapping("/iac/run")
	public String run(@RequestParam(defaultValue = "plan") String mode,
			@RequestParam(defaultValue = "none") String change, Model model) {
		model.addAttribute("result", terraform.run(mode, change));
		return "_fragments :: terraformPlan";
	}
}