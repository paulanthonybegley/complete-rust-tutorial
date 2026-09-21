package com.example.devops.model;

/**
 * One pod in the orchestrator sim.
 */
public record PodState(String name, String phase) {
}