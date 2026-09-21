package com.example.devops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.SecurityCheck;
import com.example.devops.model.SecurityResult;

/**
 * Edge guard simulator (the role Arcjet plays in the video): a mix of
 * heuristics scores an incoming request and decides allow vs block.
 */
@Service
public class SecurityService {

	public SecurityResult check(String kind, int botLike, boolean hasKey) {
		String k = kind == null || kind.isBlank() ? "browser" : kind.toLowerCase();
		int b = Math.max(0, Math.min(100, botLike));

		List<SecurityCheck> checks = new ArrayList<>();
		boolean ua = k.equals("scraper") || (k.equals("curl") && b >= 60);
		checks.add(new SecurityCheck("User-agent fingerprint", ua, ua ? "bot UA or headless hints" : "organic UA"));

		boolean headless = b >= 80 && !k.equals("browser");
		checks.add(new SecurityCheck("Headless / automation hints", headless,
				headless ? "no GPU, synthetic timing" : "clean"));

		boolean rate = k.equals("spammer") || b >= 60;
		checks.add(new SecurityCheck("Request rate", rate, rate ? "bursting (500 req/min)" : "human pace"));

		boolean payload = k.equals("spammer") || (k.equals("scraper") && b >= 70);
		checks.add(new SecurityCheck("Payload pattern", payload,
				payload ? "repeated form-submission template" : "unique content"));

		boolean key = hasKey;
		checks.add(new SecurityCheck("API key presented", !key, key ? "valid edge key" : "missing key"));
		if (!key) {
			checks.set(4, new SecurityCheck("API key presented", true, "missing key"));
		}

		int base = switch (k) {
			case "scraper" -> 35;
			case "curl" -> 25;
			case "spammer" -> 55;
			default -> b != 0 ? b / 3 : 0;
		};
		int score = Math.max(0, Math.min(100, base + b / 2 + (ua ? 10 : 0) + (headless ? 8 : 0) + (rate ? 12 : 0)
				+ (payload ? 8 : 0)));
		boolean allowed = hasKey && score < 62;

		String verdict;
		if (!hasKey) {
			verdict = "BLOCKED — no API key. The edge guard refuses anonymous traffic before any checks run. "
					+ "This is the 'security from day one' the video preaches.";
		} else if (allowed) {
			verdict = ("ALLOWED — risk score %d/100 under the 62 threshold. The request looks human and carries a "
					+ "valid key: bots, spam and scraping were the ones bounced.").formatted(score);
		} else {
			verdict = ("BLOCKED — risk score %d/100 crossed the threshold. Real-time mitigation keeps abuse out of "
					+ "your API so the 3 a.m. call stays about the bug, not the botnet.").formatted(score);
		}
		return new SecurityResult(score, allowed, checks, verdict);
	}
}