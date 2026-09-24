package com.example.fioci;

public record AwsConcept(String slug, String name, String awsService, String backing, String role,
                         String dockerCompose, String apiHint) {
}
