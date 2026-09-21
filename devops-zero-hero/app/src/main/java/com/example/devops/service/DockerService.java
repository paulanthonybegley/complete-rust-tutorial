package com.example.devops.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.DockerResult;
import com.example.devops.model.LayerState;

/**
 * Docker layer-cache simulator for the multi-stage api-rules image. A change
 * tells you which layers rebuild and how long the build takes — the 
 * 'bake the dough into bread' lesson, visualized per layer. The base tags
 * deliberately match deploy/Dockerfile.api-rules so the lab and the real
 * image tell the same story.
 */
@Service
public class DockerService {

	public DockerResult build(String change, boolean noCache) {
		String what = change == null || change.isBlank() ? "src" : change.toLowerCase();
		boolean base = what.equals("base");
		boolean pom = what.equals("pom");

		List<LayerState> layers = new ArrayList<>();
		add(layers, "FROM eclipse-temurin:17-jre · runtime base", 9, noCache || base);
		add(layers, "FROM maven:3.9-eclipse-temurin-17 · builder image", 55, noCache || base);
		add(layers, "COPY pom.xml + RUN go-offline · dependencies", 26, noCache || base || pom);
		add(layers, "COPY src + RUN mvn package · the jar", 14, true);
		add(layers, "COPY jar + USER app + HEALTHCHECK + EXPOSE 8080", 3, true);

		int seconds = layers.stream().filter(LayerState::rebuilt).mapToInt(LayerState::seconds).sum();
		int cached = (int) layers.stream().filter(LayerState::cached).count();

		String verdict = switch (what) {
			case "base" -> "You changed the runtime base image — both stages inherit from it, so every layer "
					+ "below it rebuilds. Whole image from scratch.";
			case "pom" -> "You touched pom.xml — the go-offline layer sees a new checksum, so dependencies, the "
					+ "package layer and the runtime COPY rebuild, but the two base images stay cached.";
			default -> "You changed a source file — the dependency layer still matches its checksum, so only the "
					+ "jar and the runtime COPY rebuild.";
		};
		if (noCache) {
			verdict = "You forced a cold build (--no-cache): the cache is ignored and every layer is rebuilt from "
					+ "scratch. Immune to stale cache bugs, but slow.";
		}
		if (cached > 0) {
			verdict += " " + cached + " layer" + (cached == 1 ? "" : "s") + " reused from cache.";
		}
		return new DockerResult(layers, seconds, verdict);
	}

	private void add(List<LayerState> layers, String name, int seconds, boolean rebuild) {
		layers.add(new LayerState(name, !rebuild, seconds));
	}
}