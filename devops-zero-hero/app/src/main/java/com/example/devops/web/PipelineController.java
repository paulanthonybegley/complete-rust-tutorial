package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.PipelineService;

/**
 * The CI/CD pipeline simulator: run the eight-stage build and inject a failure
 * anywhere to watch the pipeline stop at the first red job.
 */
@Controller
public class PipelineController {

	private final PipelineService pipelines;

	public PipelineController(PipelineService pipelines) {
		this.pipelines = pipelines;
	}

	@GetMapping("/pipeline")
	public String page(@RequestParam(defaultValue = "none") String failAt, Model model) {
		model.addAttribute("run", pipelines.run(failAt));
		model.addAttribute("failAt", failAt);
		return "pipeline";
	}

	@GetMapping("/pipeline/run")
	public String run(@RequestParam(defaultValue = "none") String failAt, Model model) {
		model.addAttribute("run", pipelines.run(failAt));
		return "_fragments :: pipelineRun";
	}
}