package com.example.postgresstack.web;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class JsonbController {

    private final NamedParameterJdbcTemplate jdbc;

    public JsonbController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Product(long id, String name, String category, BigDecimal price, String attributes) {
        public String prettyJson() {
            return attributes == null ? "{}" : attributes;
        }
    }

    public record IndexInfo(String indexName, String indexDef) {}

    @GetMapping("/jsonb")
    public String page(Model model) {
        SearchOutcome out = run("", "text");
        model.addAttribute("products", out.products());
        model.addAttribute("notice", out.notice());
        model.addAttribute("requested", "");
        model.addAttribute("mode", "text");
        model.addAttribute("indexes", listIndexes());
        return "jsonb";
    }

    @GetMapping("/jsonb/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "text") String mode,
                         Model model) {
        SearchOutcome out = run(q, mode);
        model.addAttribute("products", out.products());
        model.addAttribute("notice", out.notice());
        model.addAttribute("requested", q);
        model.addAttribute("mode", mode);
        return "partials/jsonb :: products";
    }

    @PostMapping("/jsonb/products")
    public String add(@RequestParam String name,
                      @RequestParam String category,
                      @RequestParam BigDecimal price,
                      @RequestParam String attributes,
                      Model model) {
        try {
            jdbc.update("""
                INSERT INTO products (name, category, price, attributes)
                VALUES (:name, :category, :price, CAST(:attributes AS jsonb))
                """, new MapSqlParameterSource()
                    .addValue("name", name)
                    .addValue("category", category)
                    .addValue("price", price)
                    .addValue("attributes", attributes.isEmpty() ? "{}" : attributes));
            model.addAttribute("flash", "Inserted '" + name + "' as a single JSONB row.");
        } catch (Exception e) {
            model.addAttribute("error", "Insert failed: " + e.getMessage());
        }
        model.addAttribute("products", run("", "text").products());
        return "partials/jsonb :: products";
    }

    public record SearchOutcome(List<Product> products, String notice) {}

    private SearchOutcome run(String q, String mode) {
        if (q != null && !q.isBlank() && !mode.equals("text")) {
            try {
                return new SearchOutcome(search(q, mode), null);
            } catch (DataAccessException e) {
                return new SearchOutcome(List.of(), warning(q, mode));
            }
        }
        return new SearchOutcome(search(q, mode), null);
    }

    private String warning(String q, String mode) {
        String what = mode.equals("json") ? "JSON" : "JSONPath";
        return "Couldn't parse \"" + q + "\" as " + what + " yet. "
            + "Keep typing until it's valid — JSON needs balanced braces like {\"os\":\"Android\"}, "
            + "JSONPath needs its $ anchor, e.g. $.specs.ports[*] ? (@ == \"HDMI\") — then the next keystroke will search.";
    }

    private List<Product> search(String q, String mode) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("q", q).addValue("mode", mode);
        String sql = """
            SELECT id, name, category, price, attributes::text AS attributes
            FROM products
            WHERE (:q = '') OR
                  (CASE :mode
                     WHEN 'json' THEN attributes @> CAST(:q AS jsonb)
                     WHEN 'path' THEN jsonb_path_exists(attributes, CAST(:q AS jsonpath))
                     ELSE name ILIKE '%'||:q||'%' OR category ILIKE '%'||:q||'%' OR attributes::text ILIKE '%'||:q||'%'
                   END)
            ORDER BY id DESC
            LIMIT 100
            """;
        return jdbc.query(sql, params, (rs, i) -> new Product(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getBigDecimal("price"),
            rs.getString("attributes")));
    }

    private List<IndexInfo> listIndexes() {
        return jdbc.query("""
            SELECT indexname, indexdef FROM pg_indexes
            WHERE tablename = 'products'
            """, new MapSqlParameterSource(), (rs, i) -> new IndexInfo(
            rs.getString("indexname"),
            rs.getString("indexdef")));
    }
}