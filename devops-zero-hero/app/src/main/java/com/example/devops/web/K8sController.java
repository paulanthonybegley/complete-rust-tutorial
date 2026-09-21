package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.K8sService;

/**
 * The orchestrator simulator: scale replicas, "kill a pod" to trigger
 * self-healing, and toggle a rolling update.
 */
@Controller
public class K8sController {

	private final K8sService k8s;

	public K8sController(K8sService k8s) {
		this.k8s = k8s;
	}

	@GetMapping("/k8s")
	public String page(@RequestParam(defaultValue = "3") int replicas, Model model) {
		model.addAttribute("result", k8s.reconcile(replicas, null, false));
		model.addAttribute("replicas", replicas);
		return "k8s";
	}

	@GetMapping("/k8s/reconcile")
	public String reconcile(@RequestParam(defaultValue = "3") int replicas,
			@RequestParam(required = false) Integer killPod,
			@RequestParam(defaultValue = "false") boolean rolling, Model model) {
		model.addAttribute("result", k8s.reconcile(replicas, killPod, rolling));
		return "_fragments :: k8sCluster";
	}
}