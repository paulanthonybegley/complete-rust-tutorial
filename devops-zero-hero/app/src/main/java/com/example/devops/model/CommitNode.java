package com.example.devops.model;

/**
 * One node in the rendered git graph.
 */
public record CommitNode(String mark, String sha, String message, String author, String when, int column) {
}