package com.example.devops.model;

import java.util.List;

/**
 * Verdict of the bot/spam/abuse guard for one request.
 */
public record SecurityResult(int score, boolean allowed, List<SecurityCheck> checks, String verdict) {
}