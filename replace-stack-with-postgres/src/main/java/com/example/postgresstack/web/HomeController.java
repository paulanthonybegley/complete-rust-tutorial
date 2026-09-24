package com.example.postgresstack.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    public record Feature(String title, String url, String replaces, String blurb) {}

    private static final List<Feature> FEATURES = List.of(
        new Feature("ACID + Extensibility", "/acid",
            "a distributed mess",
            "Custom data types, arrays, geometry and key/value stores all live inside the database."),
        new Feature("JSONB + GIN index", "/jsonb",
            "MongoDB / NoSQL document stores",
            "Binary JSON with a generalized inverted index for queries on deeply nested properties."),
        new Feature("Job queue via SKIP LOCKED", "/queue",
            "Redis / RabbitMQ / Kafka",
            "FOR UPDATE SKIP LOCKED turns a plain table into a wait-free concurrent queue."),
        new Feature("Full-text + trigram search", "/search",
            "Elasticsearch / Algolia",
            "tsvector, tsquery and pg_trigram give ranked, typo-tolerant search with zero extra services."),
        new Feature("pgvector + HNSW", "/vector",
            "Pinecone / vector databases",
            "Vectors live beside relational data, so semantic search can be filtered by author and date."),
        new Feature("PostGIS + GiST", "/geo",
            "GraphHopper / Mapbox / GIS servers",
            "Radius, kNN and polygon queries over geography types, accelerated by a GiST index."),
        new Feature("Partitioning + BRIN", "/timeseries",
            "InfluxDB / Prometheus / time-series DBs",
            "Declarative range partitioning plus block range indexes for fast scan on huge logs."),
        new Feature("Materialized views", "/analytics",
            "Snowflake / data warehouses",
            "Heavy dashboard aggregations are stored and refreshed concurrently without locking readers."),
        new Feature("Row Level Security", "/rls",
            "hand-written authz middleware",
            "Policies inside the database guarantee a user can only ever see and write their own rows."),
        new Feature("When NOT to use Postgres", "/caveats",
            "your critical thinking",
            "The honest limits: horizontal sharding, millions of events per second, sub-ms in-memory cache.")
    );

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("features", FEATURES);
        return "home";
    }

    @GetMapping("/acid")
    public String acid(Model model) {
        return "acid";
    }

    @GetMapping("/caveats")
    public String caveats(Model model) {
        return "caveats";
    }
}