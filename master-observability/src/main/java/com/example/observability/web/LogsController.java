package com.example.observability.web;

import com.example.observability.client.LokiClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LogsController {

    private final LokiClient loki;

    public LogsController(LokiClient loki) {
        this.loki = loki;
    }

    @GetMapping("/logs")
    public String page(Model model) {
        model.addAttribute("query", "");
        model.addAttribute("level", "any");
        model.addAttribute("logq", "{service=\"app\"} — every line this app has written");
        model.addAttribute("result", loki.query("{service=\"app\"}", 40));
        return "logs";
    }

    @GetMapping("/logs/fragment")
    public String fragment(@RequestParam(defaultValue = "") String query,
                           @RequestParam(defaultValue = "any") String level,
                           Model model) {
        String logQl = buildQuery(query, level);
        model.addAttribute("query", query);
        model.addAttribute("level", level);
        model.addAttribute("logq", logQl);
        model.addAttribute("result", loki.query(logQl, 50));
        return "partials/logs :: entries";
    }

    private static String buildQuery(String query, String level) {
        StringBuilder q = new StringBuilder("{service=\"app\"");
        if (!"any".equals(level) && !level.isEmpty()) {
            q.append(",level=\"").append(level).append("\"");
        }
        q.append("}");
        if (!query.isEmpty()) {
            q.append(" |= \"").append(query.replace("\"", "\\\"")).append("\"");
        }
        return q.toString();
    }
}