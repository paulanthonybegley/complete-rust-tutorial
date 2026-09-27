package com.example.observability.web;

import com.example.observability.client.JaegerClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TracesController {

    private final JaegerClient jaeger;

    public TracesController(JaegerClient jaeger) {
        this.jaeger = jaeger;
    }

    @GetMapping("/traces")
    public String list(Model model) {
        model.addAttribute("traces", jaeger.recent(40));
        model.addAttribute("reachable", jaeger.reachable());
        model.addAttribute("jaegerSearchUrl", "http://localhost:16686/search");
        return "traces";
    }

    @GetMapping("/traces/list")
    public String listFragment(Model model) {
        model.addAttribute("traces", jaeger.recent(40));
        model.addAttribute("reachable", jaeger.reachable());
        return "partials/traces :: list";
    }

    @GetMapping("/traces/{traceId}")
    public String detail(@PathVariable String traceId, Model model) {
        JaegerClient.TraceInfo trace = jaeger.byId(traceId);
        model.addAttribute("trace", trace);
        model.addAttribute("traceId", traceId);
        model.addAttribute("spans", trace == null ? java.util.List.of()
                : JaegerClient.dfs(trace.spans()));
        model.addAttribute("jaegerUrl", "http://localhost:16686/trace/" + traceId);
        if (trace != null) {
            model.addAttribute("logQuery", "{service=\"app\"} |= \"" + traceId + "\"");
        }
        return "traces/detail";
    }
}