package com.example.postgresstack.web;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;

@Controller
public class SearchController {

    private final NamedParameterJdbcTemplate jdbc;

    public SearchController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Result(long id, String title, String author, String tag,
                         OffsetDateTime publishedAt, double rank, String snippet) {}

    public record Suggestion(String title, String author, double similarity) {}

    public record SearchView(List<Result> results, List<Suggestion> suggestions, long hitCount,
                             String note, String q, boolean fuzzy) {}

    @GetMapping("/search")
    public String page(Model model) {
        model.addAttribute("view", run("", false));
        return "search";
    }

    @GetMapping("/search/results")
    public String results(@RequestParam(defaultValue = "") String q,
                          @RequestParam(defaultValue = "false") boolean fuzzy,
                          Model model) {
        model.addAttribute("view", run(q, fuzzy));
        return "partials/search :: results";
    }

    private SearchView run(String q, boolean fuzzy) {
        List<Result> fts = fuzzy ? fullText(q) : fullText(q);
        List<Suggestion> suggestions = fuzzy ? suggestions(q) : List.of();
        String note = "";
        List<Result> results = fts;

        if (q.isBlank()) {
            results = latest();
            note = "type something to run tsvector full-text search (across title + body).";
        } else if (fts.isEmpty() && fuzzy) {
            results = trigram(q);
            if (!results.isEmpty()) {
                note = "Full-text produced zero hits, so pg_trigram fuzzy matching stepped in and still found results.";
            }
        } else if (fts.isEmpty()) {
            note = "No full-text hits. Enable fuzzy matching to let pg_trigram tolerate typos.";
        }

        return new SearchView(results, suggestions, results.size(), note, q, fuzzy);
    }

    private List<Result> fullText(String q) {
        if (q.isBlank()) return List.of();
        return jdbc.query("""
            SELECT id, title, author, tag, published_at,
                   ts_rank(search_vector, websearch_to_tsquery('english', :q)) AS rank,
                   ts_headline('english', body, websearch_to_tsquery('english', :q),
                               'StartSel=<mark>, StopSel=</mark>, MaxFragments=2, MaxWords=25') AS snippet
            FROM articles
            WHERE search_vector @@ websearch_to_tsquery('english', :q)
            ORDER BY rank DESC, published_at DESC
            LIMIT 25
            """, new MapSqlParameterSource("q", q), (rs, i) -> new Result(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("tag"),
            rs.getObject("published_at", OffsetDateTime.class),
            rs.getDouble("rank"),
            rs.getString("snippet")));
    }

    private List<Result> trigram(String q) {
        return jdbc.query("""
            SELECT id, title, author, tag, published_at,
                   greatest(word_similarity(:q, title), word_similarity(:q, body)) AS rank,
                   substring(body, 1, 180) AS snippet
            FROM articles
            WHERE word_similarity(:q, title) > :min OR word_similarity(:q, body) > :min
            ORDER BY rank DESC
            LIMIT 25
            """, new MapSqlParameterSource()
            .addValue("q", q)
            .addValue("min", 0.4), (rs, i) -> new Result(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("tag"),
            rs.getObject("published_at", OffsetDateTime.class),
            rs.getDouble("rank"),
            rs.getString("snippet")));
    }

    private List<Suggestion> suggestions(String q) {
        if (q.isBlank()) return List.of();
        return jdbc.query("""
            SELECT title, author, word_similarity(:q, title) AS sim
            FROM articles
            WHERE word_similarity(:q, title) > :min
            ORDER BY sim DESC
            LIMIT 6
            """, new MapSqlParameterSource()
            .addValue("q", q)
            .addValue("min", 0.4), (rs, i) -> new Suggestion(
            rs.getString("title"),
            rs.getString("author"),
            rs.getDouble("sim")));
    }

    private List<Result> latest() {
        return jdbc.query("""
            SELECT id, title, author, tag, published_at, 0 AS rank,
                   substring(body, 1, 160) AS snippet
            FROM articles
            ORDER BY published_at DESC
            LIMIT 10
            """, new MapSqlParameterSource(), (rs, i) -> new Result(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("tag"),
            rs.getObject("published_at", OffsetDateTime.class),
            rs.getDouble("rank"),
            rs.getString("snippet")));
    }
}