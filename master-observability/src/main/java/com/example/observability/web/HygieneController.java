package com.example.observability.web;

import com.example.observability.client.LokiClient;
import com.example.observability.obs.SafeLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Observability hygiene. Two logging call sites that <em>look</em> identical:
 * <ul>
 *   <li>safe — the message passes through {@link SafeLog} first, so the line that lands in
 *       Loki shows <code>[email]</code> and <code>password=[redacted]</code></li>
 *   <li>leak — raw user data logged straight, the way real leaks happen (someone forgot)</li>
 * </ul>
 * Learners query Loki for both and see the difference with their own eyes.
 */
@Controller
public class HygieneController {

    private static final Logger log = LoggerFactory.getLogger(HygieneController.class);

    private final LokiClient loki;

    public HygieneController(LokiClient loki) {
        this.loki = loki;
    }

    @GetMapping("/hygiene")
    public String page(Model model) {
        model.addAttribute("evidence", evidence());
        return "hygiene";
    }

    @PostMapping("/hygiene/log")
    public String logLine(@RequestParam(defaultValue = "safe") String mode,
                          @RequestParam(defaultValue = "alice@example.com") String email,
                          @RequestParam(defaultValue = "Alice Adeyemi") String name,
                          @RequestParam(defaultValue = "supersecret-123") String password,
                          Model model) {
        String raw = "customer signup name=" + name + " email=" + email + " password=" + password;
        if ("leak".equals(mode)) {
            log.warn("[LEAK-DEMO] {}", raw);
            model.addAttribute("flash",
                    "Leak line written the way leaks actually happen — someone logged raw user data "
                            + "without masking it. A real email and password are now sitting in Loki, "
                            + "indistinguishable from any other log line. That is the failure mode; the "
                            + "grep below finds it by its marker, but the marker is the very thing a real "
                            + "leak would not have.");
        } else {
            log.info("{}", SafeLog.redact(raw));
            model.addAttribute("flash",
                    "Safe line written. Same call, but the message passed through obs.SafeLog before it "
                            + "reached the logger, so the email and password were redacted before the line "
                            + "left the JVM. No encoder, no pipeline, no post-processing can undo what is "
                            + "logged raw — hygiene has to happen here, at the call site.");
        }
        model.addAttribute("evidence", evidence());
        return "hygiene";
    }

    @GetMapping("/hygiene/evidence")
    public String evidence(Model model) {
        model.addAttribute("evidence", evidence());
        return "partials/hygiene :: evidence";
    }

    private Evidence evidence() {
        LokiClient.QueryResult leaks = loki.query("{service=\"app\"} |= \"LEAK-DEMO\"", 15);
        LokiClient.QueryResult safe = loki.query("{service=\"app\"} |= \"email=[email]\"", 15);
        return new Evidence(leaks, safe);
    }

    public record Evidence(LokiClient.QueryResult leaks, LokiClient.QueryResult safe) {}
}