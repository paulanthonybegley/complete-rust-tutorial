package com.example.devops.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.service.MonitorService;

/**
 * Observability simulator: mark a pod unhealthy and see the health panel
 * react — uptime, latency, error rate, and (un)missed 3 a.m. pages.
 */
@Controller
public class MonitorController {

	private final MonitorService monitor;

	public MonitorController(MonitorService monitor) {
		this.monitor = monitor;
	}

	@GetMapping("/monitor")
	public String page(@RequestParam(defaultValue = "false") boolean unhealthy,
			@RequestParam(defaultValue = "true") boolean autoHeal, Model model) {
		model.addAttribute("result", monitor.sample(unhealthy, autoHeal));
		model.addAttribute("unhealthy", unhealthy);
		model.addAttribute("autoHeal", autoHeal);
		return "monitor";
	}

	@GetMapping("/monitor/sample")
	public String sample(@RequestParam(defaultValue = "false") boolean unhealthy,
			@RequestParam(defaultValue = "true") boolean autoHeal, Model model) {
		model.addAttribute("result", monitor.sample(unhealthy, autoHeal));
		return "_fragments :: monitorPanel";
	}
}