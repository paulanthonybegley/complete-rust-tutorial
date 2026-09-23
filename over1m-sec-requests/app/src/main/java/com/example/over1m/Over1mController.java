package com.example.over1m;

import java.time.Year;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

/**
 * One tick for each station the 2024 "Let's Handle One Million Requests per
 * Second, It's Scarier Than You Think!" video measured. Every number lives in
 * {@link SimLoad}; this controller only picks which station page to show and
 * which arc to hand it. The session keeps the small "done" meter alive so a
 * learner who closes the tab and comes back still sees where they were.
 */
@Controller
public class Over1mController {

	private static final String DONE = "over1m.done";

	/** The eight stations in the order the video measured them. */
	private static final List<String> STATIONS = List.of(
		"bench", "pm2", "nic", "db", "cache", "async", "cluster", "uuids"
	);

	// ---- the home console: 8 stations, one measured arc ----------------------

	@GetMapping("/")
	public String index(HttpSession session, Model model) {
		model.addAttribute("stations", STATIONS);
		model.addAttribute("done", done(session));
		model.addAttribute("meter", done(session));
		model.addAttribute("meterMax", STATIONS.size());
		return "index";
	}

	@GetMapping("/bench")
	public String bench(HttpSession session, Model model) {
		model.addAttribute("frameworks", SimLoad.FRAMEWORKS.values());
		mark(session, "bench");
		return "bench";
	}

	@GetMapping("/pm2")
	public String pm2(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.PM2_ARC);
		mark(session, "pm2");
		return "pm2";
	}

	@GetMapping("/nic")
	public String nic(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.NIC_ARC);
		mark(session, "nic");
		return "nic";
	}

	@GetMapping("/db")
	public String db(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.DB_ARC);
		mark(session, "db");
		return "db";
	}

	@GetMapping("/cache")
	public String cache(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.CACHE_ARC);
		mark(session, "cache");
		return "cache";
	}

	@GetMapping("/async")
	public String async(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.ASYNC_ARC);
		mark(session, "async");
		return "async";
	}

	@GetMapping("/cluster")
	public String cluster(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.CLUSTER_ARC);
		mark(session, "cluster");
		return "cluster";
	}

	@GetMapping("/uuids")
	public String uuids(HttpSession session, Model model) {
		model.addAttribute("steps", SimLoad.UUID_ARC);
		mark(session, "uuids");
		return "uuids";
	}

	// ---- the little progress meter -------------------------------------------

	private void mark(HttpSession session, String slug) {
		@SuppressWarnings("unchecked")
		var done = (LinkedHashSet<String>) session.getAttribute(DONE);
		if (done == null) {
			done = new LinkedHashSet<>();
		}
		done.add(slug);
		session.setAttribute(DONE, done);
	}

	private int done(HttpSession session) {
		@SuppressWarnings("unchecked")
		var done = (LinkedHashSet<String>) session.getAttribute(DONE);
		return done == null ? 0 : done.size();
	}
}
