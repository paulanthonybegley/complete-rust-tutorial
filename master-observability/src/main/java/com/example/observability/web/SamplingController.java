package com.example.observability.web;

import com.example.observability.domain.ShopService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The "observability at scale" lab. The collector can't store 1M traces a minute; the
 * /sampling page mints a burst of synthetic requests, records what the app-side policy
 * decided for each (error / slow / vip / random), and compares it with what tail_sampling
 * actually persisted in Jaeger. Teaches: sample <em>smartly</em>, not simply "less".
 */
@Controller
public class SamplingController {

    private static final Random RANDOM = new Random();
    private final ShopService shop;
    private final JdbcTemplate jdbc;
    private final ExecutorService pool = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "sampling-lab");
        t.setDaemon(true);
        return t;
    });

    public SamplingController(ShopService shop, JdbcTemplate jdbc) {
        this.shop = shop;
        this.jdbc = jdbc;
    }

    @GetMapping("/sampling")
    public String page() {
        return "sampling";
    }

    @PostMapping("/sampling/generate")
    public String generate(@RequestParam(defaultValue = "mixed") String mix,
                           @RequestParam(defaultValue = "40") int n,
                           Model model) {
        int count = Math.min(n, 100);
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        List<ShopService.SamplingResult> results = java.util.Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < count; i++) {
            tasks.add(CompletableFuture.runAsync(() -> {
                long durationMs = switch (mix) {
                    case "slow" -> 350 + RANDOM.nextInt(400);
                    case "clean" -> 10 + RANDOM.nextInt(80);
                    default -> RANDOM.nextInt(700);
                };
                boolean fail = switch (mix) {
                    case "fail" -> RANDOM.nextInt(10) < 6;
                    case "clean" -> false;
                    default -> RANDOM.nextInt(10) < 3;
                };
                boolean vip = RANDOM.nextInt(10) < 2;
                ShopService.SamplingResult result = shop.probe("checkout", durationMs, fail, vip);
                recordDecision(result);
                results.add(result);
            }, pool));
        }
        for (CompletableFuture<Void> task : tasks) {
            task.join();
        }

        results.sort((a, b) -> Long.compare(b.durationMs(), a.durationMs()));
        model.addAttribute("results", results);
        model.addAttribute("n", count);
        model.addAttribute("mix", mix);
        model.addAttribute("kept", results.stream().filter(ShopService.SamplingResult::kept).count());
        model.addAttribute("jaegerUrl", "http://localhost:16686/search?service=observability-shop");
        return "partials/sampling :: generated";
    }

    @GetMapping("/sampling/stats")
    public String stats(Model model) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT reason, kept, count(*) AS total FROM trace_decisions "
                        + "WHERE created_at > now() - interval '15 minutes' "
                        + "GROUP BY reason, kept ORDER BY total DESC");
        Long total = jdbc.queryForObject(
                "SELECT count(*) FROM trace_decisions WHERE created_at > now() - interval '15 minutes'",
                Long.class);
        Long dropped = jdbc.queryForObject(
                "SELECT count(*) FROM trace_decisions WHERE kept = false AND created_at > now() - interval '15 minutes'",
                Long.class);
        model.addAttribute("rows", rows);
        model.addAttribute("total", total == null ? 0 : total);
        model.addAttribute("dropped", dropped == null ? 0 : dropped);
        return "partials/sampling :: stats";
    }

    private void recordDecision(ShopService.SamplingResult result) {
        jdbc.update("INSERT INTO trace_decisions (trace_id, endpoint, kept, reason, duration_ms) "
                        + "VALUES (?, ?, ?, ?, ?)",
                result.traceId(), result.endpoint(), result.kept(), result.reason(), result.durationMs());
    }
}