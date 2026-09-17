# Structured Lesson Plans: The 8 API Laws of Senior Backend Developer

> Applied from the YouTube video: **"8 API Laws of Senior Backend Developer"** by Cloud X Berry
> https://www.youtube.com/watch?v=-40xErgJIBg (9 min)
>
> Core tagline from `OLD.jpg`: **"A lesson plan is a system, not a prompt."**

Every lesson below is built with the 9 design inputs extracted from
[`OLD.txt`](./OLD.txt):
**Learning Goal**, **Lesson Sequence**, **Assessment Evidence**, **Learner
Profile**, **Prior Knowledge**, **Learning Activities**, **Output
Requirements**, **Accessibility & Supports**, and **Teacher Decisions**.

The companion application lives in [`app/`](./app) — a Spring Boot 4 REST API
for `products` and `orders` whose code is the "worked example" for each law.
Every lesson lists the exact files to open.

| # | Law (from the video) | App files to read |
|---|----------------------|-------------------|
| 1 | Design around resources, not actions | `web/ProductController.java`, `web/OrderController.java` |
| 2 | Make URLs predictable | `web/ProductController.java`, `web/OrderController.java` |
| 3 | Use HTTP methods for their actual purpose | `web/ProductController.java`, `model/CreateProductRequest.java`, `model/UpdateProductRequest.java` |
| 4 | Make status codes useful | `exception/GlobalExceptionHandler.java`, `web/OrderController.java` |
| 5 | Keep error responses consistent | `model/ApiError.java`, `exception/GlobalExceptionHandler.java` |
| 6 | Don't put everything into the URL path | `service/ProductService.java`, `service/OrderService.java` |
| 7 | Treat API changes carefully | `config/WebConfig.java`, `web/ProductController.java` |
| 8 | Keep request and response formats consistent | `model/PageResponse.java`, `model/ProductResponse.java`, `model/ApiError.java` |

---

## Lesson 1: Design Around Resources, Not Actions

**Learning Goal:** Learners can explain why the URL must name a resource while
the HTTP method names the action, and can design resource endpoints instead of
action endpoints.

**Lesson Sequence:**
1. Warm-up: list what is wrong with `/getUsers`, `/createOrder`, `/deleteProduct`.
2. Read `web/ProductController.java` header comment (Law 1).
3. Map a "ASCII shelf" of actions → resources + methods as a table.
4. Hands-on: complete Exercise 1 in `exercises.md`.

**Assessment Evidence:** Given a set of action-verb URLs, the learner rewrites
them as resource URLs + HTTP verbs, and explains in one sentence why the raw
URL is long-lived.
- Assessment answer key: `solutions.md` → Lesson 1.

**Learner Profile:** Junior developers up to ~1 year of backend experience;
comfortable with Java basics and HTTP enough to have "shipped a few endpoints".

**Prior Knowledge:** What a URL is, what GET/POST/PUT/DELETE mean on a basic
level, Spring `@RestController` basics.

**Learning Activities:** group brainstorm of a "bad URL zoo", paired resource
modeling, individual rewrite drill.

**Output Requirements:** A markdown table in the learner's notes with 10
action-URLs → resource+method pairs; no code writing required at this stage.

**Accessibility & Supports:** Provide the "URL Parts" glossary (scheme, host,
path, query); allow verbal explanations for the rationale question; grant extra
time on the mapping activity.

**Teacher Decisions:** Keep this lesson code-free and discussion-first;
the payoff moment is seeing one URL serve retrieve/replace/delete.

---

## Lesson 2: Make URLs Predictable

**Learning Goal:** Learners apply one naming convention everywhere so a
developer can guess any endpoint without reading docs.

**Lesson Sequence:**
1. Spotlight the inconsistency trap: `user`, `customers`, `customer-profiles`.
2. Read `web/ProductController.java` and `web/OrderController.java` and list *every* URL the app exposes.
3. Identify the convention (plural noun, lowercase, cases, nesting).
4. Hands-on: complete Exercise 2.

**Assessment Evidence:** Learner predicts the URL for a hypothetical resource
(e.g. a "shopping cart") before seeing it, and audits the app's URLs for
consistency.
- Assessment answer key: `solutions.md` → Lesson 2.

**Learner Profile / Prior Knowledge:** Same as Lesson 1.

**Learning Activities:** URL-prediction quiz (Jeopardy style), convention audit
of the app, designing a new nested resource URL.

**Output Requirements:** A written "URL Convention" for the project — the 
learner's own rule card with 5 bullet points.

**Accessibility & Supports:** Use a printed route table; screen-reader-friendly
route listing; allow dictation of predictions instead of typing.

**Teacher Decisions:** Emphasize that *any* chosen convention is acceptable —
the non-negotiable is that you pick one and stick to it.

---

## Lesson 3: Use HTTP Methods for Their Actual Purpose

**Learning Goal:** Learners select GET/POST/PUT/PATCH/DELETE by semantic
meaning and understand idempotency and why it matters for retries.

**Lesson Sequence:**
1. Recap the five methods and their contract (table).
2. Introduce idempotency with the "send it twice" thought experiment (network
   failure / retry).
3. Read the POST/PUT/PATCH/DELETE handlers in `web/ProductController.java`.
4. Hands-on: complete Exercise 3.

**Assessment Evidence:** Learner states whether 5 given operations are
idempotent and why; writes diagnostic sentences for why `POST /products` retried
creates two products while `PUT /products/1` retried does not.
- Assessment answer key: `solutions.md` → Lesson 3.

**Learner Profile / Prior Knowledge:** Bonus if learners have seen a `POST`
used to update something — that becomes the teaching hook.

**Learning Activities:** "Send it twice" prediction game; method-matching drill
(10 scenarios → pick the verb); live curl of the app's endpoints.

**Output Requirements:** A one-page "HTTP method cheatsheet" authored by the
learner, marked with idempotency per row.

**Accessibility & Supports:** Provide a completed template cheatsheet to annotate
instead of writing from scratch.

**Teacher Decisions:** Spend real time on the retry story — it is the strongest,
most memorable justification for the law.

---

## Lesson 4: Make Status Codes Useful

**Learning Goal:** Learners answer failures with the right HTTP status and
never "200 OK + failure body".

**Lesson Sequence:**
1. Show the anti-pattern a snippet returns `200` while the body says "product not found".
2. Quiz: which status for which situation (list of 10 situations).
3. Read `exception/GlobalExceptionHandler.java` and the conflict logic in `service/OrderService.java`.
4. Hands-on: complete Exercise 4.

**Assessment Evidence:** For a bank-transfer scenario, learner selects the
correct status for 8 distinct outcomes, including 400/401/403/404/409/422/429.
- Assessment answer key: `solutions.md` → Lesson 4.

**Learner Profile / Prior Knowledge:** Prior lessons assumed; no networking
background required.

**Learning Activities:** status-code card-sort, "guess the curl exit", mapping
exceptions to codes in the app.

**Output Requirements:** A status-code decision tree (text or image) with the
codes the app actually uses.

**Accessibility & Supports:** Keep cards large; allow "status = category" level
answers rather than exact codes.

**Teacher Decisions:** Teach categories (2xx ok, 4xx client, 5xx server) first;
exact codes come second.

---

## Lesson 5: Keep Error Responses Consistent

**Learning Goal:** Learners design one structured error shape used by every
endpoint.

**Lesson Sequence:**
1. Contrast "something went wrong" with a structured error body.
2. Read `model/ApiError.java` and dissect the JSON it produces.
3. Trace a 404 and a 422 through the app.
4. Hands-on: complete Exercise 5.

**Assessment Evidence:** Learner explains what each `ApiError` field is for
(code vs message vs fieldErrors) and writes the error contract for a new
feature in the same shape.
- Assessment answer key: `solutions.md` → Lesson 5.

**Learner Profile / Prior Knowledge:** Comfortable with JSON.

**Learning Activities:** before/after error-body comparison, "client simulation"
role-play (what does the app do with `code`? with `message`?), extending the
`ApiError` for a new exception.

**Output Requirements:** The error contract (JSON example) for the new feature,
matching the existing field names.

**Accessibility & Supports:** Provide audio description of a sample error
payload; allow building error shape from a drag-and-drop palette.

**Teacher Decisions:** Stress that `code` (machine) and `message` (human) have
separate jobs — that distinction is the senior-level insight.

---

## Lesson 6: Don't Put Everything into the URL Path

**Learning Goal:** Learners decide when something belongs in the path versus a
query parameter.

**Lesson Sequence:**
1. Show the "path creep" example from the video: products → category → stock → price...
2. Read `GET /products` in `web/ProductController.java` and `list()` in `service/ProductService.java`.
3. Rule: the path identifies the resource; query parameters refine it; HTTP methods describe the operation.
4. Hands-on: complete Exercise 6.

**Assessment Evidence:** Learner classifies 10 scenarios as "identity"
(path-worthy) or "refinement" (query-worthy), and justifies `action=delete` as
the anti-pattern.
- Assessment answer key: `solutions.md` → Lesson 6.

**Learner Profile / Prior Knowledge:** Comfortable with URLs and simple HTTP.

**Learning Activities:** path-vs-query sorting game, curl experiments with
`category`, `minPrice`, `inStock`, `sort`, `page`/`size`.

**Output Requirements:** A decision rule (2 sentences) plus 5 example URLs with
their query parameters annotated.

**Accessibility & Supports:** Provide a pre-printed table with rows to classify.

**Teacher Decisions:** Let learners get one wrong on purpose ("put price in the
path") so the decoding pain is felt before the rule is revealed.

---

## Lesson 7: Treat API Changes Carefully

**Learning Goal:** Learners distinguish additive from breaking changes and
version an API consciously.

**Lesson Sequence:**
1. Story: price field → richer pricing structure; who breaks?
2. Read `config/WebConfig.java` and the two versioned `GET /products/{id}` handlers.
3. Compare the v1.0 and v1.1 response JSON (additive `currency`).
4. Hands-on: complete Exercise 7.

**Assessment Evidence:** Learner states whether 5 proposed changes are breaking
or not and picks a versioning strategy, explaining what the "migration path"
for consumers is.
- Assessment answer key: `solutions.md` → Lesson 7.

**Learner Profile / Prior Knowledge:** Comfortable reading `@GetMapping` and
JSON; knows headers exist.

**Learning Activities:** breaking-vs-additive card sort, sending `API-Version`
headers to the app and diffing the two bodies, designing a hypothetical v2.0.

**Output Requirements:** A short "evolving the API" proposal with 2 additive
and 1 breaking change classified.

**Accessibility & Supports:** Side-by-side diff view of v1.0/v1.1 responses;
permit verbal justification.

**Teacher Decisions:** Emphasize that *how* you version (header vs path vs media
type) matters less than being consistent and announcing migration paths.

---

## Lesson 8: Keep Request and Response Formats Consistent

**Learning Goal:** Learners standardize naming, dates, pagination, and error
shapes so the next endpoint looks familiar.

**Lesson Sequence:**
1. Show the inconsistent zoo: `created_at`, `CreatedAt`, `createdAt`, `dateCreated`.
2. List the conventions the app follows (camelCase, ISO-8601 `Instant`,
   `PageResponse` envelope, `ApiError` shape).
3. Read `model/PageResponse.java`, `model/ProductResponse.java`, `model/ApiError.java`.
4. Hands-on: complete Exercise 8.

**Assessment Evidence:** Learner audits a supplied "new" endpoint snippet and
lists every consistency violation, then rewrites it to conform.
- Assessment answer key: `solutions.md` → Lesson 8.

**Learner Profile / Prior Knowledge:** Prior lessons assumed; reading JSON is a
prerequisite.

**Learning Activities:** consistency hunt (find the odd ones out), conventions
poster, rewriting a paginated endpoint to the `PageResponse` shape.

**Output Requirements:** The rewritten endpoint payload plus a 5-line
"Conventions" card.

**Accessibility & Supports:** Provide the conventions card pre-written; the
audit can be done as a checklist instead of prose.

**Teacher Decisions:** Close the loop with the video's core message: *a good API
is one clients can understand without constantly checking documentation*.

---

## Integration Project

**Learning Goal:** Build one coherent REST API that obeys all 8 laws and defend
the design choices.

**Lesson Sequence:** (project sprint over 1–2 sessions)

**Project brief (choose one):**
1. **Library API** — books, authors, copies, loans.
2. **Fitness API** — workouts, exercises, sessions, progress notes.
3. **Booking API** — venues, slots, reservations, cancellations.

**Output Requirements (all eight laws must be visible):**
- Resource URLs only; predictable plural naming (Laws 1, 2).
- Correct method semantics + an idempotency note per method (Law 3).
- Meaningful status codes incl. at least one 409 (Law 4).
- One `ApiError` shape reused everywhere (Law 5).
- Filtering/sorting/pagination via query parameters (Law 6).
- At least one additive version change between v1.0 and v1.1 (Law 7).
- Consistent JSON naming, ISO dates, one pagination envelope (Law 8).

**Assessment Evidence:** Code walkthrough + design review where the learner
answers "how does this obey law N?" for each law, with a peer acting as a
skeptical reviewer.
- Assessment rubric: `exercises.md` → Integration Project.

**Learner Profile / Teacher Decisions:** Work in pairs; reviewers rotate so
everyone both reviews and is reviewed. Prior knowledge: all lessons 1–8.