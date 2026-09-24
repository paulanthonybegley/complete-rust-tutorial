package com.example.postgresstack.rls;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RlsService {

    private final NamedParameterJdbcTemplate jdbc;

    public RlsService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record User(long id, String name) {}

    public record Note(long id, String content, OffsetDateTime createdAt, String ownerName) {}

    public record AddResult(List<Note> notes, int inserted, boolean rejected) {}

    public List<User> users() {
        return jdbc.query("SELECT id, name FROM app_users ORDER BY id",
            new MapSqlParameterSource(), (rs, i) -> new User(rs.getLong("id"), rs.getString("name")));
    }

    @Transactional
    public List<Note> notesFor(long uid) {
        jdbc.getJdbcTemplate().execute("SET LOCAL app.uid = " + uid);
        return queryNotes();
    }

    @Transactional
    public AddResult addNote(long uid, String content, String ownerOverride) {
        jdbc.getJdbcTemplate().execute("SET LOCAL app.uid = " + uid);
        long ownerId = uid;
        try {
            ownerId = ownerOverride == null || ownerOverride.isBlank()
                ? uid
                : Long.parseLong(ownerOverride);
        } catch (NumberFormatException e) {
            ownerId = uid;
        }

        List<Note> notes = queryNotes();
        if (ownerId == uid) {
            int inserted = jdbc.update("""
                INSERT INTO private_notes (content, owner_id) VALUES (:content, :ownerId)
                """, new MapSqlParameterSource()
                    .addValue("content", content)
                    .addValue("ownerId", ownerId));
            return new AddResult(queryNotes(), inserted, false);
        }

        boolean rejected = true;
        try {
            jdbc.update("""
                INSERT INTO private_notes (content, owner_id) VALUES (:content, :ownerId)
                """, new MapSqlParameterSource()
                    .addValue("content", content)
                    .addValue("ownerId", ownerId));
            rejected = false;
            return new AddResult(queryNotes(), 1, false);
        } catch (RuntimeException e) {
            return new AddResult(notes, 0, rejected);
        }
    }

    private List<Note> queryNotes() {
        return jdbc.query("""
            SELECT n.id, n.content, n.created_at, u.name AS owner_name
            FROM private_notes n
            JOIN app_users u ON u.id = n.owner_id
            ORDER BY n.id DESC
            """, new MapSqlParameterSource(), (rs, i) -> new Note(
            rs.getLong("id"),
            rs.getString("content"),
            rs.getObject("created_at", OffsetDateTime.class),
            rs.getString("owner_name")));
    }
}