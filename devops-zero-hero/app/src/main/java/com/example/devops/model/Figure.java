package com.example.devops.model;

/**
 * A quoted value + what it means + where it comes from — the "get the number"
 * unit used inside concept drawers.
 */
public record Figure(String value, String label, String note) {
}