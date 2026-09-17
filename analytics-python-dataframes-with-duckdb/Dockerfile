# Analytics from Python DataFrames with DuckDB — minimal CLI container.
#
# DuckDB is an *embedded* engine, not a server, so "the database" is one file
# (analytics.duckdb) on a shared volume. This image provides the DuckDB CLI
# (pinned to the same version as the Python driver in app/requirements.txt)
# so learners can run DESCRIBE/EXPLAIN/SUMMARIZE and ad-hoc SQL from the same
# database file the Python app writes to. Database files are forward- but NOT
# backward-compatible between releases, so CLI and driver must stay on the
# same version (v1.5.5).
FROM debian:bookworm-slim

ARG DUCKDB_VERSION=v1.5.5

RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl unzip \
 && rm -rf /var/lib/apt/lists/*

# Pick the architecture-appropriate CLI binary (BuildKit sets TARGETARCH for us;
# fall back to uname for plain `docker build`).
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

# The seed script is built into the image (rather than bind-mounted) so the
# image works even where Docker Desktop cannot bind-mount project paths
# (e.g. macOS TCC-protected folders like ~/Documents).
COPY app/duckdb_edu/seed /init

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]