# Education Log — Eighth API Rules Course

> Documentation of every step taken to build this course, written for "future
> me" (and anyone else) to learn from and reuse.

## 1. Project Overview

**Goal:** Turn the YouTube video *"8 API Laws of Senior Backend Developer"*
(<https://www.youtube.com/watch?v=-40xErgJIBg>) into a structured, runnable
educational resource: a Spring Boot 4 application that demonstrates the laws,
plus lesson plans, exercises and solutions.

**Tools used:**
- **Tesseract OCR** (Homebrew) — extract the framework image `OLD.jpg` → `OLD.txt`
- **Web search / fetch** — analyse the video, pull the full transcript
- **Maven 3.9.16 + JDK 24** (OpenJDK 24.0.1) — build and test the app
- **Spring Boot 4.1.1** — the application platform

**Deliverables:**

| File / Folder | Description |
|---|---|
| `OLD.txt` | OCR extraction of `OLD.jpg` (9-input lesson-planning framework) |
| `README.md` | Course overview, run instructions, curl examples |
| `lesson_plans.md` | 8 lesson plans + integration project, built on the 9-input framework |
| `exercises.md` | Per-lesson exercises + integration review rubric |
| `solutions.md` | Verified sample answers |
| `education.md` | This document |
| `app/` | Full Spring Boot 4 REST application (products + orders) |

## 2. The Source Material

Two inputs were combined:

1. **`OLD.jpg`** — an image of a "lesson planning as a system" framework. The
   active model cannot read images, so OCR was used. The 9 design inputs
   extracted:
   1. Learning Goal
   2. Lesson Sequence
   3. Assessment Evidence
   4. Learner Profile
   5. Prior Knowledge
   6. Learning Activities
   7. Output Requirements
   8. Accessibility & Supports
   9. Teacher Decisions
   Tagline: *"A lesson plan is a system, not a prompt."*

2. **YouTube video** — *8 API Laws of Senior Backend Developer* by Cloud X Berry
   (~9 min). The 8 laws, extracted from the transcript:
   1. Design around resources, not actions
   2. Make URLs predictable
   3. Use HTTP methods for their actual purpose (incl. idempotency)
   4. Make status codes useful
   5. Keep error responses consistent
   6. Don't put everything into the URL path (query params for refinement)
   7. Treat API changes carefully (additive vs breaking; versioning)
   8. Keep request and response formats consistent

   The closing message of the video — *"a good API is one clients can
   understand without constantly checking documentation; start by defining the
   patterns, not endpoints"* — became the design thesis for the app.

## 3. Step 0 — OCR the Image

The model can't read images directly, so:

```bash
which tesseract          # tesseract 5.5.3 already installed
tesseract OLD.jpg OLD.txt
```

Note: tesseract appends its own `.txt`, producing `OLD.txt.txt` — rename after:
```bash
mv OLD.txt.txt OLD.txt
```
The extracted text was then verified against the image content (framework
inputs + tagline all present).

## 4. Step 1 — Analyse the Video

`webfetch` on the YouTube page returns almost no content (JS-rendered). Two
more effective routes:

1. `websearch` for the video title → found the law list and chapter timestamps
   immediately.
2. Fetched the full auto-generated transcript from a transcript-scraper site
   (`youtube-transcript.ai/transcript/-40xErgJIBg.txt`) → 4,213 words across
   the 8 laws. This gave the detail needed to write accurate exercises
   (idempotency, 400/401/403/404/409/422/429, `created_at` vs `createdAt`, etc.).

> Lesson: for YouTube content, transcript extraction beats page scraping. Get
> the transcript first, then the description/chapters for structure.

## 5. Step 2 — Spring Boot 4 Research

Key unknowns before writing code, and how they were resolved:

| Question | Finding |
|---|---|
| Current stable Spring Boot? | **4.1.1** (Boot 4.0 GA Nov 2025; needs Java 17+, tested to 26) |
| Does the machine have JDK 17+? | `java_home` lists 17/21/24; used **24** (default shell JDK is 27-ea, too new) |
| Does Boot 4 have native API versioning? | Yes — Spring Framework 7 feature. Configure in `WebMvcConfigurer.configureApiVersioning(...)`; map with the new `version` attribute on `@RequestMapping`/`@GetMapping`; version resolved from header/param/path/media type; unsupported → 400 |
| Where did test annotations move? | `@AutoConfigureMockMvc` moved to `org.springframework.boot.webmvc.test.autoconfigure` in the new `spring-boot-webmvc-test` module; add `spring-boot-starter-webmvc-test` alongside `spring-boot-starter-test` (confirmed on Maven Central) |
| Records + Bean Validation | Records work with Jakarta validation annotations on components; put `@Valid` on the *type argument* (`List<@Valid Item>`), not the container — validation warns otherwise |

## 6. Step 3 — Design the Application

**Domain:** a tiny shop — `products` and `orders` — big enough to show every law,
small enough to read in one sitting. Deliberate design decisions mapping to laws:

| Law | Where it lives in the app |
|---|---|
| 1 | `/products`, `/orders`, `/orders/{id}/items` — never `/getProducts` |
| 2 | Always-plural nouns, camelCase JSON keys, nested sub-resources |
| 3 | POST create → 201 + Location; PUT full replace; PATCH partial update; DELETE → 204; idempotency discussed in exercises |
| 4 | 200/201/204 success; 400 bad request; 404 not found; 409 conflicts (insufficient stock, illegal status transition); 422 validation |
| 5 | `ApiError` record reused by every handler in `GlobalExceptionHandler` |
| 6 | `?category=&minPrice=&maxPrice=&inStock=&sort=&page=&size=` on `GET /products` |
| 7 | Native versioning via `API-Version` header; `version = "1.0"` and `"1.1"` handlers for `GET /products/{id}` (v1.1 adds `currency`/`stock`); `"1.0+"` baseline on unchanged endpoints |
| 8 | `PageResponse<T>` envelope everywhere; `Instant` (ISO-8601); `ProductResponse` naming |

**Architecture:** records for models/DTOs, `@Service` with in-memory
`ConcurrentHashMap` stores (no DB dependency — keeps teaching focused),
`@RestController`s with constructor injection, a `@RestControllerAdvice` for
errors, and `WebConfig` enabling API versioning.

## 7. Step 4 — Verify the Code

"Never ship code you haven't compiled."

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f eighth-api-rules/app/pom.xml test
```

Result: **19 tests, 0 failures** across three test classes, including:
- versioned shapes (v1.0 omits `currency`, v1.1 includes it),
- missing version header → 400,
- consistent error bodies (404/409/422),
- pagination/filter/sort via query params,
- idempotency-relevant create/replace/partial-update/delete flows,
- nested sub-resource (`/orders/1/items`).

One deprecation warning surfaced during the first run
(`@Valid` on a `List` container) and was fixed by moving `@Valid` to the type
argument.

**Live smoke test** — the MockMvc tests pass, but a real boot was also verified:
packaged the jar (`mvn package`), ran it on port 18080, and curl'd:
- `GET /products/1` with v1.0 vs v1.1 → confirmed different additive shapes,
- `GET /products/999` → `{"status":404,"code":"PRODUCT_NOT_FOUND",...}`,
- missing `API-Version` → 400,
- query filtering/sorting/pagination → correct 2-element `home` page,
- `POST /products` → 201 with `Location: /products/5`.

(Note: a foreign process occupied port 8080 on this machine; used `--server.port=18080`.)

## 8. Step 5 — Author the Teaching Material

`lesson_plans.md` applies the 9 inputs from `OLD.txt` to each of the 8 laws and
an integration project. `exercises.md` gives hands-on tasks that require reading
the app's code and running curl commands; `solutions.md` provides verified
answers (responses were produced by actually running the app).

## 9. Quick Reference — Commands

```bash
# OCR (macOS)
tesseract input.jpg out        # -> writes out.txt (rename if odd suffix)
mv out.txt.txt out.txt 2>/dev/null

# Build & test the Spring Boot 4 app
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -f eighth-api-rules/app/pom.xml test
JAVA_HOME=$(/usr/libexec/java_home -v 24) mvn -B -q -f eighth-api-rules/app/pom.xml package -DskipTests

# Live smoke test on a custom port
java -jar eighth-api-rules/app/target/api-rules-0.0.1-SNAPSHOT.jar --server.port=18080 &
curl -H "API-Version: 1.1" http://localhost:18080/products/1
kill %1

# Standard run
cd eighth-api-rules/app && mvn spring-boot:run
```

## 10. Lessons Learned

1. **OCR rescue again.** When a model can't read an image, `tesseract` is the
   reliable escape hatch (worked for this series before; worked here again in
   one command).
2. **Transcripts beat page scraps for YouTube.** Whole transcript in one fetch;
   page fetch returned almost nothing.
3. **Verify platform facts before coding.** Spring Boot 4 moved packages
   (`@AutoConfigureMockMvc`), introduced new starters (`spring-boot-starter-webmvc-test`)
   and new features (API versioning). A 30-second release-notes check saved an
   hour of guessing.
4. **Use the newest SDK that is *supported*.** Default `java` was 27-ea; Spring
   Boot 4.1.1 documents support up to Java 26. Pinned to JDK 24 for the build.
5. **Compile, run, then smoke-test.** 19 green MockMvc tests felt great; the
   live curl against a packaged jar confirmed the README examples actually work.
6. **Design the domain around the teaching goals.** A shop with products and
   orders made all 8 laws concrete without needing a database.

## 11. Where to Go Next

- [ ] Add a 9th security lesson (auth, rate limiting — mentioned by the video).
- [ ] Add OpenAPI/Swagger docs and an OpenAPI-first contract exercise.
- [ ] Add a DB-backed variant (Spring Data JPA + H2) as a stretch project.
- [ ] Script the video→course pipeline (transcript → law list → app scaffold).
- [ ] Add per-lesson quizzes with self-grading.