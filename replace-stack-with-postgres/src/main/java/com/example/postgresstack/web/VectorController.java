package com.example.postgresstack.web;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class VectorController {

    private final NamedParameterJdbcTemplate jdbc;

    public VectorController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Doc(long id, String title, String author, String tag,
                      LocalDate created, double similarity) {}

    public record FilterOptions(List<String> tags, List<String> authors) {}

    @GetMapping("/vector")
    public String page(Model model) {
        model.addAttribute("view", search("", "", "", 0, model));
        return "vector";
    }

    @GetMapping("/vector/results")
    public String results(@RequestParam(defaultValue = "") String q,
                          @RequestParam(defaultValue = "") String tag,
                          @RequestParam(defaultValue = "") String author,
                          @RequestParam(defaultValue = "0") int days,
                          Model model) {
        model.addAttribute("view", search(q, tag, author, days, model));
        return "partials/vector :: results";
    }

    private void search(String q, String tag, String author, int days, Model model) {
        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("q", q)
            .addValue("tag", tag)
            .addValue("author", author)
            .addValue("days", days);

        List<Doc> docs;
        String queryEmbedding = "";
        if (q.isBlank()) {
            docs = jdbc.query("""
                SELECT id, title, author, tag, created_at, 0 AS similarity
                FROM documents
                WHERE (:tag = '' OR tag = :tag)
                  AND (:author = '' OR author = :author)
                  AND (:days = 0 OR created_at >= CURRENT_DATE - :days)
                ORDER BY created_at DESC
                LIMIT 12
                """, p, (rs, i) -> toDoc(rs));
        } else {
            queryEmbedding = jdbc.queryForObject("SELECT text_embedding(:q)::text", p, String.class);
            docs = jdbc.query("""
                WITH query AS (SELECT text_embedding(:q)::vector AS e)
                SELECT d.id, d.title, d.author, d.tag, d.created_at,
                       1 - (d.embedding <=> (SELECT e FROM query)) AS similarity
                FROM documents d
                WHERE (:tag = '' OR d.tag = :tag)
                  AND (:author = '' OR d.author = :author)
                  AND (:days = 0 OR d.created_at >= CURRENT_DATE - :days)
                ORDER BY d.embedding <=> (SELECT e FROM query)
                LIMIT 12
                """, p, (rs, i) -> toDoc(rs));
        }

        model.addAttribute("filters", new FilterOptions(
            jdbc.queryForList("SELECT DISTINCT tag FROM documents ORDER BY tag", String.class),
            jdbc.queryForList("SELECT DISTINCT author FROM documents ORDER BY author", String.class)));
        model.addAttribute("q", q);
        model.addAttribute("tag", tag);
        model.addAttribute("author", author);
        model.addAttribute("days", days);
        model.addAttribute("docs", docs);
        model.addAttribute("queryEmbedding", preview(queryEmbedding));
    }

    private Doc toDoc(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Doc(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("tag"),
            rs.getObject("created_at", LocalDate.class),
            rs.getDouble("similarity"));
    }

    private String preview(String vectorText) {
        if (vectorText == null || vectorText.isBlank()) return "";
        String[] dims = vectorText.replaceFirst("^\\[", "").replaceFirst("\\]$", "").split(",");
        StringBuilder sb = new StringBuilder("[ ");
        int n = Math.min(8, dims.length);
        for (int i = 0; i < n; i++) {
            if (i > 0) sb.append(", ");
            sb.append(dims[i].trim());
        }
        sb.append(n < dims.length ? ", ... " : " ").append("] (dim=").append(dims.length).append(")");
        return sb.toString();
    }
}