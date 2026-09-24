# ============================================================================
#  Idempotent start/stop for the course application suite.
#
#  Suite:
#    eighth-api-rules/app   Spring Boot 4 REST API (no database)   :8080
#    seven-database-laws/db postgres:16 (docker compose)           :5432
#    seven-database-laws/app Spring Boot 4 REST API (needs db)     :8083
#    analytics-with-duckdb   DuckDB CLI container (shared file DB)   (docker)
#    analytics-with-duckdb/app Spring Boot 4 analytics API          :8084
#    america-debt-crisis-world  Jupyter notebook course (DuckDB file + CLI)  (docker; tests are venv-only)
#    eu-independence (standalone)  Spring Boot 4 + Thymeleaf + htmx course :8091
#    devops-zero-hero (standalone) Spring Boot 4 + Thymeleaf + htmx course :8092
#    k8s (standalone) kubectl simulator for the DevOps course             :8093
#
#  Idempotency: `make start` on an already-running suite is a no-op that
#  reports what is already up; `make stop` on an already-stopped suite
#  succeeds silently. Ports are overridable:
#
#    make start PORT_RULES=9090 PORT_DBLAWS=9093 PORT_ANALYTICS=9084 PORT_DEVOPS=9092 PORT_K8S=9093
# ============================================================================

RUN_DIR     := $(abspath .run)
RULES_APP   := eighth-api-rules
DBLAWS_APP  := seven-database-laws
ANAL_APP    := analytics-with-duckdb
DEBT_APP    := america-debt-crisis-world
EU_APP      := eu-independence
DEVOPS_APP  := devops-zero-hero
K8S_APP     := k8s
COMPOSE          := $(DBLAWS_APP)/docker-compose.yml
COMPOSE_ANALYTICS := $(ANAL_APP)/docker-compose.yml
COMPOSE_DEBT := $(DEBT_APP)/docker-compose.yml

# Derived DuckDB file URL for hosts whose docker-compose .env redirects the
# shared data directory (macOS Docker can't bind-mount ~/Documents). Empty on
# machines that use the default ./data mount — the app then opens the repo's
# own data/analytics.duckdb via the relative URL in application.yml.
ANALYTICS_URL := $(shell awk -F= '/^DUCKDB_DATA=/{print "jdbc:duckdb:"$$2"/analytics.duckdb"}' $(ANAL_APP)/.env 2>/dev/null)

PORT_RULES     ?= 8080
PORT_DBLAWS    ?= 8083
PORT_ANALYTICS ?= 8084
PORT_EU        ?= 8091
PORT_DEVOPS    ?= 8092
PORT_K8S       ?= 8093

# Spring Boot 4.1.1 is supported up to Java 26. sdkman may point JAVA_HOME at
# the 27-ea "current" JDK, which breaks the build, so prefer JDK 24 when the
# SDK is present and fall back to the inherited JAVA_HOME otherwise.
JAVA_HOME   := $(shell /usr/libexec/java_home -v 24 2>/dev/null || echo "$(JAVA_HOME)")
export JAVA_HOME

.DEFAULT_GOAL := help
.PHONY: all help start stop restart status logs test clean \
        start-apps stop-apps start-db stop-db start-duckdb stop-duckdb \
        start-rules stop-rules start-dblaws stop-dblaws \
        start-analytics stop-analytics start-euind stop-euind \
        start-devops stop-devops docker-build-devops docker-run-devops \
        start-k8s stop-k8s \
        start-debt-db stop-debt-db

# ----------------------------------------------------------------------------
#  Recipe templates. NOTE: no '@' prefixes inside defines — they are embedded
#  mid-recipe by $(call); each invoking rule supplies its own leading '@'.
#  Each app is started from ITS OWN directory so relative config like
#  DuckDB's "jdbc:duckdb:../data/analytics.duckdb" resolves next to app/.
# ----------------------------------------------------------------------------

# start_spring_app NAME PORT [DUCKDB_URL] — background the app if the port is
# free. The optional URL becomes -Dspring-boot.run.jvmArguments (an env/system
# property the duckdb.url placeholder reads), for hosts whose data dir differs
# from the repo default (e.g. the macOS ~/Documents mount workaround).
define start_spring_app
	cd $(1)/app; \
	mkdir -p $(RUN_DIR); \
	if [ -n "$$(lsof -tiTCP:$(2) -sTCP:LISTEN 2>/dev/null)" ]; then \
	  echo "==> $(1) already running on port $(2), skipping"; \
	else \
	  echo "==> starting $(1) on port $(2)"; \
	  __jvmargs=""; \
	  if [ -n "$(3)" ]; then __jvmargs="-Dspring-boot.run.jvmArguments=-Dduckdb.url=$(3)"; fi; \
	  mvn -q spring-boot:run -Dspring-boot.run.arguments=--server.port=$(2) $$__jvmargs >>$(RUN_DIR)/$(1).log 2>&1 & \
	  echo $$! > $(RUN_DIR)/$(1).pid; \
	  $(call wait_port,$(2),$(1)); \
	fi
endef

# wait_port PORT NAME — block until the port accepts connections or the app dies.
define wait_port
	__port=$(1); __name=$(2); \
	__pid=$$(cat $(RUN_DIR)/$$__name.pid 2>/dev/null); __ok=0; \
	for __i in $$(seq 1 90); do \
	  if nc -z localhost $$__port 2>/dev/null; then __ok=1; break; fi; \
	  if [ -n "$$__pid" ] && ! kill -0 $$__pid 2>/dev/null; then break; fi; \
	  sleep 1; \
	done; \
	if [ "$$__ok" = 1 ]; then \
	  echo "==> $$__name is UP on port $$__port"; \
	else \
	  echo "==> FAILED: $$__name did not open port $$__port"; \
	  echo "---- last log lines ($$__name) ----"; \
	  tail -20 $(RUN_DIR)/$$__name.log 2>/dev/null; \
	  exit 1; \
	fi
endef

# stop_spring_app NAME PORT — kill whatever listens on the port, tolerate none.
define stop_spring_app
	__port=$(2); __name=$(1); \
	__pid=$$(lsof -tiTCP:$$__port -sTCP:LISTEN 2>/dev/null | head -1); \
	if [ -n "$$__pid" ]; then \
	  echo "==> stopping $$__name (pid $$__pid)"; \
	  kill $$__pid 2>/dev/null; \
	  for __i in $$(seq 1 20); do \
	    if ! lsof -tiTCP:$$__port -sTCP:LISTEN >/dev/null 2>&1; then break; fi; \
	    sleep 0.5; \
	  done; \
	  rm -f $(RUN_DIR)/$$__name.pid; \
	  echo "==> $$__name stopped"; \
	else \
	  echo "==> $$__name not running (port $$__port already free)"; \
	fi
endef

# ----------------------------------------------------------------------------
#  Suite-level lifecycle
# ----------------------------------------------------------------------------

start: start-rules start-dblaws start-analytics
	@echo ""
	@echo "Suite up. Postgres :5432 | api-rules :$(PORT_RULES) | db-laws :$(PORT_DBLAWS) | analytics :$(PORT_ANALYTICS)"
	@echo "Run 'make status' to verify, 'make logs' to watch output."

stop: stop-rules stop-dblaws stop-analytics stop-db stop-duckdb
	@echo ""
	@echo "Suite stopped."

restart: stop start

start-apps: start-rules start-dblaws start-analytics

stop-apps: stop-rules stop-dblaws stop-analytics

status:
	@echo "eighth-api-rules : $$(lsof -tiTCP:$(PORT_RULES) -sTCP:LISTEN >/dev/null 2>&1 && echo 'UP  :$(PORT_RULES)' || echo 'down')"
	@echo "seven-db-laws    : $$(lsof -tiTCP:$(PORT_DBLAWS) -sTCP:LISTEN >/dev/null 2>&1 && echo 'UP  :$(PORT_DBLAWS)' || echo 'down')"
	@echo "postgres         : $$(docker ps --filter name=dblaws-postgres --format '{{.Status}}' 2>/dev/null | grep -q . && echo 'Up :5432' || echo 'down')"
	@echo "duckdb file DB   : $$(docker ps --filter name=duckdb-analytics --format '{{.Status}}' 2>/dev/null | grep -q . && echo 'Up (container)' || echo 'down')"
	@echo "duckdb-debt CLI  : $$(docker ps --filter name=duckdb-debt --format '{{.Status}}' 2>/dev/null | grep -q . && echo 'Up (container)' || echo 'down')"
	@echo "analytics        : $$(lsof -tiTCP:$(PORT_ANALYTICS) -sTCP:LISTEN >/dev/null 2>&1 && echo 'UP  :$(PORT_ANALYTICS)' || echo 'down')"
	@echo "eu-independence  : $$(lsof -tiTCP:$(PORT_EU) -sTCP:LISTEN >/dev/null 2>&1 && echo 'UP  :$(PORT_EU)' || echo 'down')"
	@echo "devops-zero-hero : $$(lsof -tiTCP:$(PORT_DEVOPS) -sTCP:LISTEN >/dev/null 2>&1 && echo 'UP  :$(PORT_DEVOPS)' || echo 'down')"
	@echo "k8s-simulator    : $$(lsof -tiTCP:$(PORT_K8S) -sTCP:LISTEN >/dev/null 2>&1 && echo 'UP  :$(PORT_K8S)' || echo 'down')"
	@echo "docker images    : $$(docker images --format '{{.Repository}}:{{.Tag}}' 2>/dev/null | grep -c 'devops-zero-hero\|api-rules' ) devops/api-rules image(s) built"

logs:
	@echo "==> tailing $(RUN_DIR)/*.log (Ctrl-C to detach)"
	@tail -f $(RUN_DIR)/eighth-api-rules.log $(RUN_DIR)/seven-database-laws.log $(RUN_DIR)/analytics-with-duckdb.log 2>/dev/null \
	  || { echo "No logs yet — run 'make start' first."; exit 0; }

test:
	@echo "==> Testing eighth-api-rules"
	+mvn -B -f $(RULES_APP)/app/pom.xml test
	@echo "==> Testing seven-database-laws (H2, no Docker needed)"
	+mvn -B -f $(DBLAWS_APP)/app/pom.xml test
	@echo "==> Testing analytics-with-duckdb (in-memory DuckDB, no Docker needed)"
	+mvn -B -f $(ANAL_APP)/app/pom.xml test
	@echo "==> Testing eu-independence (Thymeleaf + htmx course, no Docker needed)"
	+mvn -B -f $(EU_APP)/app/pom.xml test
	@echo "==> Testing devops-zero-hero (DevOps course app, no Docker needed)"
	+mvn -B -f $(DEVOPS_APP)/app/pom.xml test
	@echo "==> Testing k8s simulator (kubectl lab, no Docker needed)"
	+mvn -B -f $(K8S_APP)/app/pom.xml test
	@echo "==> Testing over1m-sec-requests"
	+mvn -B -f $(OVER1M_APP)/app/pom.xml test
	@echo "==> Testing america-debt-crisis-world (seed + model + notebook smoke tests, no Docker needed)"
	+if [ -x $(DEBT_APP)/.venv/bin/pytest ]; then \
	  cd $(DEBT_APP) && .venv/bin/pytest -q; \
	elif python3 -m pytest --version >/dev/null 2>&1; then \
	  cd $(DEBT_APP) && python3 -m pytest -q; \
	else \
	  echo "ERROR: no pytest found in $(DEBT_APP)/.venv — run: cd $(DEBT_APP) && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt && .venv/bin/pip install pytest"; exit 1; \
	fi

clean:
	@rm -rf $(RUN_DIR)
	@echo "Removed $(RUN_DIR)/ (logs + pid files)."

# ----------------------------------------------------------------------------
#  Per-application controls
# ----------------------------------------------------------------------------

start-rules:
	@$(call start_spring_app,$(RULES_APP),$(PORT_RULES))

stop-rules:
	@$(call stop_spring_app,$(RULES_APP),$(PORT_RULES))

start-dblaws: start-db
	@$(call start_spring_app,$(DBLAWS_APP),$(PORT_DBLAWS))

stop-dblaws:
	@$(call stop_spring_app,$(DBLAWS_APP),$(PORT_DBLAWS))

# The analytics app opens the DuckDB file at ../data/analytics.duckdb from
# inside app/. If the course .env overrides DUCKDB_DATA (macOS Docker cannot
# bind-mount ~/Documents), ANALYTICS_URL carries the matching absolute URL so
# container and app really share one file.
start-analytics: start-duckdb
	@$(call start_spring_app,$(ANAL_APP),$(PORT_ANALYTICS),$(ANALYTICS_URL))

stop-analytics:
	@$(call stop_spring_app,$(ANAL_APP),$(PORT_ANALYTICS))

start-euind:
	@$(call start_spring_app,$(EU_APP),$(PORT_EU))

stop-euind:
	@$(call stop_spring_app,$(EU_APP),$(PORT_EU))

start-devops:
	@$(call start_spring_app,$(DEVOPS_APP),$(PORT_DEVOPS))

stop-devops:
	@$(call stop_spring_app,$(DEVOPS_APP),$(PORT_DEVOPS))

start-k8s:
	@$(call start_spring_app,$(K8S_APP),$(PORT_K8S))

stop-k8s:
	@$(call stop_spring_app,$(K8S_APP),$(PORT_K8S))

# The Docker targets build and run the course itself as a container, so the
# "what ships" lesson is demonstrable end-to-end on one machine.
docker-build-devops:
	@docker info >/dev/null 2>&1 \
	  || { echo "ERROR: Docker daemon is not running — start Docker Desktop first."; exit 1; }; \
	echo "==> building devops-zero-hero image"; \
	docker build -t devops-zero-hero:0.0.1 $(DEVOPS_APP)

docker-run-devops: docker-build-devops
	@docker info >/dev/null 2>&1 \
	  || { echo "ERROR: Docker daemon is not running — start Docker Desktop first."; exit 1; }; \
	if [ -n "$$(docker ps -aq --filter name=devops-lab 2>/dev/null)" ]; then \
	  echo "==> removing previous devops-lab container"; \
	  docker rm -f devops-lab >/dev/null 2>&1; \
	fi; \
	echo "==> running devops-zero-hero on http://localhost:$(PORT_DEVOPS)"; \
	docker run -d --name devops-lab -p $(PORT_DEVOPS):8080 devops-zero-hero:0.0.1; \
	$(call wait_port,$(PORT_DEVOPS),devops-lab)

start-db:
	@docker info >/dev/null 2>&1 \
	  || { echo "ERROR: Docker daemon is not running — start Docker Desktop first."; exit 1; }; \
	if [ -n "$$(docker ps --filter name=dblaws-postgres --format '{{.Names}}')" ] \
	   && [ "$$(docker inspect -f '{{.State.Health.Status}}' dblaws-postgres 2>/dev/null)" = "healthy" ]; then \
	  echo "==> Postgres already healthy, skipping"; \
	else \
	  echo "==> starting Postgres ($(COMPOSE))"; \
	  docker compose -f $(COMPOSE) up -d --wait; \
	fi

stop-db:
	@docker info >/dev/null 2>&1 || { echo "==> Docker not running — nothing to stop"; exit 0; }; \
	if [ -n "$$(docker ps --filter name=dblaws-postgres --format '{{.Names}}')" ]; then \
	  echo "==> stopping Postgres ($(COMPOSE))"; \
	  docker compose -f $(COMPOSE) down; \
	else \
	  echo "==> Postgres not running, skipping"; \
	fi

start-duckdb:
	@docker info >/dev/null 2>&1 \
	  || { echo "ERROR: Docker daemon is not running — start Docker Desktop first."; exit 1; }; \
	if [ -n "$$(docker ps --filter name=duckdb-analytics --format '{{.Names}}')" ] \
	   && [ "$$(docker inspect -f '{{.State.Health.Status}}' duckdb-analytics 2>/dev/null)" = "healthy" ]; then \
	  echo "==> duckdb-analytics already healthy, skipping"; \
	else \
	  echo "==> starting duckdb-analytics ($(COMPOSE_ANALYTICS))"; \
	  docker compose -f $(COMPOSE_ANALYTICS) up -d --build --wait; \
	fi

stop-duckdb:
	@docker info >/dev/null 2>&1 || { echo "==> Docker not running — nothing to stop"; exit 0; }; \
	if [ -n "$$(docker ps --filter name=duckdb-analytics --format '{{.Names}}')" ]; then \
	  echo "==> stopping duckdb-analytics ($(COMPOSE_ANALYTICS))"; \
	  docker compose -f $(COMPOSE_ANALYTICS) down; \
	else \
	  echo "==> duckdb-analytics not running, skipping"; \
	fi

start-debt-db:
	@docker info >/dev/null 2>&1 \
	  || { echo "ERROR: Docker daemon is not running — start Docker Desktop first."; exit 1; }; \
	if [ -n "$$(docker ps --filter name=duckdb-debt --format '{{.Names}}')" ] \
	   && [ "$$(docker inspect -f '{{.State.Health.Status}}' duckdb-debt 2>/dev/null)" = "healthy" ]; then \
	  echo "==> duckdb-debt already healthy, skipping"; \
	else \
	  echo "==> starting duckdb-debt ($(COMPOSE_DEBT))"; \
	  docker compose -f $(COMPOSE_DEBT) up -d --build --wait; \
	fi

stop-debt-db:
	@docker info >/dev/null 2>&1 || { echo "==> Docker not running — nothing to stop"; exit 0; }; \
	if [ -n "$$(docker ps --filter name=duckdb-debt --format '{{.Names}}')" ]; then \
	  echo "==> stopping duckdb-debt ($(COMPOSE_DEBT))"; \
	  docker compose -f $(COMPOSE_DEBT) down; \
	else \
	  echo "==> duckdb-debt not running, skipping"; \
	fi

# ----------------------------------------------------------------------------
#  Help
# ----------------------------------------------------------------------------

help:
	@echo "Course application suite — idempotent start/stop"
	@echo ""
	@echo "  make start          bring up Postgres, both Docker helpers + all three apps (no-op if up)"
	@echo "  make stop           bring the whole suite down (no-op if down)"
	@echo "  make restart        stop then start"
	@echo "  make status         show which pieces are up"
	@echo "  make logs           tail app logs (Ctrl-C to detach)"
	@echo "  make test           run all test suites (H2/in-memory/venv, no Docker needed)"
	@echo ""
	@echo "  make start-db       Postgres only         |  make stop-db"
	@echo "  make start-duckdb   DuckDB CLI container  |  make stop-duckdb"
	@echo "  make start-debt-db  debt-course CLI       |  make stop-debt-db"
	@echo "  make start-rules    api-rules only        |  make stop-rules"
	@echo "  make start-dblaws   db-laws only (+ db)   |  make stop-dblaws"
	@echo "  make start-analytics analytics only (+duckdb) |  make stop-analytics"
	@echo "  make start-euind    eu-independence only  |  make stop-euind"
	@echo "  make start-devops   devops-zero-hero only |  make stop-devops"
	@echo "  make start-k8s      kubectl simulator only |  make stop-k8s"
	@echo "  make docker-run-devops build + run devops-zero-hero as a container"
	@echo "  make start-apps     apps only             |  make stop-apps"
	@echo "  make clean          remove logs + pid files"
	@echo ""
	@echo "  Ports (override with e.g. 'make start PORT_RULES=9090'):"
	@echo "    api-rules :$(PORT_RULES)   db-laws :$(PORT_DBLAWS)   analytics :$(PORT_ANALYTICS)   postgres :5432"
	@echo "    eu-independence :$(PORT_EU)   devops-zero-hero :$(PORT_DEVOPS)   k8s-simulator :$(PORT_K8S)   (standalone)"

# ---------------------------------------------------------------------------
# over1m-sec-requests — 1M-RPS course (mirrors k8s conventions, no Docker)
# ---------------------------------------------------------------------------
OVER1M_APP  := over1m-sec-requests
PORT_OVER1M  ?= 8094

.PHONY: start-over1m stop-over1m

start-over1m:
	@$(call start_spring_app,$(OVER1M_APP),$(PORT_OVER1M))

stop-over1m:
	@$(call stop_spring_app,$(OVER1M_APP),$(PORT_OVER1M))

test-over1m:
	@echo "==> Testing over1m-sec-requests (1M-RPS arc simulator, no Docker needed)"
	+mvn -B -f $(OVER1M_APP)/app/pom.xml test

# ---------------------------------------------------------------------------
# fioci-demo — educational AWS-concepts app (compose-backed, standalone)
# ---------------------------------------------------------------------------
FIOCI_APP    := fioci-demo
PORT_FIOCI   ?= 8095

.PHONY: start-fioci-demo stop-fioci-demo test-fioci-demo

start-fioci-demo:
	@$(call start_spring_app,$(FIOCI_APP),$(PORT_FIOCI))

stop-fioci-demo:
	@$(call stop_spring_app,$(FIOCI_APP),$(PORT_FIOCI))

test-fioci-demo:
	@echo "==> Testing fioci-demo (AWS concepts on docker-compose, no Docker needed for app)"
	+mvn -B -f $(FIOCI_APP)/app/pom.xml test
