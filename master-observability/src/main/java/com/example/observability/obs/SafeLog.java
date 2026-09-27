package com.example.observability.obs;

import java.util.regex.Pattern;

/**
 * The observability-hygiene lesson made visible in code. EVERY log line the shop writes
 * is routed through {@link #redact(String)}, so passwords, credit card numbers and emails
 * never reach Loki. The logback appender applies the same redaction a second time
 * (defense-in-depth) via the {@link MaskedMessageProvider}.
 */
public final class SafeLog {

    private static final Pattern[] PATTERNS = {
        Pattern.compile("(?i)\\b([A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})\\b"),
        Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b"),
        Pattern.compile("(?i)\\b(pass(word)?|secret|token|api[_-]?key|authorization)\\b\\s*[:=]\\s*\\S+"),
        Pattern.compile("(?i)bearer\\s+[A-Za-z0-9._~+/=-]{8,}")
    };

    private static final String[] REPLACEMENTS = {
        "[email]",
        "[card]",
        "$1=[redacted]",
        "bearer [redacted]"
    };

    private SafeLog() {}

    public static String redact(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        String redacted = message;
        for (int i = 0; i < PATTERNS.length; i++) {
            redacted = PATTERNS[i].matcher(redacted).replaceAll(REPLACEMENTS[i]);
        }
        return redacted;
    }
}