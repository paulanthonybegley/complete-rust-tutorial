package com.example.duckdbanalytics.config;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Wires one embedded DuckDB engine to the whole application.
 *
 * DuckDB is not a server: there is nothing to connect to over a port, and in
 * memory mode every <em>new</em> JDBC connection is a brand-new empty database.
 * That is why a {@link SingleConnectionDataSource} is used instead of a pool:
 * the app shares <em>one</em> live connection (and therefore one engine and one
 * database) for its whole life. This mirrors the talk's "it is just a file you
 * open" model and keeps writes visible to every query.
 */
@Configuration
public class DuckDbConfig {

	static final String JDBC_PREFIX = "jdbc:duckdb:";

	@Bean
	@Primary
	DataSource duckDbDataSource(@Value("${duckdb.url}") String url) {
		ensureParentDirectory(url);
		return new SingleConnectionDataSource(url, true);
	}

	private static void ensureParentDirectory(String url) {
		String target = url.substring(JDBC_PREFIX.length());
		if (target.isBlank() || target.startsWith(":")) {
			return; // in-memory engine
		}
		String directory = target.contains("?") ? target.substring(0, target.indexOf('?')) : target;
		Path parent = Path.of(directory).toAbsolutePath().normalize().getParent();
		try {
			Files.createDirectories(parent);
		} catch (Exception ex) {
			throw new IllegalStateException("Cannot create DuckDB data directory " + parent, ex);
		}
	}
}