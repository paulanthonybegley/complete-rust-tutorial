package com.example.postgresstack.web;

import com.example.postgresstack.queue.QueueService;
import com.example.postgresstack.queue.QueueWorker;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
public class QueueController {

    private final QueueService queue;
    private final QueueWorker worker;
    private final NamedParameterJdbcTemplate jdbc;

    public QueueController(QueueService queue, QueueWorker worker, NamedParameterJdbcTemplate jdbc) {
        this.queue = queue;
        this.worker = worker;
        this.jdbc = jdbc;
    }

    public record StatusRow(String status, long count) {}

    public record JobRow(long id, String kind, String payload, String status, int attempts,
                         int maxAttempts, OffsetDateTime createdAt, String lastError, String result) {}

    @GetMapping("/queue")
    public String page(Model model) {
        fill(model);
        return "queue";
    }

    @GetMapping("/queue/jobs")
    public String jobs(Model model) {
        fill(model);
        return "partials/queue :: jobsView";
    }

    @PostMapping("/queue/enqueue")
    public String enqueue(@RequestParam String kind, @RequestParam String payload, Model model) {
        long id = queue.enqueue(kind, payload);
        model.addAttribute("flash", "Enqueued job #" + id + " ('" + kind + "') — a worker will claim it with SKIP LOCKED.");
        fill(model);
        return "partials/queue :: jobsView";
    }

    private void fill(Model model) {
        model.addAttribute("statuses", statusCounts());
        model.addAttribute("jobs", recentJobs());
        model.addAttribute("workers", QueueWorker.WORKERS);
        model.addAttribute("doneCount", worker.doneCount());
        model.addAttribute("failedCount", worker.failedCount());
        model.addAttribute("ratePerMin", worker.processedPerMinute());
        model.addAttribute("avgMs", worker.avgProcessedMs());
        model.addAttribute("uptime", worker.uptimeSeconds());
    }

    private List<StatusRow> statusCounts() {
        return jdbc.query("""
            SELECT status, COUNT(*) AS count FROM jobs GROUP BY status ORDER BY status
            """, new MapSqlParameterSource(), (rs, i) -> new StatusRow(
            rs.getString("status"), rs.getLong("count")));
    }

    private List<JobRow> recentJobs() {
        return jdbc.query("""
            SELECT id, kind, payload::text AS payload, status, attempts, max_attempts,
                   created_at, last_error, result
            FROM jobs
            ORDER BY id DESC
            LIMIT 30
            """, new MapSqlParameterSource(), (rs, i) -> new JobRow(
            rs.getLong("id"),
            rs.getString("kind"),
            rs.getString("payload"),
            rs.getString("status"),
            rs.getInt("attempts"),
            rs.getInt("max_attempts"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getString("last_error"),
            rs.getString("result")));
    }
}