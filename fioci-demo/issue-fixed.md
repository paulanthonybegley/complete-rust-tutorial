# issue-fixed.md — what actually broke, and the trail we used to fix it

Every fix below was driven by a real observation (log line, byte, HTTP code),
not a guess. That is the whole teaching point: **a white page is not a 404 —
it is a render failure, and the log always says why.**

## 1. `make start-fioci-demo` → `cd fioci-demo/app/app: No such file or directory`

**Observed:** the recipe `cd`'d into a path that does not exist.

**Why:** the `start_fioci_demo` recipe passed `$(FIOCI_APP)/app` to the
`start_spring_app` macro — but the macro *already* appends `/app`. The effect
was `cd fioci-demo/app/app`.

**Fix:** pass `$(FIOCI_APP)` (not `$(FIOCI_APP)/app`) so the macro's own
`/app` lands on the real directory `fioci-demo/app`, which holds `pom.xml`.

**How we knew it was fixed:** `make -n start-fioci-demo` dry-ran and showed
`cd fioci-demo/app; … mvn -f fioci-demo/app/pom.xml`, and the detached boot
opened port `:8095`.

## 2. Every `/station/{slug}` page came back blank (white page)

**Observed:** `/` rendered 200, but `/station/s3`, `/station/rds`, … all
rendered nothing — yet **not** a 404.

**Real log line (the decisive one):**
```
EL1008E: Property or field 'dockerCompose' cannot be found on object of type
'com.example.fioci.AwsConcept'
```

**Why:** `station.html` renders `th:text="${c.dockerCompose}"` (and `apiHint`),
but the `AwsConcept` record only declared five fields
(`slug, name, awsService, backing, role`). Thymeleaf/SpEL cannot create an
accessor for a field the record does not have, so **every** station render
threw `EL1008E` → Spring returned HTTP 500 → the browser showed a blank page.

**Fix:** the record now carries the two extra fields the template needs —
`dockerCompose` and `apiHint` — and `AwsCatalog` was expanded from 5 to the 8
documented concepts (`s3, rds, elasticache, dynamodb, sqs, lambda, cloudfront,
iam`) so the index grid, every `/station/{slug}`, and every `/api/{slug}`
agree on the same set of slugs.

**How we knew it was fixed:** offline `mvn -o compile` exited 0 with zero
`[ERROR]`; a live sweep of all 18 routes (`/`, 8 stations, `/api`, 8 `/api/*`)
returned `200` on every single one, and the boot log contained **zero**
`EL1008E`/render exceptions.

## 3. `/api` answered but `/api/{slug}` said `simulated` even with Docker up

**Intended design (not a bug):** the app probes each backing port with a
400 ms connect timeout. If the container answers, it reports
`"mode":"live-compose"`; otherwise `"mode":"simulated"`. The front end and the
JSON both render either way, so the API never breaks — it just downgrades
gracefully.

**To flip a probe to live:** `docker compose -f fioci-demo/docker-compose.yml
up -d` (Postgres, Redis, MinIO, DynamoDB Local, LocalStack). Wait for the
healthchecks, then re-call the route.

## 4. White-page fixes are invisible until you re-render

**Observed:** after fixing the record, builds compiled clean but the fix "did
not show up."

**Why:** the fix was only in compiled bytecode; the running JVM still held the
old class. Re-render requires a restart, not a `curl` of a cached page.

**Fix:** restart the app (`make stop-fioci-demo; make start-fioci-demo`) and
sweep the routes again — 200 on all 18 is the acceptance gate.

## How issues were formulated (the repeatable method)

1. **Observe** — let the real failure appear: white page, `000`, or a
   non-200, and capture the actual log/status, not the symptom.
2. **Probe the port or the route** — `curl -w '%{http_code}'` + `tail` the
   `.run/fioci-demo.log`; the log names the exact class and property.
3. **Find the byte truth** — grep the real source: `dockerCompose` is read in
   `station.html`, defined (or missing) in `AwsConcept.java`. The gap between
   the two files *is* the bug.
4. **Fix the smallest true cause** — one field set in the record + one catalog
   expansion; no speculative rewrites.
5. **Verify against the same oracle** — offline compile (exit 0, zero
   `[ERROR]`) then a route sweep where **every** page returns 200 and the log
   shows no render exception.

Result: `fioci-demo` boots offline on `:8095`, all eight stations render,
every `/api/{slug}` answers in `simulated` (or `live-compose` when the backing
container is up), and none of the fixes needed Docker.
