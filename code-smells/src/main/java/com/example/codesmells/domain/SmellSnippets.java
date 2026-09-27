package com.example.codesmells.domain;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Loads the smelly/refactored snippet pairs out of src/main/resources/snippets. */
@Component
public class SmellSnippets {

    public String load(String slug, boolean after) {
        String path = "/snippets/%s/%s.java".formatted(slug, after ? "After" : "Before");
        try {
            return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Missing snippet resource " + path, e);
        }
    }

    public String before(String slug) {
        return load(slug, false);
    }

    public String after(String slug) {
        return load(slug, true);
    }
}