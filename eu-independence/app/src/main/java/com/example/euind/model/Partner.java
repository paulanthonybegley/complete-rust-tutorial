package com.example.euind.model;

/**
 * A country in the /partners explorer.
 */
public record Partner(String country, String flag, PartnerKind kind, String status, String note) {
}