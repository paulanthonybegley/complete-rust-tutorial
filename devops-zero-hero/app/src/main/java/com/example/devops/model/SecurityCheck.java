package com.example.devops.model;

/**
 * One rule the edge guard evaluated for an incoming request.
 */
public record SecurityCheck(String name, boolean flagged, String detail) {
}