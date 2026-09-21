package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.GitGraphService;

/**
 * The version-control explorer: a hand-drawn git graph for main or the
 * feature branch, swapped in place by htmx.
 */
@Controller
public class GitController {

	private final GitGraphService git;

	public GitController(GitGraphService git) {
		this.git = git;
	}

	@GetMapping("/git")
	public String page(@RequestParam(defaultValue = "main") String branch, Model model) {
		model.addAttribute("commits", git.forBranch(branch));
		model.addAttribute("branch", branch);
		return "git";
	}

	@GetMapping("/git/graph")
	public String graph(@RequestParam(defaultValue = "main") String branch, Model model) {
		model.addAttribute("commits", git.forBranch(branch));
		model.addAttribute("branch", branch);
		return "_fragments :: gitGraph";
	}
}