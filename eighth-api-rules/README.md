# Eighth API Rules

An educational course that turns the YouTube video **"8 API Laws of Senior
Backend Developer"** (Cloud X Berry,
<https://www.youtube.com/watch?v=-40xErgJIBg>) into a runnable Spring Boot 4
application plus lesson plans, exercises and solutions.

> Tagline from the source framework: **"A lesson plan is a system, not a prompt."**

## The 8 laws covered

| # | Law | Short version |
|---|-----|---------------|
| 1 | Design around resources, not actions | URLs name resources; HTTP methods name actions |
| 2 | Make URLs predictable | One naming convention, everywhere, forever |
| 3 | Use HTTP methods for their purpose | GET/POST/PUT/PATCH/DELETE semantics + idempotency |
| 4 | Make status codes useful | 200/201/204 vs 400/404/409/422 — never "200 + error body" |
| 5 | Keep error responses consistent | One structured error shape for every endpoint |
| 6 | Don't put everything in the path | Identity in path, refinement in query parameters |
| 7 | Treat API changes carefully | Additive vs breaking; version with one consistent strategy |
| 8 | Keep formats consistent | camelCase keys, ISO dates, one pagination envelope |

## Repository layout

```
eighth-api-rules/
├── OLD.txt          # OCR text extracted from OLD.jpg (the lesson-planning framework)
├── lesson_plans.md  # 8 lesson plans + integration project built on the OLD framework
├── exercises.md     # per-lesson exercises + integration review rubric
├── solutions.md     # sample answers (verified against the running app)
├── education.md     # education log: how this course was built
└── app/             # Spring Boot 4.1.1 REST application (products + orders)
    └── src/main/java/com/example/apirules/
        ├── config/WebConfig.java      # Law 7: native Spring API versioning (API-Version header)
        ├── model/                     # records: Product, Order, ApiError, PageResponse, ...
        ├── exception/                 # 404/409/400 + GlobalExceptionHandler (Laws 4 & 5)
        ├── service/                   # ProductService, OrderService (business rules)
        └── web/                       # ProductController, OrderController (Laws 1,2,3,6,7,8)
```

## Run the app

Requirements: JDK 17+ (tested on 24), Maven 3.9+.

```bash
cd app
mvn spring-boot:run        # starts on http://localhost:8080
mvn test                   # 19 tests verifying every law
```

Spring Framework 7's API versioning is enabled, so **every request must send an
`API-Version` header** (default: required; a missing header answers `400`).

### Try it yourself

```bash
# Law 7 — the same URL, two contracts:
curl -H "API-Version: 1.0"  http://localhost:8080/products/1
# {"id":1,"name":"Clean Code",...,"price":39.99,"createdAt":"2026-01-15T09:00:00Z"}

curl -H "API-Version: 1.1"  http://localhost:8080/products/1
# ...adds "currency":"USD","stock":100  (additive, non-breaking)

# Laws 1-3 — resource URLs + correct methods:
curl -i -X POST -H "API-Version: 1.1" -H "Content-Type: application/json" \
  -d '{"name":"Mouse","category":"electronics","price":12.99,"currency":"USD","stock":40}' \
  http://localhost:8080/products            # 201 + Location: /products/5
curl -i -X DELETE -H "API-Version: 1.1" http://localhost:8080/products/5   # 204

# Law 6 — identity in path, refinement in query:
curl -H "API-Version: 1.1" "http://localhost:8080/products?category=home&sort=price:desc&page=0&size=1"

# Laws 4 & 5 — useful status + consistent error body:
curl -H "API-Version: 1.1" http://localhost:8080/products/999
# {"status":404,"code":"PRODUCT_NOT_FOUND","message":"Product 999 was not found",...}
```

## Build with Spring Boot 4 (what's under the hood)

- **Spring Boot 4.1.1** parent + `spring-boot-starter-web`,
  `spring-boot-starter-validation`.
- **Native API versioning** (Spring Framework 7): enabled via
  `WebMvcConfigurer.configureApiVersioning(...)`, mapped per handler with the
  new `version` attribute (`@GetMapping(path = "/{id}", version = "1.0")`) and
  baseline versions (`version = "1.0+"`).
- **Testing moved in Boot 4**: `@AutoConfigureMockMvc` now lives in
  `org.springframework.boot.webmvc.test.autoconfigure` (add the
  `spring-boot-starter-webmvc-test` companion starter alongside
  `spring-boot-starter-test`).

## Learning order

1. Read `lesson_plans.md` to see the 9-input framework applied per lesson.
2. Do the matching exercises in `exercises.md`.
3. Check your work against `solutions.md`.
4. Finish with the integration project (rubric included in `exercises.md`).