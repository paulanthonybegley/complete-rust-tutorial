# America's Debt Crisis in the World — minimal DuckDB CLI container.
#
# DuckDB is an embedded analytical engine. "The database" is one file
# (analytics.duckdb) on a shared volume. This image gives learners the DuckDB
# CLI pinned to the same version as the notebook kernel (v1.5.5) so they can
# explore the same tables the notebooks query:
#
#     docker compose exec duckdb duckdb /data/analytics.duckdb
#     SELECT * FROM debt WHERE year = 2026;
FROM debian:bookworm-slim

ARG DUCKDB_VERSION=v1.5.5

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl unzip \
 && rm -rf /var/lib/apt/lists/*

ARG TARGETARCH
RUN if [ -n "${TARGETARCH:-}" ]; then \
        ARCH=${TARGETARCH}; \
    else \
        case "$(uname -m)" in x86_64) ARCH=amd64;; aarch64) ARCH=arm64;; *) ARCH=amd64;; esac; \
    fi \
    && curl -fsSL -o /tmp/duckdb.zip \
        "https://github.com/duckdb/duckdb/releases/download/${DUCKDB_VERSION}/duckdb_cli-linux-${ARCH}.zip" \
    && unzip -o /tmp/duckdb.zip -d /usr/local/bin \
    && chmod +x /usr/local/bin/duckdb \
    && rm /tmp/duckdb.zip \
    && echo -n "installed ${ARCH}: " && duckdb --version

WORKDIR /data

COPY seed /init

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]