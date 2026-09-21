package com.example.devops.model;

import java.util.List;

/**
 * The outcome of one simulated pipeline run.
 */
public record PipelineResult(List<PipelineStage> stages, boolean succeeded, String verdict) {

	public long passed() {
		return stages.stream().filter(PipelineStage::passed).count();
	}

	public long failed() {
		return stages.stream().filter(s -> !s.passed() && !s.skipped()).count();
	}

	public long skipped() {
		return stages.stream().filter(PipelineStage::skipped).count();
	}
}