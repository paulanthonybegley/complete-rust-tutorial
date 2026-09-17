package com.example.duckdbanalytics.model;

/** Count of log events per level — the "dark data / find the 1%" lesson. */
public record LogLevelCount(String level, Long count) {
}