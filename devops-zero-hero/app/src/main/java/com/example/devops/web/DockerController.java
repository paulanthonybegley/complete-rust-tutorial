package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.DockerService;
import com.example.devops.service.K8sService;
import com.example.devops.service.PipelineService;

/**
 * The integrated Container lab: one page walks the whole ship-an-image arc —
 * the pipeline builds it, the docker layer-cache shows the build cost, and the
 * cluster (k8s) runs it. Each panel is served by the same fragment endpoints
 * the standalone pages use. The /docker (page), /docker/build (partial),
 * /pipeline/run (partial) and /k8s/reconcile (partial) routes stay untouched.
 */
@Controller
public class DockerController {

	private final DockerService docker;
	private final PipelineService pipelines;
	private final K8sService k8s;

	public DockerController(DockerService docker, PipelineService pipelines, K8sService k8s) {
		this.docker = docker;
		this.pipelines = pipelines;
		this.k8s = k8s;
	}

	@GetMapping("/docker")
	public String page(@RequestParam(defaultValue = "none") String failAt,
			@RequestParam(defaultValue = "src") String change, @RequestParam(defaultValue = "false") boolean noCache,
			@RequestParam(defaultValue = "3") int replicas, @RequestParam(required = false) Integer killPod,
			@RequestParam(defaultValue = "false") boolean rolling, Model model) {
		model.addAttribute("run", pipelines.run(failAt));
		model.addAttribute("failAt", failAt);
		model.addAttribute("dresult", docker.build(change, noCache));
		model.addAttribute("change", change);
		model.addAttribute("noCache", noCache);
		model.addAttribute("kresult", k8s.reconcile(replicas, killPod, rolling));
		model.addAttribute("kreplicas", replicas);
		model.addAttribute("killPod", killPod == null ? "" : killPod);
		return "docker";
	}

	@GetMapping("/docker/build")
	public String build(@RequestParam(defaultValue = "src") String change,
			@RequestParam(defaultValue = "false") boolean noCache, Model model) {
		model.addAttribute("result", docker.build(change, noCache));
		return "_fragments :: dockerLayers";
	}
}