#!/usr/bin/env bash
# Entrypoint for the DuckDB CLI container.
#
# 1. Ensure the shared database file and parent directories exist.
# 2. Seed the database on first run (idempotent statements in seed/seed.sql).
# 3. Stay alive so the notebook suite and the CLI share the file.
set -euo pipefail

DB_FILE="${DUCKDB_FILE:-/data/analytics.duckdb}"
mkdir -p "$(dirname "$DB_FILE")"

if [ ! -f "$DB_FILE" ]; then
  echo "[duckdb] creating empty database at $DB_FILE"
  duckdb "$DB_FILE" -c "SELECT 1" >/dev/null
fi

if [ "${SEED_ON_START:-1}" = "1" ] && [ -f /init/seed.sql ]; then
  echo "[duckdb] seeding (idempotent)"
  duckdb "$DB_FILE" < /init/seed.sql 2>/dev/null || true
fi

echo "[duckdb] ready — database file: $DB_FILE"
exec sleep infinity