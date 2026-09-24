package com.example.postgresstack.queue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class QueueService {

    private final NamedParameterJdbcTemplate jdbc;
    private final ObjectMapper mapper = new ObjectMapper();

    public QueueService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Job(long id, String kind, String payload, int attempts, int maxAttempts) {}

    public long enqueue(String kind, String payloadJson) {
        Long id = jdbc.queryForObject("""
            INSERT INTO jobs (kind, payload)
            VALUES (:kind, CAST(:payload AS jsonb))
            RETURNING id
            """, new MapSqlParameterSource()
                .addValue("kind", kind)
                .addValue("payload", payloadJson == null || payloadJson.isBlank() ? "{}" : payloadJson),
            Long.class);
        return id == null ? -1 : id;
    }

    public Optional<Job> claim() {
        return Optional.ofNullable(jdbc.query("""
            WITH claimed AS (
              SELECT id FROM jobs
              WHERE status = 'pending'
              ORDER BY created_at
              LIMIT 1
              FOR UPDATE SKIP LOCKED
            )
            UPDATE jobs SET status = 'running', started_at = now(), attempts = attempts + 1
            FROM claimed
            WHERE jobs.id = claimed.id
            RETURNING jobs.id, jobs.kind, jobs.payload::text, jobs.attempts, jobs.max_attempts
            """, new MapSqlParameterSource(), rs -> {
                if (!rs.next()) return null;
                return new Job(rs.getLong("id"), rs.getString("kind"), rs.getString("payload"),
                    rs.getInt("attempts"), rs.getInt("max_attempts"));
            }));
    }

    public void complete(long id, String result) {
        jdbc.update("""
            UPDATE jobs SET status = 'done', finished_at = now(), result = :result
            WHERE id = :id
            """, new MapSqlParameterSource().addValue("id", id).addValue("result", result));
    }

    public void fail(long id, int attempts, int maxAttempts, String message) {
        MapSqlParameterSource params = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("message", message);
        if (attempts >= maxAttempts) {
            jdbc.update("""
                UPDATE jobs SET status = 'failed', last_error = :message, finished_at = now()
                WHERE id = :id
                """, params);
        } else {
            jdbc.update("""
                UPDATE jobs SET status = 'pending', last_error = :message, started_at = NULL
                WHERE id = :id
                """, params);
        }
    }

    public boolean wantsToFail(String payload) {
        try {
            JsonNode node = mapper.readTree(payload == null ? "{}" : payload);
            return node.path("fail").asBoolean(false);
        } catch (Exception e) {
            return false;
        }
    }

    public String describe(Job job) {
        try {
            JsonNode node = mapper.readTree(job.payload() == null ? "{}" : job.payload());
            return switch (job.kind()) {
                case "email" -> "email to " + node.path("to").asText("<unknown>") + " re: " + node.path("subject").asText("");
                case "report" -> node.path("format").asText("csv") + " report with " + node.path("rows").asLong(0) + " rows";
                case "thumbnail" -> "thumbnail for image_" + node.path("image_id").asLong(0);
                default -> node.toString();
            };
        } catch (Exception e) {
            return job.payload();
        }
    }
}