package com.example.ebicsswift;

public record LedgerConcept(String slug, String name, String awsService, String backing, String role,
                            String dockerCompose, String apiHint) {
}
