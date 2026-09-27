package com.example.codesmells.domain;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/** Persistent record of every quiz attempt, so the scoreboard is real evidence. */
@Repository
public class AttemptStore {

    private final JdbcTemplate jdbc;

    public AttemptStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record AttemptView(String lab, String player, String identifyPick, String refactorPick,
                              boolean identifyCorrect, boolean refactorCorrect, Timestamp createdAt) {}

    public record PlayerRow(String player, int attempts, int identifyOk, int bothOk) {}

    public record LabStats(String lab, int attempts, int identifyPct, int refactorPct) {}

    public void insert(String lab, String player, String identifyPick, String refactorPick,
                       boolean identifyCorrect, boolean refactorCorrect) {
        try {
            jdbc.update("""
                    INSERT INTO smell_attempts
                        (lab, player, identify_pick, refactor_pick, identify_correct, refactor_correct)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, lab, player, identifyPick, refactorPick, identifyCorrect, refactorCorrect);
        } catch (DataAccessException e) {
            // The labs still work without the database; the scoreboard shows the gap.
        }
    }

    public List<AttemptView> recent(int limit) {
        try {
            return jdbc.query("""
                    SELECT lab, player, identify_pick, refactor_pick, identify_correct,
                           refactor_correct, created_at
                    FROM smell_attempts
                    ORDER BY id DESC
                    LIMIT ?
                    """, (rs, i) -> new AttemptView(
                    rs.getString("lab"), rs.getString("player"),
                    rs.getString("identify_pick"), rs.getString("refactor_pick"),
                    rs.getBoolean("identify_correct"), rs.getBoolean("refactor_correct"),
                    rs.getTimestamp("created_at")), limit);
        } catch (DataAccessException e) {
            return List.of();
        }
    }

    public List<PlayerRow> leaderboard() {
        try {
            return jdbc.query("""
                    SELECT player,
                           COUNT(*) AS attempts,
                           COUNT(*) FILTER (WHERE identify_correct) AS identify_ok,
                           COUNT(*) FILTER (WHERE identify_correct AND refactor_correct) AS both_ok
                    FROM smell_attempts
                    GROUP BY player
                    ORDER BY both_ok DESC, identify_ok DESC, player
                    LIMIT 10
                    """, (rs, i) -> new PlayerRow(
                    rs.getString("player"), rs.getInt("attempts"),
                    rs.getInt("identify_ok"), rs.getInt("both_ok")));
        } catch (DataAccessException e) {
            return List.of();
        }
    }

    public List<LabStats> labStats() {
        try {
            return jdbc.query("""
                    SELECT lab,
                           COUNT(*) AS attempts,
                           ROUND(100.0 * COUNT(*) FILTER (WHERE identify_correct) / COUNT(*)) AS identify_pct,
                           ROUND(100.0 * COUNT(*) FILTER (WHERE refactor_correct) / COUNT(*)) AS refactor_pct
                    FROM smell_attempts
                    GROUP BY lab
                    ORDER BY lab
                    """, (rs, i) -> new LabStats(
                    rs.getString("lab"), rs.getInt("attempts"),
                    rs.getInt("identify_pct"), rs.getInt("refactor_pct")));
        } catch (DataAccessException e) {
            return List.of();
        }
    }
}