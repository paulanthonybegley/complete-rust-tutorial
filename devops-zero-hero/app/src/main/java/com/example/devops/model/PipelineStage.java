package com.example.devops.model;

/**
 * One stage of a CI/CD run, as rendered after a pipeline attempt. Skipped
 * stages are the ones the pipeline never reached once it hit a failure.
 */
public record PipelineStage(String name, boolean passed, boolean skipped, String note) {
}