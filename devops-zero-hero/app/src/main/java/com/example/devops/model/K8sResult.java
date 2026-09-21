package com.example.devops.model;

import java.util.List;

/**
 * State of the simulated Kubernetes cluster after a reconcile.
 */
public record K8sResult(List<PodState> pods, int desired, int ready, int respawned, boolean rolling, String verdict) {
}