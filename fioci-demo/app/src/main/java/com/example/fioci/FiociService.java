package com.example.fioci;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class FiociService {
	public Map<String, Object> describe(AwsConcept c, HttpSession session) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("slug", c.slug());
		body.put("name", c.name());
		body.put("awsService", c.awsService());
		body.put("role", c.role());
		body.put("backing", c.backing());
		body.put("mode", up(c.backing()) ? "live-compose" : "simulated");
		body.put("generatedAt", Instant.now().toString());
		visit(session, c.slug());
		return body;
	}

	public boolean up(String backing) {
		if (backing == null) return false;
		int c = backing.indexOf(':');
		if (c < 0) return false;
		int port;
		try { port = Integer.parseInt(backing.substring(c + 1)); }
		catch (NumberFormatException e) { return false; }
		long start = System.nanoTime();
		try (Socket s = new Socket()) {
			s.connect(new InetSocketAddress("localhost", port), 400);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	private void visit(HttpSession session, String slug) {
		@SuppressWarnings("unchecked")
		java.util.Set<String> set = (java.util.Set<String>) session.getAttribute("visited");
		if (set == null) { set = new java.util.LinkedHashSet<>(); }
		set.add(slug);
		session.setAttribute("visited", set);
	}

	public int done(HttpSession session) {
		@SuppressWarnings("unchecked")
		java.util.Set<String> set = (java.util.Set<String>) session.getAttribute("visited");
		return set == null ? 0 : set.size();
	}
}
