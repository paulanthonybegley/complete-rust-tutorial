package com.example.duckdbanalytics.config;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

/**
 * Seeds the course database on every start. The script
 * ({@code seed/seed.sql}) is idempotent — tables use {@code IF NOT EXISTS} and
 * each insert is guarded by a row-count check — so running it against an
 * already-seeded file costs a few quick counts and changes nothing.
 *
 * The same file is mounted into the Docker DuckDB container
 * ({@code docker-compose.yml} → {@code /init/seed.sql}), so the dataset is
 * identical no matter which side populates it first.
 */
@Component
public class DataInitializer implements ApplicationRunner {

	private final DataSource dataSource;

	public DataInitializer(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void run(ApplicationArguments args) {
		// The shared connection is wrapped by SingleConnectionDataSource, so
		// closing it here does not close the engine.
		try (Connection connection = dataSource.getConnection()) {
			ScriptUtils.executeSqlScript(connection,
					new EncodedResource(new ClassPathResource("seed/seed.sql")));
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to run seed/seed.sql against DuckDB", ex);
		}
	}
}