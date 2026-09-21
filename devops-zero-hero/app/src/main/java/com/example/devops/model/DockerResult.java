package com.example.devops.model;

import java.util.List;

/**
 * What a Docker build of the api-rules image would do with a given change:
 * which layers are cached and which must rebuild.
 */
public record DockerResult(List<LayerState> layers, int totalSeconds, String verdict) {
}