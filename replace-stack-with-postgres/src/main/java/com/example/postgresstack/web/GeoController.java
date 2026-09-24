package com.example.postgresstack.web;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class GeoController {

    private final NamedParameterJdbcTemplate jdbc;

    public GeoController(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Shop(long id, String name, String chain, BigDecimal rating, BigDecimal distanceKm,
                       BigDecimal lat, BigDecimal lng) {}

    private static final Map<String, String> AREAS = new LinkedHashMap<>(Map.of(
        "soho", "POLYGON((-0.1400 51.5100, -0.1300 51.5100, -0.1300 51.5180, -0.1400 51.5180, -0.1400 51.5100))",
        "southbank", "POLYGON((-0.1300 51.5000, -0.0900 51.5000, -0.0900 51.5080, -0.1300 51.5080, -0.1300 51.5000))",
        "kings_cross", "POLYGON((-0.1310 51.5280, -0.1200 51.5280, -0.1200 51.5360, -0.1310 51.5360, -0.1310 51.5280))"));

    @GetMapping("/geo")
    public String page(Model model) {
        model.addAttribute("view", near("51.5074", "-0.1278", "4", model));
        model.addAttribute("areas", AREAS);
        return "geo";
    }

    @GetMapping("/geo/near")
    public String near(@RequestParam(defaultValue = "51.5074") String lat,
                       @RequestParam(defaultValue = "-0.1278") String lng,
                       @RequestParam(defaultValue = "4") String maxKm,
                       Model model) {
        MapSqlParameterSource p = new MapSqlParameterSource()
            .addValue("lat", Double.parseDouble(lat))
            .addValue("lng", Double.parseDouble(lng))
            .addValue("radius", (int) (Double.parseDouble(maxKm) * 1000));

        List<Shop> shops = jdbc.query("""
            SELECT id, name, chain, rating,
                   round((ST_Distance(location, ST_MakePoint(:lng, :lat)::geography) / 1000)::numeric, 2) AS distance_km,
                   ST_Y(location::geometry) AS lat,
                   ST_X(location::geometry) AS lng
            FROM coffee_shops
            WHERE ST_DWithin(location, ST_MakePoint(:lng, :lat)::geography, :radius)
            ORDER BY location <-> ST_MakePoint(:lng, :lat)::geography
            LIMIT 50
            """, p, (rs, i) -> new Shop(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("chain"),
            rs.getBigDecimal("rating"),
            rs.getBigDecimal("distance_km"),
            rs.getBigDecimal("lat"),
            rs.getBigDecimal("lng")));

        long total = shops.size();
        model.addAttribute("shops", shops);
        model.addAttribute("total", total);
        model.addAttribute("lat", lat);
        model.addAttribute("lng", lng);
        model.addAttribute("maxKm", maxKm);
        model.addAttribute("isAreaResult", false);
        return "partials/geo :: nearResults";
    }

    @GetMapping("/geo/area")
    public String area(@RequestParam String area, Model model) {
        String wkt = AREAS.getOrDefault(area, AREAS.get("soho"));
        List<Shop> shops = jdbc.query("""
            SELECT id, name, chain, rating,
                   ST_Y(location::geometry) AS lat, ST_X(location::geometry) AS lng,
                   0 AS distance_km
            FROM coffee_shops
            WHERE ST_Within(location::geometry, ST_GeomFromText(:wkt, 4326))
            ORDER BY rating DESC NULLS LAST
            """, new MapSqlParameterSource("wkt", wkt), (rs, i) -> new Shop(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("chain"),
            rs.getBigDecimal("rating"),
            BigDecimal.ZERO,
            rs.getBigDecimal("lat"),
            rs.getBigDecimal("lng")));

        model.addAttribute("shops", shops);
        model.addAttribute("total", shops.size());
        model.addAttribute("wkt", wkt);
        model.addAttribute("areaName", area);
        model.addAttribute("isAreaResult", true);
        return "partials/geo :: areaResults";
    }
}