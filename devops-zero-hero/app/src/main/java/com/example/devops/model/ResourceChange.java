package com.example.devops.model;

/**
 * One Terraform action the planner would take against the desired state.
 */
public record ResourceChange(String address, String action, String reason) {
}