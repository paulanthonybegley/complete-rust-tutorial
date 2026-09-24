package com.example.postgresstack.web;

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
        model.addAttribute("products", search("", "text"));
        model.addAttribute("requested", "");
        model.addAttribute("mode", "text");
        model.addAttribute("indexes", listIndexes());
        return "jsonb";
    }

    @GetMapping("/jsonb/search")
    public String search(@RequestParam(defaultValue = "") String q,
                         @RequestParam(defaultValue = "text") String mode,
                         Model model) {
        model.addAttribute("products", search(q, mode));
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
        model.addAttribute("products", search("", "text"));
        return "partials/jsonb :: products";
    }

    private List<Product> search(String q, String mode) {
        MapSqlParameterSource params = new MapSqlParameterSource().addValue("q", q).addValue("mode", mode);
        String sql = """
            SELECT id, name, category, price, attributes::text AS attributes
            FROM products
            WHERE (:q = '') OR
                  (CASE :mode
                     WHEN 'json' THEN attributes @> CAST(:q AS jsonb)
                     WHEN 'path' THEN attributes @? CAST(:q AS jsonpath)
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