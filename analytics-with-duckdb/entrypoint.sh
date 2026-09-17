#!/usr/bin/env bash
# Entrypoint for the DuckDB CLI container.
#
# Tasks:
# 1. Ensure the shared database file and parent directories exist.
# 2. Seed the database on first run (idempotent statements).
# 3. Stay alive so the Spring app and the CLI can share the file.
#    Run `docker compose exec duckdb duckdb /data/analytics.duckdb`
#    to open the interactive REPL.
set -euo pipefail

DB_FILE="${DUCKDB_FILE:-/data/analytics.duckdb}"
mkdir -p "$(dirname "$DB_FILE")"

# Create an empty file-based database if none exists yet.
if [ ! -f "$DB_FILE" ]; then
  echo "[duckdb] creating empty database at $DB_FILE"
  duckdb "$DB_FILE" -c "SELECT 1" >/dev/null
fi

# Run the shared seed script once (idempotent: every statement uses IF NOT EXISTS
# and inserts only when the table is empty).  The same seed.sql is run by the
# Spring DataInitializer, so running it here too is harmless and lets the CLI
# be useful even before the JVM starts.
if [ "${SEED_ON_START:-1}" = "1" ] && [ -f /init/seed.sql ]; then
  # The seed script is fast (< 5 s for the default 300k event / 50k order
  # dataset).  DuckDB auto-commits, so partial failures are safe to re-run.
  echo "[duckdb] seeding (idempotent)"
  duckdb "$DB_FILE" < /init/seed.sql 2>/dev/null || true
fi

echo "[duckdb] ready — database file: $DB_FILE"

# Keep the container alive while the app uses the shared file.
exec sleep infinity