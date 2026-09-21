package com.example.devops.service;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

/**
 * Remembers failed pods across requests while the JVM runs — and is exactly
 * the kind of in-memory state that vanishes on restart, which the course
 * points out about the api-rules API too.
 */
@Component
public class RespawnCounter {

	private final AtomicInteger respawned = new AtomicInteger(0);

	public int total() {
		return respawned.get();
	}

	public int count() {
		return respawned.incrementAndGet();
	}
}